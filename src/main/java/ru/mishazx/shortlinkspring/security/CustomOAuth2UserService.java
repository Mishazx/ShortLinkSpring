package ru.mishazx.shortlinkspring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.service.UserService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String username = extractUsername(oauth2User, registrationId);
        
        // Находим или создаем пользователя
        User user = userService.findByUsername(username)
                .orElseGet(() -> {
                    String providerId;
                    if ("vk".equals(registrationId)) {
                        // Для VK получаем id из response
                        List<Map<String, Object>> response = oauth2User.getAttribute("response");
                        if (response != null && !response.isEmpty()) {
                            providerId = response.get(0).get("id").toString();
                        } else {
                            throw new OAuth2AuthenticationException("VK user info not found");
                        }
                    } else {
                        // Для других провайдеров
                        providerId = oauth2User.getAttribute("id");
                        if (providerId == null) {
                            throw new OAuth2AuthenticationException("User ID not found");
                        }
                    }

                    User newUser = User.builder()
                            .username(username)
                            .email(oauth2User.getAttribute("email"))
                            .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
                            .providerId(providerId)
                            .build();
                    return userService.save(newUser);
                });

        return new CustomOAuth2User(oauth2User, username, registrationId);
    }

    private String extractUsername(OAuth2User oauth2User, String registrationId) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (registrationId) {
            case "github":
                return oauth2User.getAttribute("login");
                
            case "vk":
                // Для VK API ответ приходит в виде массива в поле "response"
                List<Map<String, Object>> response = (List<Map<String, Object>>) attributes.get("response");
                if (response != null && !response.isEmpty()) {
                    Map<String, Object> userInfo = response.get(0);
                    String firstName = (String) userInfo.get("first_name");
                    String lastName = (String) userInfo.get("last_name");
                    if (firstName != null && lastName != null) {
                        return firstName.toLowerCase() + "." + lastName.toLowerCase();
                    }
                    // Если имя не получено, используем id
                    return "vk_" + userInfo.get("id").toString();
                }
                // Fallback на случай, если структура ответа неожиданная
                return "vk_" + oauth2User.getAttribute("id").toString();
                
            case "yandex":
                String login = oauth2User.getAttribute("login");
                if (login != null) {
                    return login;
                }
                String realName = oauth2User.getAttribute("real_name");
                if (realName != null) {
                    return realName.toLowerCase().replace(" ", ".");
                }
                return oauth2User.getAttribute("default_email").toString().split("@")[0];
                
            default:
                String name = oauth2User.getAttribute("name");
                if (name != null) {
                    return name.toLowerCase().replace(" ", ".");
                }
                return registrationId + "_" + oauth2User.getAttribute("id").toString();
        }
    }
} 