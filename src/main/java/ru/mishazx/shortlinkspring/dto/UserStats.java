package ru.mishazx.shortlinkspring.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class UserStats {
    private String longUrl;
    private String shortUrl;
    private Long clicks;
    private LocalDateTime createdAt;
} 