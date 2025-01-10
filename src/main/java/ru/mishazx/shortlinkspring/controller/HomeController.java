package ru.mishazx.shortlinkspring.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.mishazx.shortlinkspring.service.UrlService;
import ru.mishazx.shortlinkspring.dto.UserStats;

@Controller
public class HomeController {
    
    private final UrlService urlService;

    public HomeController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserStats stats = urlService.getUserStats(username);
            model.addAttribute("urlCount", stats.getActiveUrls());
            model.addAttribute("totalClicks", stats.getTotalClicks());
        }
        return "home";
    }
} 