package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.security.CustomOAuth2User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;

import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class OAuth2UserProcessingService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String username = extractUsername(oauth2User, registrationId);
        
        return new CustomOAuth2User(oauth2User, username, registrationId);
    }

    private String extractUsername(OAuth2User oauth2User, String registrationId) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        if ("github".equals(registrationId)) {
            return (String) attributes.get("login");
        } else if ("vk".equals(registrationId)) {
            return "vk_" + attributes.get("id").toString();
        }
        
        // Для других провайдеров используем name или id
        String name = (String) attributes.get("name");
        if (name != null) {
            return name;
        }
        return registrationId + "_" + attributes.get("id").toString();
    }
} 