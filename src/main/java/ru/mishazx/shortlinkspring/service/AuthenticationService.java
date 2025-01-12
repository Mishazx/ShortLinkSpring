package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.security.CustomOAuth2User;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {
    
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .roles("USER")
                .build();
    }

    public UserDetails authenticateUser(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
            User user = userService.processOAuthUser(oauth2User);
            
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                "",
                true,
                true,
                true,
                true,
                Collections.emptyList()
            );
        }
        return null;
    }
} 