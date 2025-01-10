package ru.mishazx.shortlinkspring.url;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UrlGenerator {
    public String generateShortUrl() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
