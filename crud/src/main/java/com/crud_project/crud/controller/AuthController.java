package com.crud_project.crud.controller;


import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.crud_project.crud.controller.utils.StringValidation;
import com.crud_project.crud.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;

    /**
     *  Redirects anyone who is already authenticated to the "/crud" endpoint instead
     */ 
    public String authRedirect(HttpServletRequest request, Authentication authentication, HttpSession session){
        if (authentication != null && 
            authentication.isAuthenticated() &&
            !(authentication instanceof AnonymousAuthenticationToken)) {
            log.info("User {} is already authenticated, directing to " + "/crud", authentication.getName());
            return "redirect:" + "/crud";
        }

        if (request == null) {
            log.warn("No request provided, directing to /home");
            return "redirect:" + "/home";
        }


        String destination = request.getRequestURI().substring(1);
        log.info("User is not authenticated, showing requested template");
        

         // if none provided default to home
        if (destination.equals("") || destination.equals("/")) {
            log.warn("Destination empty, directing to " + "/home");
            return "redirect:" + "/home";
        }

        return destination;
    }
    
    @GetMapping({"/login", "/register"})
    public String loginGet(HttpServletRequest request, Authentication authentication, HttpSession session) {
        return authRedirect(request, authentication, session);
    }


    @PostMapping("/register")
    public String registerPost(@RequestParam String username, @RequestParam String password) {
        if (StringValidation.isValidUsername(username) 
        && StringValidation.isValidPassword(password) 
        && (userService.registerUser(username, password) != null)) {
            return "redirect:" + "/auth" + "/login";
        }
        return "redirect:" + "/auth" + "/register" + "?error=true";
    }
}
