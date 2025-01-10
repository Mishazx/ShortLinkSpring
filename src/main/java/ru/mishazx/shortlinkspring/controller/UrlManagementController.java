package ru.mishazx.shortlinkspring.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mishazx.shortlinkspring.security.SecurityUtils;
import ru.mishazx.shortlinkspring.service.UrlService;
import ru.mishazx.shortlinkspring.service.UserService;

@Controller
@RequestMapping("/urls")
public class UrlManagementController {
    

} 