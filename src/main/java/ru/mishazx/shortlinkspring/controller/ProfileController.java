package ru.mishazx.shortlinkspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.service.UserService;
import ru.mishazx.shortlinkspring.service.UrlService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mishazx.shortlinkspring.dto.UserStatistics;
import ru.mishazx.shortlinkspring.dto.UrlStats;
import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UrlService urlService;

    @GetMapping
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

            System.out.println(user.toString());

            UserStatistics stats = urlService.getUserStatistics(user.getUsername());
            List<UrlStats> urlStats = urlService.getUserUrlStats(user.getUsername());
            
            model.addAttribute("user", user);
            model.addAttribute("urlStats", urlStats);
            model.addAttribute("totalClicks", stats.getTotalClicks());
            
            return "profile/index";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String email,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
            
            if (email != null && !email.isEmpty()) {
                user.setEmail(email);
                userService.save(user);
                redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
            }
            
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        }
    }
} 