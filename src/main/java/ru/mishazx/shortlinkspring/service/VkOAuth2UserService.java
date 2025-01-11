package ru.mishazx.shortlinkspring.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.mishazx.shortlinkspring.security.CustomOAuth2User;
import java.util.Map;
import java.util.HashMap;

@Service
public class VkOAuth2UserService extends DefaultOAuth2UserService {
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
            
            JsonNode response = new ObjectMapper()
                .readTree(attributes.get("response").toString())
                .get(0);
                
            attributes.put("id", response.get("id").asText());
            attributes.put("name", response.get("first_name").asText() + " " + response.get("last_name").asText());
            
            String email = userRequest.getAdditionalParameters().get("email") != null ? 
                userRequest.getAdditionalParameters().get("email").toString() : 
                userRequest.getAccessToken().getTokenValue();
                
            attributes.put("email", email);
            attributes.put("clientRegistration", userRequest.getClientRegistration().getRegistrationId());
            
            return new CustomOAuth2User(oauth2User, attributes);
            
        } catch (Exception e) {
            // Логируем ошибку для отладки
            e.printStackTrace();
            // Создаем OAuth2Error и выбрасываем исключение
            OAuth2Error oauth2Error = new OAuth2Error(
                "vk_auth_error",
                "VK authentication failed: " + e.getMessage(),
                null
            );
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.getDescription());
        }
    }
} 