package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService clientService;
    private final RestTemplate restTemplate;

    public User processOAuthPostLogin(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        log.info("OAuth2User attributes:");
        oAuth2User.getAttributes().forEach((key, value) -> 
            log.info("  {} : {}", key, value)
        );

        String provider = getProvider(userRequest);
        String providerId = getProviderId(oAuth2User);
        String username = generateUsername(oAuth2User);
        
        log.info("Extracted data from OAuth2User:");
        log.info("  Username: {}", username);
        log.info("  Provider: {}", provider);
        log.info("  Provider ID: {}", providerId);

        // Сначала ищем пользователя по ID провайдера
        Optional<User> userOptional = findUserByProviderId(provider, providerId);
        
        if (userOptional.isEmpty()) {
            AuthProvider authProvider = AuthProvider.valueOf(provider);
            User user = createNewOAuthUser(username, authProvider, providerId, oAuth2User);
            userRepository.save(user);
            log.info("Created new user: {}", user);
            return user;
        }

        User existingUser = userOptional.get();
        AuthProvider authProvider = AuthProvider.valueOf(provider);
        updateUserOAuthInfo(existingUser, authProvider, providerId);
        userRepository.save(existingUser);
        return existingUser;
    }

    private Optional<User> findUserByProviderId(String provider, String providerId) {
        return switch (provider.toUpperCase()) {
            case "GITHUB" -> userRepository.findByGithubId(providerId);
            case "YANDEX" -> userRepository.findByYandexId(providerId);
            case "VK" -> userRepository.findByVkId(providerId);
            default -> Optional.empty();
        };
    }

    private String generateUsername(OAuth2User oAuth2User) {
        String name = oAuth2User.getAttribute("name");
        if (name != null && !name.isEmpty()) {
            String baseUsername = name.replaceAll("\\s+", "").toLowerCase();
            return ensureUniqueUsername(baseUsername);
        }

        String login = oAuth2User.getAttribute("login");
        if (login != null && !login.isEmpty()) {
            return ensureUniqueUsername(login.toLowerCase());
        }

        // Если не нашли ни имя, ни логин, генерируем случайный username
        return generateRandomUsername();
    }

    private String ensureUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }
        
        return username;
    }

    private String generateRandomUsername() {
        String base = "user";
        int randomNum = (int) (Math.random() * 10000);
        return ensureUniqueUsername(base + randomNum);
    }

    private User createNewOAuthUser(String username, AuthProvider provider, String providerId, OAuth2User oAuth2User) {
        User user = new User();
        user.setUsername(username);
        user.setProvider(provider);
        
        // Опционально сохраняем email, если он есть
        String email = oAuth2User.getAttribute("email");
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        
        // Устанавливаем ID провайдера
        switch (provider) {
            case GITHUB -> user.setGithubId(providerId);
            case YANDEX -> user.setYandexId(providerId);
            case VK -> user.setVkId(providerId);
        }

        return user;
    }

    private String getProviderId(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        if (attributes.containsKey("id")) {
            Object id = attributes.get("id");
            return id.toString();
        } else if (attributes.containsKey("sub")) {
            return attributes.get("sub").toString();
        } else if (attributes.containsKey("response")) {
            List<Map<String, Object>> response = (List<Map<String, Object>>) attributes.get("response");
            if (!response.isEmpty()) {
                Object id = response.get(0).get("id");
                return id.toString();
            }
        }
        
        throw new IllegalStateException("Provider ID not found in OAuth2User attributes");
    }

    private String getEmail(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = null;
        
        // GitHub
        if (attributes.containsKey("email")) {
            email = (String) attributes.get("email");
        }
        // VK
        else if (attributes.containsKey("response")) {
            List<Map<String, Object>> response = (List<Map<String, Object>>) attributes.get("response");
            if (!response.isEmpty()) {
                email = (String) response.get(0).get("email");
            }
        }
        // Yandex
        else if (attributes.containsKey("default_email")) {
            email = (String) attributes.get("default_email");
        }

        if (email == null || email.isEmpty()) {
            log.error("Email not found in OAuth2User attributes: {}", attributes);
            throw new IllegalStateException("Email not found from OAuth2 provider");
        }

        return email;
    }

    private String generateUsername(String email) {
        // Если имя не доступно, используем часть email до @
        return email.substring(0, email.indexOf('@')).toLowerCase();
    }

    private String getProvider(OAuth2UserRequest userRequest) {
        return userRequest.getClientRegistration().getRegistrationId().toUpperCase();
    }

    private void updateUserOAuthInfo(User user, AuthProvider provider, String providerId) {
        switch (provider) {
            case GITHUB -> user.setGithubId(providerId);
            case YANDEX -> user.setYandexId(providerId);
            case VK -> user.setVkId(providerId);
        }
        
        // Если это первый вход через OAuth2, обновляем провайдера
        if (user.getProvider() == null) {
            user.setProvider(provider);
        }
    }
} 