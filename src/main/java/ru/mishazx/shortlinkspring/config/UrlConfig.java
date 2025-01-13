package ru.mishazx.shortlinkspring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "url")
@Data
public class UrlConfig {
    private Integer defaultClickLimit = 100;
    private Integer defaultExpirationHours = 24;
} 