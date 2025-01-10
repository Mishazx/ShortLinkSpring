package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;

import java.util.Map;

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
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            
            final String email;
            final String name;
            
            switch (registrationId.toLowerCase()) {
                case "github" -> {
                    email = oauth2User.getAttribute("email");
                    name = oauth2User.getAttribute("login");
                }
                case "yandex" -> {
                    email = oauth2User.getAttribute("default_email");
                    name = oauth2User.getAttribute("login");
                    
                    if (email == null || name == null) {
                        Map<String, Object> attributes = oauth2User.getAttributes();
                        throw new IllegalStateException("Missing required attributes from Yandex. Received: " + attributes);
                    }
                }
                default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
            }

            if (name == null) {
                throw new IllegalStateException("Username cannot be null");
            }

            final String finalEmail = email != null ? email : name + "@" + registrationId + ".user";
            
            User user = userService.findByEmail(finalEmail)
                    .orElseGet(() -> {
                        return userService.findByUsername(name)
                                .orElseGet(() -> {
                                    User newUser = User.builder()
                                            .username(name)
                                            .email(finalEmail)
                                            .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
                                            .totalClicks(0L)
                                            .build();
                                    return userService.save(newUser);
                                });
                    });

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword() != null ? user.getPassword() : "")
                    .roles("USER")
                    .build();
        }
        
        throw new IllegalArgumentException("Unsupported authentication type");
    }
} 