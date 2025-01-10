package ru.mishazx.shortlinkspring.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mishazx.shortlinkspring.model.Url;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.security.SecurityUtils;
import ru.mishazx.shortlinkspring.service.UrlService;
import ru.mishazx.shortlinkspring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/url")
@RequiredArgsConstructor
public class UrlController {
    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);
    private final UrlService urlService;
    private final UserService userService;

    @GetMapping("/create")
    public String showCreateForm() {
        return "url/create";
    }

    @GetMapping("/list")
    public String listUrls(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
        
        List<Url> urls = urlService.findAllByUser(user);
        model.addAttribute("urls", urls);
        return "url/list";
    }

    @PostMapping("/create")
    public String createShortUrl(@RequestParam String originalUrl,
                           @RequestParam(required = false) Integer clickLimit,
                           @RequestParam(required = false) Integer expirationHours,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        try {
            logger.info("Creating short URL for: {} by user: {}", originalUrl, userDetails.getUsername());
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
            
            // Вычисляем время истечения, если указано
            LocalDateTime expiresAt = null;
            if (expirationHours != null && expirationHours > 0) {
                expiresAt = LocalDateTime.now().plusHours(expirationHours);
            }

            Url url = urlService.createShortUrl(originalUrl, user, clickLimit, expiresAt);
            model.addAttribute("shortUrl", url.getShortUrl());
            model.addAttribute("fullUrl", originalUrl);
            return "redirect:/url/list";
        } catch (Exception e) {
            logger.error("Error creating short URL: ", e);
            model.addAttribute("error", "Failed to create short URL: " + e.getMessage());
            return "url/create";
        }
    }

    @GetMapping("/{shortUrl}")
    public String redirectToOriginalUrl(@PathVariable String shortUrl) {
        try {
            String originalUrl = urlService.getOriginalUrl(shortUrl);
            return "redirect:" + originalUrl;
        } catch (Exception e) {
            logger.error("Error redirecting short URL: {}", shortUrl, e);
            return "redirect:/url/create?error=URL+not+found";
        }
    }

    @PostMapping("/delete")
    public String deleteUrl(@RequestParam Long urlId,
                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));

        urlService.deleteUrl(urlId, user);
        return "redirect:/url/list";
    }
} 