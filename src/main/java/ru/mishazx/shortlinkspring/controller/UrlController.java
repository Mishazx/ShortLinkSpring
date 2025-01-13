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
import ru.mishazx.shortlinkspring.config.UrlConfig;

@Controller
@RequestMapping("/url")
@RequiredArgsConstructor
public class UrlController {
    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);
    private final UrlService urlService;
    private final UserService userService;
    private final UrlConfig urlConfig;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("defaultClickLimit", urlConfig.getDefaultClickLimit());
        model.addAttribute("defaultExpirationHours", urlConfig.getDefaultExpirationHours());
        return "url/create";
    }

    @GetMapping("/list")
    public String listUrls(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
        
        List<Url> urls = urlService.findAllByUser(user);
        model.addAttribute("urls", urls);
        model.addAttribute("defaultClickLimit", urlConfig.getDefaultClickLimit());
        model.addAttribute("defaultExpirationHours", urlConfig.getDefaultExpirationHours());

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
            
            Integer finalClickLimit = clickLimit == null ? urlConfig.getDefaultClickLimit() : 
                                   clickLimit == 0 ? null : clickLimit;

            Integer finalExpirationHours = expirationHours == null ? urlConfig.getDefaultExpirationHours() : 
                                         expirationHours == 0 ? null : expirationHours;

            LocalDateTime expiresAt = null;
            if (finalExpirationHours != null && finalExpirationHours > 0) {
                expiresAt = LocalDateTime.now().plusHours(finalExpirationHours);
            }

            Url url = urlService.createShortUrl(originalUrl, user, finalClickLimit, expiresAt);
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
            
            // Используем значения по умолчанию, если параметры не указаны
            // Если указан 0, то устанавливаем null для бесконечного использования
            Integer finalClickLimit = clickLimit == null ? urlConfig.getDefaultClickLimit() : 
                                   clickLimit == 0 ? null : clickLimit;

            Integer finalExpirationHours = expirationHours == null ? urlConfig.getDefaultExpirationHours() : 
                                         expirationHours == 0 ? null : expirationHours;

            // Вычисляем время истечения
            LocalDateTime expiresAt = null;
            if (finalExpirationHours != null && finalExpirationHours > 0) {
                expiresAt = LocalDateTime.now().plusHours(finalExpirationHours);
            }

            urlService.updateUrl(urlId, user, finalClickLimit, expiresAt);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating URL: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 