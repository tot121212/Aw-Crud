package com.crud_project.crud.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final AuthController authController;

    @GetMapping({"/", "/home"})
    public String getHome(HttpServletRequest request, Authentication authentication) {
        return authController.authRedirect(request, authentication);
    }
}
