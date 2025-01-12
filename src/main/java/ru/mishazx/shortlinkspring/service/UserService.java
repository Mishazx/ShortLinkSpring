package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.repository.UserRepository;
import ru.mishazx.shortlinkspring.security.CustomOAuth2User;
import ru.mishazx.shortlinkspring.dto.UserRankingDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public User processOAuthUser(CustomOAuth2User oauth2User) {
        return findByUsername(oauth2User.getUsername())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(oauth2User.getUsername())
                            .email(oauth2User.getEmail())
                            .provider(AuthProvider.valueOf(oauth2User.getProvider().toUpperCase()))
                            .providerId(oauth2User.getId())
                            .build();
                    return save(newUser);
                });
    }

    @Transactional(readOnly = true)
    public List<UserRankingDTO> getTopUsers(int limit) {
        List<User> topUsers = userRepository.findTopUsersByClicks(PageRequest.of(0, limit));
        
        return IntStream.range(0, topUsers.size())
                .mapToObj(i -> {
                    User user = topUsers.get(i);
                    return new UserRankingDTO(
                        user.getUsername(),
                        user.getTotalClicks(),
                        user.getUrls().size(),
                        i + 1
                    );
                })
                .collect(Collectors.toList());
    }
} 