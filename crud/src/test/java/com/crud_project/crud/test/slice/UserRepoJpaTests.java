package com.crud_project.crud.test.slice;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.crud_project.crud.entity.User;
import com.crud_project.crud.repository.UserProjection;
import com.crud_project.crud.repository.UserRepo;
import com.crud_project.crud.test.config.DatabaseTestcontainersConfiguration;

@DataJpaTest
@Import(DatabaseTestcontainersConfiguration.class)
public class UserRepoJpaTests {

    @Autowired
    private UserRepo userRepo;

    private static final List<String> listOfUsernames
            = Collections.unmodifiableList(List.of("User1", "User2", "User3", "User4"));
    private static Map<String, User> userNameToUser;

    private void initUserNameToUser() {
        userNameToUser = new HashMap<>();
        for (String name : listOfUsernames) {
            userNameToUser.put(
                    name,
                    User.builder()
                            .userName(name)
                            .hashedPassword(name)
                            .build()
            );
        }
    }

    @BeforeEach
    public void beforeEach() {
        initUserNameToUser();
    }

    @Nested
    public class findByUserNameTests {

        @Test
        void findByUserName_foundUser_returnsUser() {
            User user1 = userNameToUser.get(listOfUsernames.get(0));
            user1 = userRepo.save(user1);

            // Find the user by username
            Optional<User> foundUser = userRepo.findByUserName(user1.getUserName());

            // Verify that the user is found
            assertTrue(foundUser.isPresent());
            assertEquals(user1, foundUser.get());
        }

        @Test
        void findByUserName_notFoundUser_returnsEmptyOptional() {
            // Find a non-existent user
            Optional<User> foundUser = userRepo.findByUserName("nonExistentUser");

            // Verify that the user is not found
            assertTrue(foundUser.isEmpty());
        }
    }

    @Nested
    public class findDeadByUserNameTests {

        @Test
        void findDeadByUserName_foundUser_returnsDeadStatus() {
            User user1 = userNameToUser.get(listOfUsernames.get(0));
            user1 = userRepo.save(user1);

            // Find the dead status of the user
            Optional<Boolean> deadStatus = userRepo.findDeadByUserName(user1.getUserName());

            // Verify that the dead status is correct
            assertTrue(deadStatus.isPresent());
            assertEquals(user1.isDead(), deadStatus.get());
        }
    }

    @Nested
    public class findUserProjectionByUserNameTests {

        @Test
        void findUserProjectionByUserName_foundUser_returnsUserProjection() {
            User user1 = userNameToUser.get(listOfUsernames.get(0));
            user1 = userRepo.save(user1);

            // Find the user projection by username
            Optional<UserProjection> userProjection = userRepo.findUserProjectionByUserName(user1.getUserName());

            // Verify that the user projection is found
            assertTrue(userProjection.isPresent());
            assertEquals(user1.getUserName(), userProjection.get().getUserName());
            assertEquals(user1.getAwCrudsPerformed(), userProjection.get().getAwCrudsPerformed());
            assertEquals(user1.isDead(), userProjection.get().isDead());
        }
    }

    @Nested
    public class paginationTests {

        @Test
        void findAllBy_pageOfUsers_returnsCorrectPage() {
            // Save all test users
            for (String name : listOfUsernames) {
                userRepo.save(userNameToUser.get(name));
            }

            // Find a page of users
            Page<UserProjection> users = userRepo.findAllBy(Pageable.ofSize(2));

            // Verify that the page is correct
            assertEquals(listOfUsernames.size(), users.getTotalElements());
            assertEquals(2, users.getTotalPages());
            assertEquals(2, users.getContent().size());
        }

        @Test
        void findAllUserNames_pageOfUserNames_returnsCorrectPage() {
            // Save all test users
            for (String name : listOfUsernames) {
                userRepo.save(userNameToUser.get(name));
            }

            // Find a page of user names
            Page<String> userNames = userRepo.findAllUserNames(Pageable.ofSize(2));

            // Verify that the page is correct
            assertEquals(listOfUsernames.size(), userNames.getTotalElements());
            assertEquals(2, userNames.getTotalPages());
            assertEquals(2, userNames.getContent().size());
        }
    }

    @Nested
    public class existsByUserNameTests {

        @Test
        void existsByUserName_existentUser_returnsTrue() {
            User user1 = userNameToUser.get(listOfUsernames.get(0));
            user1 = userRepo.save(user1);

            // Check if the user exists
            Optional<Boolean> exists = userRepo.existsByUserName(user1.getUserName());

            // Verify that the user exists
            assertTrue(exists.isPresent());
            assertTrue(exists.get());
        }

        @Test
        void existsByUserName_nonExistentUser_returnsFalse() {
            // Check if a non-existent user exists
            Optional<Boolean> exists = userRepo.existsByUserName("nonExistentUser");

            // Verify that the user does not exist
            assertTrue(exists.isPresent());
            assertFalse(exists.get());
        }
    }
}
