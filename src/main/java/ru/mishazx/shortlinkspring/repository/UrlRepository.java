package ru.mishazx.shortlinkspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mishazx.shortlinkspring.model.Url;
import ru.mishazx.shortlinkspring.model.User;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortUrl(String shortUrl);
    boolean existsByShortUrl(String shortUrl);
    List<Url> findByUserOrderByCreatedAtDesc(User user);
    long countByUser(User user);
    
    @Query("SELECT COALESCE(SUM(u.clickCount), 0) FROM Url u WHERE u.user = :user")
    long sumClickCountByUser(@Param("user") User user);

    @Query("SELECT u FROM Url u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now")
    List<Url> findExpiredUrls(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM Url u WHERE u.clickLimit != -1 AND u.clickCount >= u.clickLimit")
    List<Url> findByClickLimitReached();

    List<Url> findAllByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT COUNT(u) FROM Url u WHERE u.user.username = :username")
    long countByUsername(@Param("username") String username);

    @Query("SELECT COALESCE(SUM(u.clickCount), 0) FROM Url u WHERE u.user.username = :username")
    long sumClicksByUsername(@Param("username") String username);

    @Query("SELECT u FROM Url u WHERE u.user.username = :username ORDER BY u.createdAt DESC")
    List<Url> findByUsername(@Param("username") String username);
} 