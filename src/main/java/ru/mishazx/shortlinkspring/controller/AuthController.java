package ru.mishazx.shortlinkspring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mishazx.shortlinkspring.dto.UserRegistrationDto;
import ru.mishazx.shortlinkspring.service.UserService;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        // Проверка совпадения паролей
        if (!registrationDto.getPassword().equals(registrationDto.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "error.user", "Passwords do not match");
        }

        // Если есть ошибки валидации, возвращаемся к форме
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerNewUser(registrationDto);
            redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }
}