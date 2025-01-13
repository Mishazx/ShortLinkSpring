package ru.mishazx.shortlinkspring.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import ru.mishazx.shortlinkspring.service.UserService;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    @GetMapping("/login")
    public String login(Model model, @RequestParam(required = false) String error,
                        @RequestParam(required = false) String success) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (success != null) {
            model.addAttribute("success", "Registration successful! Please login");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/";
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password) {
        try {
            userService.registerUser(username, email, password);
            return "redirect:/auth/login?success";
        } catch (RuntimeException e) {
            return "redirect:/auth/login?error=" + e.getMessage();
        }
    }
}