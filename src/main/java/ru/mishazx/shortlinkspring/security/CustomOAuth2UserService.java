package ru.mishazx.shortlinkspring.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        Map<String, Object> attributes = oauth2User.getAttributes();
        String providerId = extractProviderId(registrationId, attributes);
        String email = extractEmail(registrationId, attributes);
        String username = extractUsername(registrationId, attributes);

        Optional<User> userOptional = userRepository.findByProviderId(providerId);
        User user = userOptional.orElseGet(() -> {
            User newUser = User.builder()
                    .username(username)
                    .email(email)
                    .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
                    .providerId(providerId)
                    .build();
            return userRepository.save(newUser);
        });

        return new CustomOAuth2User(oauth2User, user);
    }

    private String extractProviderId(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "github":
                return String.valueOf(attributes.get("id"));
            case "yandex":
                return String.valueOf(attributes.get("id"));
            default:
                throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }
    }

    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "github":
                return (String) attributes.get("email");
            case "yandex":
                return (String) attributes.get("default_email");
            default:
                throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }
    }

    private String extractUsername(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "github":
                return (String) attributes.get("login");
            case "yandex":
                return (String) attributes.get("login");
            default:
                throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }
    }
} 