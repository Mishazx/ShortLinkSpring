package ru.mishazx.shortlinkspring.url;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mishazx.shortlinkspring.model.Url;

import java.time.Instant;
import java.util.List;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Url findByShortUrl(String shortUrl);
    List<Url> findByExpirationTimeBefore(Instant expirationTime);
}
