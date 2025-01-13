package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.security.CustomOAuth2User;
import ru.mishazx.shortlinkspring.security.UserPrincipal;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;

    public UserDetails authenticateUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
            User user = userService.processOAuthUser(oauth2User);
            return UserPrincipal.create(user);
        } else if (authentication.getPrincipal() instanceof String) {
            String username = (String) authentication.getPrincipal();
            return userService.loadUserByUsername(username);
        }
        throw new UsernameNotFoundException("Unsupported authentication type");
    }
} 