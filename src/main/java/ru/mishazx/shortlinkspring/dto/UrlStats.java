package ru.mishazx.shortlinkspring.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class UrlStats {
    private String longUrl;
    private String shortUrl;
    private Integer clicks;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
} 