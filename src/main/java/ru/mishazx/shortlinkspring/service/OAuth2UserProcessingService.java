package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.security.CustomOAuth2User;

import java.util.Map;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserProcessingService {

    private final UserService userService;

    @SuppressWarnings("unchecked")
    public CustomOAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        log.debug("Processing OAuth2 user for provider: {}", registrationId);
        log.debug("Attributes received: {}", attributes);

        if ("vk".equalsIgnoreCase(registrationId)) {
            return processVkUser(oauth2User, attributes);
        } else if ("github".equalsIgnoreCase(registrationId)) {
            return processGithubUser(oauth2User, attributes);
        } else if ("yandex".equalsIgnoreCase(registrationId)) {
            return processYandexUser(oauth2User, attributes);
        }

        throw new OAuth2AuthenticationException(
            new OAuth2Error("unknown_provider", "Unknown OAuth2 provider: " + registrationId, null)
        );
    }

    private CustomOAuth2User processVkUser(OAuth2User oauth2User, Map<String, Object> attributes) {
        List<Map<String, Object>> response = (List<Map<String, Object>>) attributes.get("response");
        if (response == null || response.isEmpty()) {
            log.error("Invalid VK response: {}", attributes);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_vk_response", "VK response does not contain user data", null)
            );
        }
        
        Map<String, Object> userInfo = response.get(0);
        String providerId = String.valueOf(userInfo.get("id"));
        if (providerId == null) {
            log.error("VK user ID is missing from response: {}", userInfo);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_vk_user", "VK user ID is missing", null)
            );
        }

        String email = (String) userInfo.get("email");
        String firstName = (String) userInfo.getOrDefault("first_name", "");
        String lastName = (String) userInfo.getOrDefault("last_name", "");
        String username = createUsername(firstName, lastName, providerId);

        return createOAuth2User(oauth2User, username, email, AuthProvider.VK, providerId);
    }

    private CustomOAuth2User processGithubUser(OAuth2User oauth2User, Map<String, Object> attributes) {
        String providerId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("login");
        
        if (username == null) {
            log.error("GitHub username is missing from response: {}", attributes);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_github_user", "GitHub username is missing", null)
            );
        }

        return createOAuth2User(oauth2User, username, email, AuthProvider.GITHUB, providerId);
    }

    private CustomOAuth2User processYandexUser(OAuth2User oauth2User, Map<String, Object> attributes) {
        String providerId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("default_email");
        String username = (String) attributes.getOrDefault("login", "yandex_user_" + providerId);
        
        if (email == null) {
            log.error("Yandex email is missing from response: {}", attributes);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_yandex_user", "Yandex email is missing", null)
            );
        }

        return createOAuth2User(oauth2User, username, email, AuthProvider.YANDEX, providerId);
    }

    private String createUsername(String firstName, String lastName, String providerId) {
        String username = (firstName + "_" + lastName).trim();
        return username.isEmpty() || username.equals("_") ? "vk_user_" + providerId : username;
    }

    private CustomOAuth2User createOAuth2User(OAuth2User oauth2User, String username, String email, 
                                            AuthProvider provider, String providerId) {
        log.debug("Creating/updating user: provider={}, providerId={}, username={}", provider, providerId, username);
        
        User user = userService.findByProviderId(providerId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(username)
                            .email(email)
                            .provider(provider)
                            .providerId(providerId)
                            .build();
                    return userService.save(newUser);
                });

        return new CustomOAuth2User(oauth2User, user);
    }
} 