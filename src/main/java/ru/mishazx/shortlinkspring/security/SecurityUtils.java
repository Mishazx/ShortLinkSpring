package ru.mishazx.shortlinkspring.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public class SecurityUtils {
    
    public static String getUsername(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
            return oauth2Auth.getPrincipal().getAttribute("login");
        }
        return authentication.getName();
    }
} 