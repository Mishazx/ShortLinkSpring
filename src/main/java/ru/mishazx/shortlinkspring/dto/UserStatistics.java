package ru.mishazx.shortlinkspring.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserStatistics {
    private final long activeUrls;
    private final long totalClicks;
} 