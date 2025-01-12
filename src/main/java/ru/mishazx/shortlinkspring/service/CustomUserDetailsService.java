package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.repository.UserRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities("ROLE_USER")
                .build();
    }

    public DefaultOAuth2User createUserDetails(User user) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", user.getUsername());
        if (user.getEmail() != null) {
            attributes.put("email", user.getEmail());
        }
        attributes.put("id", user.getId());
        
        if (user.getGithubId() != null) attributes.put("github_id", user.getGithubId());
        if (user.getYandexId() != null) attributes.put("yandex_id", user.getYandexId());
        if (user.getVkId() != null) attributes.put("vk_id", user.getVkId());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "username"
        );
    }
} 