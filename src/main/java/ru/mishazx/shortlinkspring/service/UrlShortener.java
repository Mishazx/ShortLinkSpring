package ru.mishazx.shortlinkspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishazx.shortlinkspring.repository.UrlRepository;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UrlShortener {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_URL_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();
    
    private final UrlRepository urlRepository;

    public String shorten(String originalUrl) {
        String shortUrl;
        do {
            shortUrl = generateShortUrl();
        } while (urlRepository.existsByShortUrl(shortUrl));
        
        return shortUrl;
    }

    private String generateShortUrl() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
} 