package ru.mishazx.shortlinkspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mishazx.shortlinkspring.dto.UserRankingDTO;
import ru.mishazx.shortlinkspring.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final UserService userService;

    @GetMapping
    public String showRanking(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "clicks") String sortBy,
            Model model
    ) {
        List<UserRankingDTO> rankings = userService.getTopUsers(limit);
        model.addAttribute("rankings", rankings);
        model.addAttribute("currentLimit", limit);
        model.addAttribute("currentSort", sortBy);
        return "ranking/list";
    }
} 