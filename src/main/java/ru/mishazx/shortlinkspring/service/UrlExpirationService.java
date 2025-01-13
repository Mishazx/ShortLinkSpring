package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.model.Url;
import ru.mishazx.shortlinkspring.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlExpirationService {
    private static final Logger logger = LoggerFactory.getLogger(UrlExpirationService.class);
    private final UrlService urlService;
    
    public List<Url> getUrlsAboutToExpire(User user, long thresholdHours) {
        logger.debug("Checking URLs for user {} with threshold {} hours", user.getUsername(), thresholdHours);
        List<Url> userUrls = urlService.findAllByUser(user);
        logger.debug("Found {} total URLs for user {}", userUrls.size(), user.getUsername());
        
        LocalDateTime now = LocalDateTime.now();
        
        List<Url> expiringUrls = userUrls.stream()
                .filter(url -> url.getExpiresAt() != null)
                .filter(url -> {
                    long hoursUntilExpiration = ChronoUnit.HOURS.between(now, url.getExpiresAt());
                    boolean isExpiring = hoursUntilExpiration > 0 && hoursUntilExpiration <= thresholdHours;
                    if (isExpiring) {
                        logger.debug("URL {} will expire in {} hours", url.getShortUrl(), hoursUntilExpiration);
                    }
                    return isExpiring;
                })
                .collect(Collectors.toList());
        
        logger.debug("Found {} expiring URLs for user {}", expiringUrls.size(), user.getUsername());
        return expiringUrls;
    }
} 