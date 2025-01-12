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

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UrlService urlService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
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