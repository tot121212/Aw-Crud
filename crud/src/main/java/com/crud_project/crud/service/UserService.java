package com.crud_project.crud.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
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
    private final Random random;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserByName(String username) {
        if (username == null) {
            return null;
        }

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
        if (user == null ||
                user.getUserName() == null ||
                userRepo.existsByUserName(user.getUserName())) {
            return null;
        }
        User savedUser = userRepo.save(user);
        return savedUser;
    }

    // Assume this user has been updated, just updating on db
    public User updateUser(User user) {
        if (user == null || userRepo.existsById(user.getId()) == false) {
            return null;
        }
        return userRepo.save(user);
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
     * @return Page<UserProjection> || Empty Page
     */
    public Page<UserProjection> getUserProjectionsByPageState(PageState pageState) {
        if (pageState == null) {
            return null;
        }
        Page<UserProjection> projections = userRepo
                .findAllUserProjectionBy(PageRequest.of(pageState.getPage(), pageState.getSize()));
        return projections;
    }

    /**
     *
     * @param username
     * @return boolean
     */
    public boolean getExistsByUsername(String username) {
        return userRepo.existsByUserName(username);
    }

    /**
     *
     * @param username
     * @return UserProjectionImpl || null
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
     * @return a Set of the id's associated with the created users
     */
    @Transactional
    public Set<Integer> createTestUsers() {
        List<String> unmodifiableUsernames = resourceHandler.getTestUserDbUsernames();
        if (unmodifiableUsernames == null) {
            return null;
        }

        List<String> usernames = new ArrayList<>(unmodifiableUsernames); // make list mutable
        if (usernames.isEmpty()) {
            return null;
        }
        Collections.shuffle(usernames);

        String password = resourceHandler.getTestUserDbPasswords().get(0);
        String hashedPassword = passwordEncoder.encode(password);

        Set<Integer> createdUsers = new HashSet<>();
        for (String username : usernames) {

            User user = new User();
            user.setUserName(username);
            user.setHashedPassword(hashedPassword);
            user.setAwCrudsPerformed(random.nextInt(RNG_MIN_CRUDS, RNG_MAX_CRUDS));

            createdUsers.add(createUser(user).getId());
        }

        log.info("Created test users");
        return createdUsers;
    }

    /**
     * @return boolean
     */
    @Transactional
    public boolean deleteTestUsers() {
        List<String> usernames = resourceHandler.getTestUserDbUsernames();
        if (usernames == null) {
            log.warn("Test usernames file not found");
            return false;
        }

        if (usernames.isEmpty()) {
            log.warn("Test usernames file has no usernames");
            return false;
        }

        for (String username : usernames) {

            User user = getUserByName(username.trim());
            if (user == null) {
                log.warn("User not found in testUserDbUsernames");
                continue;
            }
            deleteUserById(user.getId());
        }
        log.info("Deleted test users");
        return true;
    }

    /**
     * The index at which the currentUser should be placed within the
     * participants list
     */
    private static final int CURRENT_USER_IDX = 0;

    /**
     * Creates the list of participants from current user's name and page of
     * users. Ensures that currentUserName is at the specified CURRENT_USER_IDX
     * location in said list
     *
     * @param userName
     * @param page
     * @return
     */
    public List<String> createWheelParticipantsList(String currentUserName, Page<String> page) {
        List<String> participants = page
                .stream()
                .collect(Collectors.toList());
        participants.remove(currentUserName);
        participants.add(CURRENT_USER_IDX, currentUserName);
        participants = Collections.unmodifiableList(participants);
        return participants;
    }

    public String getRandomWheelWinner(List<String> participants) {
        return participants.get(
                random.nextInt(participants.size()));
    }

    /**
     * Spin the wheel and return the name of the user that was "dead" or null
     *
     * @param model
     * @param username
     * @param page
     * @param size
     * @return WheelSpinResult || null || throws Unchecked (which will be caught
     *         by @Transactional)
     */
    @Transactional
    public WheelSpinResult spinWheel(String username, PageState pageState) {
        if (username == null || pageState == null) {
            log.warn("username and pageState must exist");
            return null;
        }

        User currentUser = getUserByName(username);
        if (currentUser == null) {
            log.warn("user doesn't exist");
            return null;
        }
        if (currentUser.isDead()) {
            log.warn("user cannot spin the wheel when dead");
            return null;
        }

        Page<String> pageOfParticipants = getUserNamesByPageState(pageState);
        List<String> participants = createWheelParticipantsList(currentUser.getUserName(), pageOfParticipants);
        String winnerName = getRandomWheelWinner(participants);
        User winnerUser;
        if (winnerName.compareTo(currentUser.getUserName()) == 0) {
            winnerUser = currentUser;
        } else {
            winnerUser = getUserByName(winnerName);
        }

        winnerUser.setDead(true);
        currentUser.setAwCrudsPerformed(currentUser.getAwCrudsPerformed() + 1);

        return new WheelSpinResult(winnerUser.getUserName(), participants);
    }
}
