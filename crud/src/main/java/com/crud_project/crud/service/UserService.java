package com.crud_project.crud.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.crud_project.crud.entity.User;
import com.crud_project.crud.repository.UserProjection;
import com.crud_project.crud.repository.UserRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
// All actual db logic goes here
public class UserService {
    private final UserRepo userRepo;
    private final Random random = new Random();

    @Value("classpath:static/data/dbUsernames.txt")
    private Resource dbUsernamesResource;
    @Value("classpath:static/data/dbPassword.txt")
    private Resource dbPasswordResource;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserByName(String username) {
        Optional<User> optionalUser = userRepo.findByUserName(username);
        if (optionalUser.isPresent()){
            return optionalUser.get();
        }
        log.info("User with name: {} doesn't exist, failure", username);
        return null;
    }

    public User getUserById(Integer id) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isPresent()){
            return optionalUser.get();
        }
        log.info("User with id: {} doesn't exist, failure", id);
        return null;
    }

    // Assume user is provided already made
    public User createUser (User user){
        User savedUser = userRepo.save(user);
        log.info("User with id: {} saved, success", user.getId());
        return savedUser;
    }

    //Assume this user has been updated, just updating on db
    public User updateUser (User user){
        //Optional<User> existingUser = userRepo.findById(user.getId());
        User updatedUser = userRepo.save(user);
        return updatedUser;
    }

    public void deleteUserById (Integer id) {
        userRepo.deleteById(id);
        if (getUserById(id) == null){
            log.info("User with id: {} was deleted, success", id);
        }
        else{
            log.info("User with id: {} was not deleted, failure", id);
        }
    }

    private String readResourceFile(Resource resource) throws Exception {
        try(InputStream inputStream = resource.getInputStream()){
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    public Boolean createTestUsers(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try {
            String[] usernames = readResourceFile(dbUsernamesResource).split("\n");
            String password = readResourceFile(dbPasswordResource);
            
            String hashedPassword = encoder.encode(password);
            
            for (String username : usernames) {
                User user = new User();
                user.setUserName(username);
                user.setHashedPassword(hashedPassword);
                user.setAwCrudsPerformed(random.nextInt(0, 100));
                createUser(user);
            }
            log.info("Created test users");
            
        } catch (Exception e) {
            log.error("Error creating test users: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public Boolean deleteTestUsers(){
        try {
            String[] usernames = readResourceFile(dbUsernamesResource).split("\n");
            
            for (String username : usernames) {
                User user = getUserByName(username.trim());
                if (user != null) {
                    deleteUserById(user.getId());
                }
            }
            log.info("Deleted test users");
            return true;

        } catch (Exception e) {
            log.error("Error deleting test users: {}", e.getMessage());
            return false;
        }
    }

    public Boolean deleteAllUsers(){
        try {
            userRepo.deleteAll();
            log.info("Deleted all users");
            return true;

        } catch (Exception e) {
            log.error("Error deleting all users: {}", e.getMessage());
            return false;
        }
    }

    public Page<UserProjection> getUserProjectionsByPageAndSize(Integer page, Integer size) {
        if (page == null || 
            size == null || 
            page < 0 || 
            size < 1) {
            return Page.empty();
        }
        if (size > 100) {
            size = 100;
        }
        return userRepo.findAllProjectionsByPage(PageRequest.of(page, size));
    }

    public UserProjection getUserProjectionByName(String username) {
        Optional<UserProjection> optionalUserProjection = userRepo.findUserProjectionByUserName(username);
        if (optionalUserProjection.isPresent()){
            log.info("User with name: {} exists, success", username);
            return optionalUserProjection.get();
        }
        log.info("User with name: {} doesn't exist, failure", username);
        return null;
    }
}