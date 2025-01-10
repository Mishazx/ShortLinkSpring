package ru.mishazx.shortlinkspring.dto;

public class UserStats {
    private final long activeUrls;
    private final long totalClicks;

    public UserStats(long activeUrls, long totalClicks) {
        this.activeUrls = activeUrls;
        this.totalClicks = totalClicks;
    }

    public long getActiveUrls() {
        return activeUrls;
    }

    public long getTotalClicks() {
        return totalClicks;
    }
} 