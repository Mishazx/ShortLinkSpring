package ru.mishazx.shortlinkspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mishazx.shortlinkspring.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByGithubId(String githubId);
    Optional<User> findByYandexId(String yandexId);
    Optional<User> findByVkId(String vkId);
} 