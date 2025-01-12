package ru.mishazx.shortlinkspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.mishazx.shortlinkspring.service.UrlService;
import ru.mishazx.shortlinkspring.dto.UserStatistics;
import ru.mishazx.shortlinkspring.dto.UrlStats;
import ru.mishazx.shortlinkspring.dto.UserRankingDTO;
import ru.mishazx.shortlinkspring.service.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UrlService urlService;
    private final UserService userService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Получаем топ-5 пользователей
        List<UserRankingDTO> topUsers = userService.getTopUsers(5);
        model.addAttribute("topUsers", topUsers);

        if (userDetails != null) {
            String username = userDetails.getUsername();
            UserStatistics stats = urlService.getUserStatistics(username);
            List<UrlStats> urlStats = urlService.getUserUrlStats(username);
            
            model.addAttribute("username", username);
            model.addAttribute("urlCount", stats.getActiveUrls());
            model.addAttribute("totalClicks", stats.getTotalClicks());
            model.addAttribute("urlStats", urlStats);
            return "home";
        }
        return "index";
    }
} 