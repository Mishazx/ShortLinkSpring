package ru.mishazx.shortlinkspring.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.http.ResponseEntity;

import ru.mishazx.shortlinkspring.model.Url;
import ru.mishazx.shortlinkspring.model.User;
import ru.mishazx.shortlinkspring.service.UrlService;
import ru.mishazx.shortlinkspring.service.UserService;

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
    public String listUrls(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
        
        List<Url> urls = urlService.findAllByUser(user);
        model.addAttribute("urls", urls);

        String fullUrl = request.getScheme() + "://" + request.getServerName() + request.getContextPath() + "/url";
        model.addAttribute("serverUrl", fullUrl);
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
    public String redirectToOriginalUrl(@PathVariable String shortUrl, Model model) {
        try {
            String originalUrl = urlService.getOriginalUrl(shortUrl);
            return "redirect:" + originalUrl;
        } catch (Exception e) {
            logger.error("Error redirecting short URL: {}", shortUrl, e);
            String errorMessage;
            if (e.getMessage().contains("expired")) {
                errorMessage = "Срок действия ссылки истек";
            } else if (e.getMessage().contains("limit")) {
                errorMessage = "Достигнут лимит переходов по ссылке";
            } else if (e.getMessage().contains("not found")) {
                errorMessage = "Ссылка не найдена или была удалена";
            } else {
                errorMessage = "Произошла ошибка при обработке ссылки";
            }
            model.addAttribute("errorMessage", errorMessage);
            return "url/error";
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

    @PutMapping("/update")
    public ResponseEntity<?> updateUrl(@RequestParam Long urlId,
                                     @RequestParam(required = false) Integer clickLimit,
                                     @RequestParam(required = false) Integer expirationHours,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
            
            // Вычисляем время истечения, если указано
            LocalDateTime expiresAt = null;
            if (expirationHours != null && expirationHours > 0) {
                expiresAt = LocalDateTime.now().plusHours(expirationHours);
            }

            urlService.updateUrl(urlId, user, clickLimit, expiresAt);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating URL: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 