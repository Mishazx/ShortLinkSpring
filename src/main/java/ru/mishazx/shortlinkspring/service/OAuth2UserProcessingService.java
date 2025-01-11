package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OAuth2UserProcessingService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            String email;
            String name;
            String id;
            String username;
            
            switch (registrationId) {
                case "github":
                    email = oauth2User.getAttribute("email");
                    name = oauth2User.getAttribute("login");
                    id = oauth2User.getAttribute("id").toString();
                    username = "github_" + id;
                    break;
                case "yandex":
                    email = oauth2User.getAttribute("default_email");
                    name = oauth2User.getAttribute("login");
                    id = oauth2User.getAttribute("id").toString();
                    username = "yandex_" + id;
                    break;
                case "vk":
                    System.out.println("VK Response: " + attributes);
                    System.out.println("Additional Parameters: " + userRequest.getAdditionalParameters());
                    
                    ObjectMapper mapper = new ObjectMapper();
                    String responseJson = mapper.writeValueAsString(attributes.get("response"));
                    System.out.println("Response JSON: " + responseJson);
                    
                    JsonNode responseNode = mapper.readTree(responseJson);
                    JsonNode userNode = responseNode.get(0);
                    
                    email = userRequest.getAdditionalParameters().get("email") != null ?
                        userRequest.getAdditionalParameters().get("email").toString() :
                        userNode.get("first_name").asText() + "@vk.user";
                    
                    name = userNode.get("first_name").asText() + " " + userNode.get("last_name").asText();
                    id = userNode.get("id").asText();
                    username = "vk_" + id;
                    break;
                default:
                    throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_provider", 
                            "Unsupported OAuth2 provider: " + registrationId, 
                            null)
                    );
            }
            
            Map<String, Object> processedAttributes = new HashMap<>();
            processedAttributes.put("email", email);
            processedAttributes.put("name", name);
            processedAttributes.put("id", id);
            processedAttributes.put("username", username);
            processedAttributes.put("clientRegistration", registrationId);
            
            return new CustomOAuth2User(oauth2User, processedAttributes);
            
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("user_processing_error", 
                    "Failed to process user details: " + e.getMessage(), 
                    null),
                e
            );
        }
    }
} 