package com.crud_project.crud.controller;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.stereotype.Controller;

import com.crud_project.crud.entity.User;
import com.crud_project.crud.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller // define this as a rest controller
// @RequestMapping("/crud")
@RequiredArgsConstructor // lombok init
@Slf4j
public class CrudController {
    private final UserService userService; // i kinda dont understand the need to do this when i can ref userservice directly but

    @Value("classpath:resources/static/data/dbUsernames.txt")
    Resource dbUsernamesResource;
    @Value("classpath:resources/static/data/dbPassword.txt")
    Resource dbPasswordResource;

    // interact with and filter requests here
    @PostMapping("/create-test-users")
    public String createTestUsers() {
        log.debug("Attempting to create test users");
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // read all lines of dbUsernamesResource
        String[] usernames; // add 95 more
        try(InputStream inputStream = dbUsernamesResource.getInputStream()){
            usernames = StreamUtils
            .copyToString(inputStream, StandardCharsets.UTF_8)
            .split("\n");
        } catch (Exception e) {
            log.error("Error reading dbUsernamesResource: {}", e.getMessage());
            return "redirect:/crud";
        }

        // read file from resources folder
        String dbPassword;
        // read first line of dbPasswordResource
        try(InputStream inputStream = dbPasswordResource.getInputStream()){
            dbPassword = StreamUtils
            .copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error reading dbPasswordResource: {}", e.getMessage());
            return "redirect:/crud";
        }
        
        String hashedPassword = encoder.encode(dbPassword);
        
        for (String username : usernames) {
            User user = new User();
            user.setUserName(username);
            user.setHashedPassword(hashedPassword);
            user.setAwCrudsPerformed(0);
            user.setIsDeleted(false);
            userService.createUser(user);
        }
        
        return "redirect:/crud";
    }
}
