package ru.mishazx.shortlinkspring.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.mishazx.shortlinkspring.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderId(String providerId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findAllByOrderByTotalClicksDesc(Pageable pageable);
} 