package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import ru.mishazx.shortlinkspring.repository.UserRepository;
import ru.mishazx.shortlinkspring.security.UserPrincipal;
import ru.mishazx.shortlinkspring.security.CustomOAuth2User;
import ru.mishazx.shortlinkspring.dto.UserRankingDTO;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return UserPrincipal.create(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User processOAuthUser(CustomOAuth2User oauth2User) {
        return findByProviderId(oauth2User.getUser().getProviderId())
                .orElseGet(() -> save(oauth2User.getUser()));
    }

    @Transactional(readOnly = true)
    public List<UserRankingDTO> getTopUsers(int limit) {
        List<User> users = userRepository.findAllByOrderByTotalClicksDesc(PageRequest.of(0, limit));
        
        return IntStream.range(0, users.size())
                .mapToObj(i -> new UserRankingDTO(
                    users.get(i).getUsername(),
                    users.get(i).getTotalClicks(),
                    users.get(i).getCreatedUrls(),
                    i + 1
                ))
                .collect(Collectors.toList());
    }

    public User registerUser(String username, String email, String password) {
        if (existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (email != null && existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .provider(AuthProvider.LOCAL)
                .build();

        return save(user);
    }
} 