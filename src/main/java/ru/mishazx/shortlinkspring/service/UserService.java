package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.repository.UserRepository;
import ru.mishazx.shortlinkspring.security.CustomOAuth2User;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByProviderId(String providerName, String providerId) {
        AuthProvider provider = AuthProvider.valueOf(providerName.toUpperCase());
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    public User registerUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .provider(AuthProvider.LOCAL)
                .totalClicks(0L)
                .build();

        return userRepository.save(user);
    }

    public User processOAuthPostLogin(CustomOAuth2User oauth2User) {
        String email = oauth2User.getEmail();
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isEmpty()) {
            User newUser = User.builder()
                .email(email)
                .username(oauth2User.getName())
                .provider(AuthProvider.valueOf(oauth2User.getProvider().toUpperCase()))
                .providerId(oauth2User.getId())
                .totalClicks(0L)
                .build();
            return userRepository.save(newUser);
        }
        
        return existingUser.get();
    }
} 