package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
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
@RequiredArgsConstructor
public class OAuth2UserProcessingService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            String email;
            String name;
            String id;
            
            switch (registrationId) {
                case "github":
                    email = oauth2User.getAttribute("email");
                    name = oauth2User.getAttribute("login");
                    id = oauth2User.getAttribute("id").toString();
                    break;
                case "yandex":
                    email = oauth2User.getAttribute("default_email");
                    name = oauth2User.getAttribute("login");
                    id = oauth2User.getAttribute("id").toString();
                    break;
                case "vk":
                    JsonNode response = new ObjectMapper()
                        .readTree(attributes.get("response").toString())
                        .get(0);
                    email = userRequest.getAdditionalParameters().get("email") != null ?
                        userRequest.getAdditionalParameters().get("email").toString() :
                        response.get("first_name").asText() + "@vk.user";
                    name = response.get("first_name").asText() + " " + response.get("last_name").asText();
                    id = response.get("id").asText();
                    break;
                default:
                    throw new OAuth2AuthenticationException("Unsupported OAuth2 provider");
            }
            
            Map<String, Object> processedAttributes = new HashMap<>();
            processedAttributes.put("email", email);
            processedAttributes.put("name", name);
            processedAttributes.put("id", id);
            processedAttributes.put("clientRegistration", registrationId);
            
            return new CustomOAuth2User(oauth2User, processedAttributes);
            
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("processing_error", "Failed to process user details: " + e.getMessage(), null)
            );
        }
    }
} 