package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.security.CustomOAuth2User;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserProcessingService {

    private final UserService userService;

    public CustomOAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String providerId = extractProviderId(registrationId, attributes);
        String email = extractEmail(registrationId, attributes);
        String username = extractUsername(registrationId, attributes);

        User user = userService.findByProviderId(providerId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(username)
                            .email(email)
                            .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
                            .providerId(providerId)
                            .build();
                    return userService.save(newUser);
                });

        return new CustomOAuth2User(oauth2User, user);
    }

    private String extractProviderId(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "github":
                return String.valueOf(attributes.get("id"));
            case "vk":
                if (attributes.containsKey("response")) {
                    var response = (java.util.ArrayList<?>) attributes.get("response");
                    if (!response.isEmpty()) {
                        var userInfo = (Map<String, Object>) response.get(0);
                        return String.valueOf(userInfo.get("id"));
                    }
                }
                return String.valueOf(attributes.get("id"));
            case "yandex":
                return String.valueOf(attributes.get("id"));
            default:
                throw new RuntimeException("Unsupported provider: " + registrationId);
        }
    }

    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "github":
                return (String) attributes.get("email");
            case "vk":
                if (attributes.containsKey("response")) {
                    var response = (java.util.ArrayList<?>) attributes.get("response");
                    if (!response.isEmpty()) {
                        var userInfo = (Map<String, Object>) response.get(0);
                        return (String) userInfo.get("email");
                    }
                }
                return null;
            case "yandex":
                return (String) attributes.get("default_email");
            default:
                throw new RuntimeException("Unsupported provider: " + registrationId);
        }
    }

    private String extractUsername(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "github":
                return (String) attributes.get("login");
            case "vk":
                if (attributes.containsKey("response")) {
                    var response = (java.util.ArrayList<?>) attributes.get("response");
                    if (!response.isEmpty()) {
                        var userInfo = (Map<String, Object>) response.get(0);
                        String firstName = (String) userInfo.get("first_name");
                        String lastName = (String) userInfo.get("last_name");
                        return firstName + "_" + lastName;
                    }
                }
                return "vk_user_" + attributes.get("id");
            case "yandex":
                return (String) attributes.get("login");
            default:
                throw new RuntimeException("Unsupported provider: " + registrationId);
        }
    }
} 