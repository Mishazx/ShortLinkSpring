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
            OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
            CustomOAuth2User oauth2User = (CustomOAuth2User) oauth2Auth.getPrincipal();
            
            String email = oauth2User.getEmail();
            String provider = oauth2Auth.getAuthorizedClientRegistrationId();
            String providerId = oauth2User.getId();
            String username = oauth2User.getAttributes().get("username").toString();
            
            // Сначала пробуем найти по providerId
            Optional<User> userByProvider = userService.findByProviderId(provider, providerId);
            User user = userByProvider.orElseGet(() -> {
                // Если не нашли по providerId, ищем по email
                Optional<User> userByEmail = userService.findByEmail(email);
                if (userByEmail.isPresent()) {
                    User existingUser = userByEmail.get();
                    existingUser.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
                    existingUser.setProviderId(providerId);
                    return userService.save(existingUser);
                }
                
                // Если пользователя нет, создаем нового
                return userService.save(User.builder()
                    .email(email)
                    .username(username)
                    .provider(AuthProvider.valueOf(provider.toUpperCase()))
                    .providerId(providerId)
                    .totalClicks(0L)
                    .build());
            });

            return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password("")
                .authorities("ROLE_USER")
                .build();
        }

        throw new IllegalArgumentException("Unsupported authentication type");
    }
} 