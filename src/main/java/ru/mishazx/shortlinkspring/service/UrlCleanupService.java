package ru.mishazx.shortlinkspring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishazx.shortlinkspring.model.Url;
import ru.mishazx.shortlinkspring.repository.UrlRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class UrlCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(UrlCleanupService.class);
    
    private final UrlRepository urlRepository;

    public UrlCleanupService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Scheduled(fixedRate = 60000) // Запускается каждую минуту
    @Transactional
    public void cleanupExpiredUrls() {
        logger.info("Starting URL cleanup task");

        List<Url> expiredUrls = urlRepository.findExpiredUrls(LocalDateTime.now());

        List<Url> clickLimitReachedUrls = urlRepository.findByClickLimitReached();

        List<Url> urlsToDelete = new ArrayList<>();
        urlsToDelete.addAll(expiredUrls);
        urlsToDelete.addAll(clickLimitReachedUrls);

        for (Url url : urlsToDelete) {
            urlRepository.delete(url);
        }
        
        if (!urlsToDelete.isEmpty()) {
            logger.info("Cleaned up {} URLs", urlsToDelete.size());
        }
    }
} 