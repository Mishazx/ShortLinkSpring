package ru.mishazx.shortlinkspring.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mishazx.shortlinkspring.model.Url;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.service.UrlExpirationService;
import ru.mishazx.shortlinkspring.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/url-expiration")
@RequiredArgsConstructor
public class UrlExpirationController {
    private static final Logger logger = LoggerFactory.getLogger(UrlExpirationController.class);
    private final UrlExpirationService urlExpirationService;
    private final UserService userService;
    private static final long EXPIRATION_THRESHOLD_HOURS = 24; // Уведомлять за 24 часа
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Data
    public static class UrlData {
        @JsonProperty("shortUrl")
        private String shortUrl;
        
        @JsonProperty("hoursLeft")
        private long hoursLeft;
        
        @JsonProperty("expiresAt")
        private String expiresAt;
    }

    @Data
    public static class ExpirationResponse {
        @JsonProperty("hasExpiring")
        private boolean hasExpiring;
        
        @JsonProperty("urls")
        private List<UrlData> urls;
    }

    @GetMapping("/check")
    public ResponseEntity<ExpirationResponse> checkExpiringUrls(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Checking expiring URLs for user: {}", userDetails.getUsername());
        
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Url> expiringUrls = urlExpirationService.getUrlsAboutToExpire(user, EXPIRATION_THRESHOLD_HOURS);
        logger.info("Found {} expiring URLs for user: {}", expiringUrls.size(), user.getUsername());
        
        ExpirationResponse response = new ExpirationResponse();
        response.setUrls(new ArrayList<>());
        response.setHasExpiring(!expiringUrls.isEmpty());
        
        if (!expiringUrls.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            
            for (Url url : expiringUrls) {
                UrlData urlData = new UrlData();
                urlData.setShortUrl(url.getShortUrl());
                urlData.setHoursLeft(ChronoUnit.HOURS.between(now, url.getExpiresAt()));
                urlData.setExpiresAt(url.getExpiresAt().format(DATE_FORMATTER));
                
                response.getUrls().add(urlData);
                logger.debug("Added URL data: {}", urlData);
            }
        }
        
        logger.debug("Sending response: {}", response);
        return ResponseEntity.ok(response);
    }
} 