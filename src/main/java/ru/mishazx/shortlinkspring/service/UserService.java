package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.dto.UserRegistrationDto;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User registerNewUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(registrationDto.getUsername())
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .provider(AuthProvider.LOCAL)
                .build();

        return userRepository.save(user);
    }

    public User processOAuthUser(String email, String name, AuthProvider provider, String providerId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Обновляем ID провайдера в зависимости от типа
            updateProviderInfo(user, provider, providerId);
            return userRepository.save(user);
        }

        // Создаем нового пользователя
        User user = User.builder()
                .email(email)
                .username(generateUsername(name, email))
                .provider(provider)
                .build();

        // Устанавливаем ID провайдера
        updateProviderInfo(user, provider, providerId);
        
        return userRepository.save(user);
    }

    private void updateProviderInfo(User user, AuthProvider provider, String providerId) {
        switch (provider) {
            case GITHUB -> user.setGithubId(providerId);
            case YANDEX -> user.setYandexId(providerId);
            case VK -> user.setVkId(providerId);
        }
    }

    private String generateUsername(String name, String email) {
        if (name != null && !name.isEmpty()) {
            String username = name.replaceAll("\\s+", "").toLowerCase();
            if (!userRepository.existsByUsername(username)) {
                return username;
            }
        }
        
        // Если имя не подходит, используем часть email
        String baseUsername = email.substring(0, email.indexOf('@')).toLowerCase();
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }
        
        return username;
    }
} 