package com.crud_project.crud.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.crud_project.crud.entity.PageState;
import com.crud_project.crud.entity.User;
import com.crud_project.crud.entity.WheelSpinResult;
import com.crud_project.crud.repository.UserProjection;
import com.crud_project.crud.repository.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
// All actual db logic goes here
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final Random random = new Random();

    @Value("classpath:static/data/dbUsernames.txt")
    private Resource dbUsernamesResource;
    @Value("classpath:static/data/dbPassword.txt")
    private Resource dbPasswordResource;


    private String readResourceFile(Resource resource) throws Exception{
        try(InputStream inputStream = resource.getInputStream()){
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    private List<String> getResourceAsListOfStr(Resource resource){
        try{
            return Arrays.asList(readResourceFile(resource).split("\\r?\\n"));
        }
        catch (Exception e){
            log.error("Error reading dbUsernamesResource: {}", e.getMessage());
            return null;
        }
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserByName(String username) {
        Optional<User> optionalUser = userRepo.findByUserName(username);
        if (optionalUser.isPresent()){
            return optionalUser.get();
        }
        log.warn("User with name: {} doesn't exist", username);
        return null;
    }

    public User getUserById(Integer id) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isPresent()){
            return optionalUser.get();
        }
        log.warn("User with id: {} doesn't exist", id);
        return null;
    }

    // Assumes user does not have id because doesnt exist yet in db
    public User createUser (User user){
        User savedUser = userRepo.save(user);
        return savedUser;
    }

    //Assume this user has been updated, just updating on db
    public User updateUser (User user){
        Optional<User> existingUser = userRepo.findById(user.getId());
        if (existingUser.isPresent()){
            return userRepo.save(user);
        }
        return null;
    }

    public void deleteUserById (Integer id) {
        userRepo.deleteById(id);
        if (getUserById(id) == null){
            log.info("User with id: {} was dead", id);
        }
        else{
            log.warn("User with id: {} was not dead", id);
        }
    }

    /**
     * 
     * @param page
     * @param size
     * @return Page<UserProjection> || ?null
     */
    public Page<UserProjection> getUserProjectionsByPageState(PageState pageState) {
        if (pageState == null) return null;
        Page<UserProjection> projections = userRepo.findAllBy(PageRequest.of(pageState.getPage(), pageState.getSize()));
        if (projections.isEmpty()) return null;
        return projections;
    }

    /**
     * 
     * @param username
     * @return boolean
     */
    public Boolean getExistsByUsername(String username) {
        Optional<Boolean> optionalExists = userRepo.existsByUserName(username);
        if (optionalExists.isPresent()) return optionalExists.get();
        return null;
    }

    /**
     * 
     * @param username
     * @return UserProjection || null
     */
    public UserProjection getUserProjectionByName(String username) {
        Optional<UserProjection> optionalUserProjection = userRepo.findUserProjectionByUserName(username);
        if (optionalUserProjection.isPresent()){
            return optionalUserProjection.get();
        }
        return null;
    }

    /**
     * @param page
     * @param size
     * @return Page<String> || null
     */
    public Page<String> getUserNamesByPageState(PageState pageState) {
        if (pageState == null) return Page.empty();
        return userRepo.findAllUserNames(PageRequest.of(pageState.getPage(), pageState.getSize()));
    }

    /**
     * @param username
     * @return boolean || null
     */
    public Boolean getDeadByName(String username) {
        Optional<Boolean> optionalIsDead = userRepo.findDeadByUserName(username);
        if (optionalIsDead.isPresent()){
            return optionalIsDead.get();
        }
        return null;
    }

    /**
     * 
     * @return boolean
     */
    public boolean deleteAllUsers(){
        try {
            userRepo.deleteAll();
            log.info("Deleted all users");
            return true;

        } catch (Exception e) {
            log.error("Error deleting all users: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Registers a new user
     * @param username
     * @param password
     * @return User or null
     */
    public User registerUser(String username, String password){
        log.debug("Attempting to register user: {}", username);
        if (getUserByName(username) != null) {
            log.warn("User {} already exists", username);
            return null;
        }
        // Hash password and save user
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUserName(username);
        user.setHashedPassword(hashedPassword);
        User createdUser = createUser(user);
        log.info("User created: {}", createdUser);
        return createdUser;
    }

    /**
     * Logs user out from session
     * @param request
     */
    public void logout(HttpServletRequest request){
        log.info("Logging out user: {}", request.getRemoteUser());
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
    }

    /**
     * 
     * @return boolean
     */
    @Transactional
    public boolean createTestUsers(){
        try {
            List<String> usernames = getResourceAsListOfStr(dbUsernamesResource);
            log.warn("DEBUG: usernames: {}", usernames);
            Collections.shuffle(usernames);

            String password = readResourceFile(dbPasswordResource);
            String hashedPassword = passwordEncoder.encode(password);
            
            for (String username : usernames) {
                User user = new User();
                user.setUserName(username);
                user.setHashedPassword(hashedPassword);
                user.setAwCrudsPerformed(random.nextInt(0, 100));
                createUser(user);
            }
            log.info("Created test users");
            return true;
            
        } catch (Exception e) {
            log.error("Error creating test users: {}", e.getMessage());
            return false;
        }
    }

    /**
     * @return boolean
     */
    public boolean deleteTestUsers(){
        try {
            List<String> usernames = getResourceAsListOfStr(dbUsernamesResource);
            
            for (String username : usernames) {
                User user = getUserByName(username.trim());
                if (user != null) {
                    deleteUserById(user.getId());
                }
                else {
                    throw new Exception(String.format("User '%s' not found during deletion", username.trim()));
                }
            }
            log.info("Deleted test users");
            return true;

        } catch (Exception e) {
            log.error("Error deleting test users: {}", e.getMessage());
            return false;
        }
    }
    
    @Transactional
    public User spinWheelTransaction(User winnerUser, User currentUser){
        winnerUser.setDead(true);
        updateUser(winnerUser); // update user on db

        currentUser.setAwCrudsPerformed(currentUser.getAwCrudsPerformed() + 1);
        updateUser(currentUser);

        return winnerUser;
    }
    
    /**
     *  Spin the wheel and return the name of the user that was "dead" or null
     * @param model
     * @param username
     * @param page
     * @param size
     * @return WheelSpinResult || null || throws Unchecked (which will be caught by @Transactional)
     */
    public WheelSpinResult spinWheel(String username, Integer page, Integer size) {
        try {
            if (getDeadByName(username)){
                throw new Exception(String.format("User '%s' is dead", username));
            }
            User currentUser = getUserByName(username);
            if (currentUser == null){
                throw new Exception(String.format("User '%s' not found", username));
            }

            List<String> participants = 
                getUserNamesByPageAndSize(page, size)
                .stream()
                .collect(Collectors.toList());
            if (participants.isEmpty()){
                throw new Exception("No usernames found");
            }
            if (!participants.contains(username)){
                participants.add(username);
            }

            String winnerName = participants.get(
                random.nextInt(participants.size()));
            
            User winnerUser = getUserByName(winnerName);
            if (winnerUser == null){
                throw new Exception(String.format("User '%s' not found", winnerName));
            }
            
            winnerUser = spinWheelTransaction(winnerUser, currentUser);

            return WheelSpinResult
                .builder()
                .winnerName(winnerUser.getUserName())
                .participants(participants)
                .build();
        } catch (Exception e) {
            log.warn(e.getMessage());
            //e.printStackTrace();
            return null;
        }
    }
}