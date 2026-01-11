package com.crud_project.crud.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.crud_project.crud.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserService userService;

    public Boolean registerUser(String username, String password){
        log.debug("Attempting to register user: {}", username);
        if (userService.getUserByName(username) != null) {
            log.error("User {} already exists", username);
            return false;
        }
        // Hash password and save user
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);
        User user = new User();
        user.setUserName(username);
        user.setHashedPassword(hashedPassword);
        User createdUser = userService.createUser(user);
        log.info("User created: {}", createdUser);
        return true;
    }

    public void logout(HttpServletRequest request){
        log.info("Logging out user: {}", request.getRemoteUser());
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
    }
}
