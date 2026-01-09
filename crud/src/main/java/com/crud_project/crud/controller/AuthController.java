package com.crud_project.crud.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.crud_project.crud.entity.User;
import com.crud_project.crud.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService; // i kinda dont understand the need to do this when i can ref userservice directly but

    public static final Map<String, String> requestedUrlMap = Map.of(
        "/auth/login", "login",
        "/auth/register", "register",
        "/", "home",
        "/home", "home"
    );
    // This redirects anyone who is already authenticated to the /crud page
    public static String authRedirect(HttpServletRequest request, Authentication authentication){
        log.info("Authenticating user: {}", authentication);
        String requestedUrl = request.getRequestURI();
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("User {} already authenticated, redirecting to /crud", authentication.getName());
            return "redirect:/crud";
        }
        String destination = requestedUrlMap.get(requestedUrl);
        log.info("User not authenticated, redirecting to {}", destination);
        return destination;
    }
    
    @GetMapping({"/login", "/register"})
    public String loginGet(HttpServletRequest request, Authentication authentication) {
        return authRedirect(request, authentication);
    }

    @PostMapping("/register")
    public String registerPost(@RequestParam String username, @RequestParam String password) {
        log.debug("Attempting to register user: {}", username);
        // Hash password and save user
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);
        
        // check if username already exists
        if (userService.getUserByName(username) != null) {
            return "redirect:/auth/register?error=true";
        }

        User user = new User();
        user.setUserName(username);
        user.setHashedPassword(hashedPassword);
        User createdUser = userService.createUser(user);
        log.info("User created: {}", createdUser);

        return "redirect:/auth/login";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request) {
        // how to remove authentication token
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
        return "redirect:/home";
    }
}
