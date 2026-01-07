package com.crud_project.crud.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.crud_project.crud.entity.User;
import com.crud_project.crud.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller // define this as a rest controller
@RequestMapping("/auth")
@RequiredArgsConstructor // lombok init
@Slf4j
public class AuthController {
    private final UserService userService; // i kinda dont understand the need to do this when i can ref userservice directly but

    @GetMapping("/register")
    public String loginGet() {
        return "register";
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

    // @PostMapping("/login")
    // public String loginPost(){
    //     return "redirect:/crud";
    // }
}
