package com.crud_project.crud.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.crud_project.crud.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrudService {
    @Value("classpath:static/data/dbUsernames.txt")
    private Resource dbUsernamesResource;
    @Value("classpath:static/data/dbPassword.txt")
    private Resource dbPasswordResource;

    private final UserService userService;

    private String readResourceFile(Resource resource) throws Exception {
        try(InputStream inputStream = resource.getInputStream()){
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    public String createTestUsers(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try {
            String[] usernames = readResourceFile(dbUsernamesResource).split("\n");
            String password = readResourceFile(dbPasswordResource);
            
            String hashedPassword = encoder.encode(password);
            
            for (String username : usernames) {
                User user = new User();
                user.setUserName(username);
                user.setHashedPassword(hashedPassword);
                userService.createUser(user);
            }
            log.info("Created test users");
            
        } catch (Exception e) {
            log.error("Error creating test users: {}", e.getMessage());
        }

        return "redirect:/crud";
    }
}
