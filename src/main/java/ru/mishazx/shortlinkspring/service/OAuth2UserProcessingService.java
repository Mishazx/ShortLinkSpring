package ru.mishazx.shortlinkspring.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;

import java.util.Map;

@Service
public class OAuth2UserProcessingService {
    
    private final UserService userService;

    public OAuth2UserProcessingService(UserService userService) {
        this.userService = userService;
    }

    public UserDetails processOAuthUser(String provider, Map<String, Object> attributes) {
        if ("github".equals(provider)) {
            return processGitHubUser(attributes);
        } else if ("yandex".equals(provider)) {
            return processYandexUser(attributes);
        }
        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    private UserDetails processGitHubUser(Map<String, Object> attributes) {
        String login = (String) attributes.get("login");
        String email = (String) attributes.get("email");
        String id = attributes.get("id").toString();
        
        ru.mishazx.shortlinkspring.model.User user = userService.findByProviderId("github", id)
            .orElseGet(() -> {
                ru.mishazx.shortlinkspring.model.User newUser = ru.mishazx.shortlinkspring.model.User.builder()
                    .username(login)
                    .email(email)
                    .provider(AuthProvider.GITHUB)
                    .providerId(id)
                    .totalClicks(0L)
                    .build();
                return userService.save(newUser);
            });

        return User.withUsername(user.getUsername())
            .password(user.getPassword() != null ? user.getPassword() : "")
            .roles("USER")
            .build();
    }

    private UserDetails processYandexUser(Map<String, Object> attributes) {
        String id = attributes.get("id").toString();
        String email = (String) attributes.get("default_email");
        String login = "yandex_" + id;

        ru.mishazx.shortlinkspring.model.User user = userService.findByProviderId("yandex", id)
            .orElseGet(() -> {
                ru.mishazx.shortlinkspring.model.User newUser = ru.mishazx.shortlinkspring.model.User.builder()
                    .username(login)
                    .email(email)
                    .provider(AuthProvider.YANDEX)
                    .providerId(id)
                    .totalClicks(0L)
                    .build();
                return userService.save(newUser);
            });

        return User.withUsername(user.getUsername())
            .password(user.getPassword() != null ? user.getPassword() : "")
            .roles("USER")
            .build();
    }
} 