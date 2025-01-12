package ru.mishazx.shortlinkspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRankingDTO {
    private String username;
    private Long totalClicks;
    private Integer urlCount;
    private Integer rank;
} 