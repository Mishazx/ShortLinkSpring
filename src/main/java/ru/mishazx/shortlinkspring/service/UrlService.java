package ru.mishazx.shortlinkspring.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishazx.shortlinkspring.model.Url;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.repository.UrlRepository;
import ru.mishazx.shortlinkspring.service.UserService;
import ru.mishazx.shortlinkspring.dto.UserStats;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UrlService {
    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);
    private final UrlRepository urlRepository;
    private final UserService userService;
    private final UrlShortener urlShortener;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Url createShortUrl(String originalUrl, User user, Integer clickLimit, LocalDateTime expiresAt) {
        String shortUrl = urlShortener.shorten(originalUrl);
        
        Url url = Url.builder()
                .originalUrl(originalUrl)
                .shortUrl(shortUrl)
                .user(user)
                .clickCount(0)
                .clickLimit(clickLimit != null ? clickLimit : -1)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        return urlRepository.save(url);
    }

    @Transactional(readOnly = true)
    public List<Url> findAllByUser(User user) {
        return urlRepository.findAllByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public String getOriginalUrl(String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        if (url.isExpired() || url.isClickLimitReached()) {
            throw new RuntimeException("URL has expired or reached click limit");
        }

        url.incrementClickCount();
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    public long countUrlsByUsername(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return urlRepository.countByUser(user);
    }

    public long getTotalClicksByUsername(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return urlRepository.sumClickCountByUser(user);
    }

    public UserStats getUserStats(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        long activeUrls = urlRepository.countByUser(user);
        long totalClicks = user.getTotalClicks();
        return new UserStats(activeUrls, totalClicks);
    }

    public void deleteUrl(Long urlId, User user) {
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        if (!url.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to delete this URL");
        }

        urlRepository.delete(url);
    }
} 