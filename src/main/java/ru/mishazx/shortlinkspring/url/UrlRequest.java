package ru.mishazx.shortlinkspring.url;

public class UrlRequest {
    private String originalUrl;
    private String userId;
    private long expirationTime;
    private int clickLimit;

    // Геттеры и сеттеры
    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public int getClickLimit() {
        return clickLimit;
    }

    public void setClickLimit(int clickLimit) {
        this.clickLimit = clickLimit;
    }
}