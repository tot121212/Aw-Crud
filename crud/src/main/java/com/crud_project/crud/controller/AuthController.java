package com.crud_project.crud.controller;

import java.util.Map;

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

    private static final Map<String, String> UriToTemplateNameMap = Map.of(
        "/auth/login", "login",
        "/auth/register", "register",
        "/", "home",
        "/home", "home"
    );


    // This redirects anyone who is already authenticated to the /crud page
    public String authRedirect(HttpServletRequest request, Authentication authentication, HttpSession session){
        // if none provided default to home
        if (request == null) {
            log.info("No request provided, directing to /home");
            return "/home";
        }
        String requestedUri = request.getRequestURI();
        if (authentication != null && authentication.isAuthenticated() &&
            !authentication.getName().equals("anonymousUser")) {
            log.info("User {} already authenticated, directing to " + "/crud", authentication.getName());
            return "/crud";
        }
        String destination = UriToTemplateNameMap.get(requestedUri);
        log.info("User not authenticated, directing to {}", destination);
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
        return "redirect:" + "/auth" + "/register" + "?error";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request) {
        userService.logout(request);
        return "redirect:" + "/login" + "?logout";
    }
}
