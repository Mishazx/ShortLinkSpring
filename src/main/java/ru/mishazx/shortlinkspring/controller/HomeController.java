package ru.mishazx.shortlinkspring.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.mishazx.shortlinkspring.dto.UserRankingDTO;
import ru.mishazx.shortlinkspring.service.UserService;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        List<UserRankingDTO> topUsers = userService.getTopUsers(5);
        model.addAttribute("topUsers", topUsers);

        return "index";
    }
} 