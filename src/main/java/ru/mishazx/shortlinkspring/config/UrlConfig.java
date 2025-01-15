package ru.mishazx.shortlinkspring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlConfig {
    @Value("${url.default-click-limit:100}")
    private Integer defaultClickLimit;

    @Value("${url.default-expiration-hours:24}")
    private Integer defaultExpirationHours;

    public Integer getDefaultClickLimit() {
        return defaultClickLimit;
    }

    public Integer getDefaultExpirationHours() {
        return defaultExpirationHours;
    }
} 