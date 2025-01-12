package ru.mishazx.shortlinkspring.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @Query("SELECT u FROM User u WHERE u.totalClicks > 0 ORDER BY u.totalClicks DESC")
    List<User> findTopUsersByClicks(Pageable pageable);
} 