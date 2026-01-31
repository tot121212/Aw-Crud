package com.crud_project.crud.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.crud_project.crud.dvo.PageState;
import com.crud_project.crud.dvo.WheelSpinResult;
import com.crud_project.crud.entity.User;
import com.crud_project.crud.repository.UserProjection;
import com.crud_project.crud.repository.UserRepo;

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
    private final ResourceHandler resourceHandler;

    private final Random random = new Random();

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserByName(String username) {
        Optional<User> optionalUser = userRepo.findByUserName(username);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        log.warn("User with name: {} doesn't exist", username);
        return null;
    }

    public User getUserById(int id) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        log.warn("User with id: {} doesn't exist", id);
        return null;
    }

    // Assumes user does not have id because doesnt exist yet in db
    public User createUser(User user) {
        User savedUser = userRepo.save(user);
        return savedUser;
    }

    //Assume this user has been updated, just updating on db
    public User updateUser(User user) {
        Optional<User> existingUser = userRepo.findById(user.getId());
        if (existingUser.isPresent()) {
            return userRepo.save(user);
        }
        return null;
    }

    public boolean deleteUserById(int id) {
        userRepo.deleteById(id);
        if (getUserById(id) == null) {
            return true;
        }
        log.warn("User was not deleted");
        return false;
    }

    /**
     *
     * @param page
     * @param size
     * @return Page<UserProjection> || ?null
     */
    public Page<UserProjection> getUserProjectionsByPageState(PageState pageState) {
        if (pageState == null) {
            return null;
        }
        Page<UserProjection> projections = userRepo.findAllBy(PageRequest.of(pageState.getPage(), pageState.getSize()));
        if (projections.isEmpty()) {
            return null;
        }
        return projections;
    }

    /**
     *
     * @param username
     * @return boolean
     */
    public Boolean getExistsByUsername(String username) {
        Optional<Boolean> optionalExists = userRepo.existsByUserName(username);
        if (optionalExists.isPresent()) {
            return optionalExists.get();
        }
        return null;
    }

    /**
     *
     * @param username
     * @return UserProjection || null
     */
    public UserProjection getUserProjectionByName(String username) {
        Optional<UserProjection> optionalUserProjection = userRepo.findUserProjectionByUserName(username);
        if (optionalUserProjection.isPresent()) {
            return optionalUserProjection.get();
        }
        return null;
    }

    /**
     * @param page
     * @param size
     * @return Page of String || Empty Page of String
     */
    public Page<String> getUserNamesByPageState(PageState pageState) {
        if (pageState == null) {
            return Page.empty();
        }
        return userRepo.findAllUserNames(PageRequest.of(pageState.getPage(), pageState.getSize()));
    }

    /**
     * @param username
     * @return boolean || null
     */
    public Boolean getDeadByName(String username) {
        Optional<Boolean> optionalIsDead = userRepo.findDeadByUserName(username);
        if (optionalIsDead.isPresent()) {
            return optionalIsDead.get();
        }
        return null;
    }

    /**
     *
     * @return boolean
     */
    public boolean deleteAllUsers() {
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
     *
     * @param username
     * @param password
     * @return User or null
     */
    public User registerUser(String username, String password) {
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

    private static final int RNG_MIN_CRUDS = 0;
    private static final int RNG_MAX_CRUDS = 100;

    /**
     *
     * @return boolean
     */
    @Transactional
    public boolean createTestUsers() {
        try {
            List<String> usernames = resourceHandler.getTestUserDbUsernames();
            if (usernames == null) {
                return false;
            }
            log.warn("DEBUG: usernames: {}", usernames);
            Collections.shuffle(usernames);

            String password = resourceHandler.getTestUserDbPasswords().get(0);
            String hashedPassword = passwordEncoder.encode(password);

            for (String username : usernames) {

                User user = new User();
                user.setUserName(username);
                user.setHashedPassword(hashedPassword);
                user.setAwCrudsPerformed(random.nextInt(RNG_MIN_CRUDS, RNG_MAX_CRUDS));

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
    @Transactional
    public boolean deleteTestUsers() {
        try {
            List<String> usernames = resourceHandler.getTestUserDbUsernames();

            for (String username : usernames) {
                
                User user = getUserByName(username.trim());
                if (user == null) {
                    throw new Exception(String.format("User '%s' not found during deletion", username.trim()));
                }
                deleteUserById(user.getId());
            }
            log.info("Deleted test users");
            return true;

        } catch (Exception e) {
            log.error("Error deleting test users: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Spin the wheel and return the name of the user that was "dead" or null
     *
     * @param model
     * @param username
     * @param page
     * @param size
     * @return WheelSpinResult || null || throws Unchecked (which will be caught
     * by @Transactional)
     */
    @Transactional
    public WheelSpinResult spinWheel(String username, PageState pageState) {

        if (getDeadByName(username)) {
            return null;
        }

        User currentUser = getUserByName(username);
        if (currentUser == null) {
            return null;
        }

        Page<String> pageOfParticipants = getUserNamesByPageState(pageState);
        if (pageOfParticipants == null) {
            return null;
        }

        List<String> participants
                = pageOfParticipants
                        .stream()
                        .collect(Collectors.toList());
        if (participants.isEmpty()) {
            log.warn("No participants found");
            return null;
        }
        participants.remove(username);
        participants.add(0, username);
        participants = Collections.unmodifiableList(participants);
        // the participants list now predictably has the currentUser at the beginning

        String winnerName = participants.get(
                random.nextInt(participants.size()));

        User winnerUser = getUserByName(winnerName);
        if (winnerUser == null) {
            log.warn("Winner is not found");
            return null;
        }

        winnerUser.setDead(true);
        currentUser.setAwCrudsPerformed(currentUser.getAwCrudsPerformed() + 1);

        return new WheelSpinResult(winnerUser.getUserName(), participants);
    }
}
