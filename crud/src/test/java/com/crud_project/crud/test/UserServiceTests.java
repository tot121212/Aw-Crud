package com.crud_project.crud.test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.crud_project.crud.entity.PageState;
import com.crud_project.crud.entity.User;
import com.crud_project.crud.entity.WheelSpinResult;
import com.crud_project.crud.repository.UserProjection;
import com.crud_project.crud.repository.UserRepo;
import com.crud_project.crud.service.ResourceHandler;
import com.crud_project.crud.service.UserService;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
class UserServiceTests {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserProjection userProjection;

    @Mock
    private ResourceHandler resourceHandler;

    private Random randomMock;

    // Base set of 4 users for all tests
    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private List<User> baseUsers;

    @BeforeEach
    void init() throws Exception {
        // Initialize base users
        user1 = User.builder()
            .id(1)
            .userName("user1")
            .hashedPassword("hash1")
            .dead(false)
            .awCrudsPerformed(0)
            .build();

        user2 = User.builder()
            .id(2)
            .userName("user2")
            .hashedPassword("hash2")
            .dead(false)
            .awCrudsPerformed(0)
            .build();

        user3 = User.builder()
            .id(3)
            .userName("user3")
            .hashedPassword("hash3")
            .dead(false)
            .awCrudsPerformed(0)
            .build();

        user4 = User.builder()
            .id(4)
            .userName("user4")
            .hashedPassword("hash4")
            .dead(false)
            .awCrudsPerformed(0)
            .build();

        baseUsers = Arrays.asList(user1, user2, user3, user4);

        randomMock = Mockito.mock(Random.class);
        // Always return 0 for nextInt
        lenient()
            .when(randomMock.nextInt(anyInt()))
            .thenAnswer(invocation -> {
                return ((int) invocation.getArgument(0) - 1);
            }
        );


        // Overwrite the private final field using reflection
        Field randomField = UserService.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(userService, randomMock);
    }

    @Nested
    class GetAllUsersTests {
        @Test
        void testGetAllUsers_WithUsers() {
            when(userRepo.findAll()).thenReturn(baseUsers);

            List<User> result = userService.getAllUsers();

            assertNotNull(result);
            assertEquals(4, result.size());
            assertEquals("user1", result.get(0).getUserName());
            verify(userRepo, times(1)).findAll();
        }

        @Test
        void testGetAllUsers_Empty() {
            when(userRepo.findAll()).thenReturn(Collections.emptyList());

            List<User> result = userService.getAllUsers();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepo, times(1)).findAll();
        }
    }

    @Nested
    class GetUserByNameTests {
        @Test
        void testGetUserByName_UserExists() {
            String username = user1.getUserName();

            when(userRepo.findByUserName(username)).thenReturn(Optional.of(user1));

            User result = userService.getUserByName(username);

            assertNotNull(result);
            assertEquals(username, result.getUserName());
            assertEquals(1, result.getId());
            verify(userRepo, times(1)).findByUserName(username);
        }

        @Test
        void testGetUserByName_UserDoesNotExist() {
            String username = "nonexistent";

            when(userRepo.findByUserName(username)).thenReturn(Optional.empty());

            User result = userService.getUserByName(username);

            assertNull(result);
            verify(userRepo, times(1)).findByUserName(username);
        }

        @Test
        void testGetUserByName_NullUsername() {
            User result = userService.getUserByName(null);

            assertNull(result);
            verify(userRepo, never()).findByUserName(anyString());
        }
    }

    @Nested
    class GetUserByIdTests {
        @Test
        void testGetUserById_UserExists() {
            int id = user1.getId();
            String username = user1.getUserName();
            String hashedPassword = user1.getHashedPassword();

            when(userRepo.findById(id)).thenReturn(Optional.of(user1));

            User result = userService.getUserById(id);

            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(username, result.getUserName());
            assertEquals(hashedPassword, result.getHashedPassword());
            verify(userRepo, times(1)).findById(id);
        }

        @Test
        void testGetUserById_UserDoesNotExist() {
            int id = 999;

            when(userRepo.findById(id)).thenReturn(Optional.empty());

            User result = userService.getUserById(id);

            assertNull(result);
            verify(userRepo, times(1)).findById(id);
        }

        @Test
        void testGetUserById_NegativeId() {
            int id = -1;

            when(userRepo.findById(id)).thenReturn(Optional.empty());

            User result = userService.getUserById(id);

            assertNull(result);
            verify(userRepo, times(1)).findById(id);
        }
    }

    @Nested
    class CreateUserTests {
        @Test
        void testCreateUser_Success() {
            User userToCreate = User.builder().userName(user1.getUserName()).hashedPassword(user1.getHashedPassword()).build();
            User savedUser = User.builder().id(1).userName(user1.getUserName()).hashedPassword(user1.getHashedPassword()).build();

            when(userRepo.save(userToCreate)).thenReturn(savedUser);

            User result = userService.createUser(userToCreate);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals(user1.getUserName(), result.getUserName());
            verify(userRepo, times(1)).save(userToCreate);
        }
    }

    @Nested
    class UpdateUserTests {
        @Test
        void testUpdateUser_UserExists() {
            User updatedUser = User.builder().id(user1.getId()).userName(user1.getUserName()).hashedPassword("newHash").build();

            when(userRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
            when(userRepo.save(updatedUser)).thenReturn(updatedUser);

            User result = userService.updateUser(updatedUser);

            assertNotNull(result);
            assertEquals("newHash", result.getHashedPassword());
            verify(userRepo, times(1)).findById(user1.getId());
            verify(userRepo, times(1)).save(updatedUser);
        }

        @Test
        void testUpdateUser_UserDoesNotExist() {
            User nonExistentUser = User.builder().id(999).userName("user").hashedPassword("hash").build();

            when(userRepo.findById(999)).thenReturn(Optional.empty());

            User result = userService.updateUser(nonExistentUser);

            assertNull(result);
            verify(userRepo, times(1)).findById(999);
            verify(userRepo, never()).save(any(User.class));
        }
    }

    @Nested
    class DeleteUserByIdTests {
        @Test
        void testDeleteUserById_Success() {
            int id = user1.getId();

            doNothing().when(userRepo).deleteById(id);
            when(userRepo.findById(id)).thenReturn(Optional.empty());

            userService.deleteUserById(id);

            verify(userRepo, times(1)).deleteById(id);
            verify(userRepo, times(1)).findById(id);
        }

        @Test
        void testDeleteUserById_UserStillExists() {
            int id = user1.getId();

            doNothing().when(userRepo).deleteById(id);
            when(userRepo.findById(id)).thenReturn(Optional.of(user1));

            userService.deleteUserById(id);

            verify(userRepo, times(1)).deleteById(id);
            verify(userRepo, times(1)).findById(id);
        }
    }

    @Nested
    class GetUserProjectionsByPageStateTests {
        @Test
        void testGetUserProjectionsByPageState_Success() {
            PageState pageState = PageState.builder().build();
            List<UserProjection> projections = Arrays.asList(
                userProjection
            );
            Page<UserProjection> page = new PageImpl<>(projections);

            when(userRepo.findAllBy(PageRequest.of(0, 10))).thenReturn(page);

            Page<UserProjection> result = userService.getUserProjectionsByPageState(pageState);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(userRepo, times(1)).findAllBy(PageRequest.of(0, 10));
        }

        @Test
        void testGetUserProjectionsByPageState_NullPageState() {
            Page<UserProjection> result = userService.getUserProjectionsByPageState(null);

            assertNull(result);
            verify(userRepo, never()).findAllBy(any());
        }

        @Test
        void testGetUserProjectionsByPageState_EmptyPage() {
            PageState pageState = PageState.builder().build();
            Page<UserProjection> emptyPage = Page.empty();

            when(userRepo.findAllBy(PageRequest.of(0, 10))).thenReturn(emptyPage);

            Page<UserProjection> result = userService.getUserProjectionsByPageState(pageState);

            assertNull(result);
            verify(userRepo, times(1)).findAllBy(PageRequest.of(0, 10));
        }
    }

    @Nested
    class GetExistsByUsernameTests {
        @Test
        void testGetExistsByUsername_Exists() {
            String username = user1.getUserName();

            when(userRepo.existsByUserName(username)).thenReturn(Optional.of((Boolean) true));

            boolean result = userService.getExistsByUsername(username);

            assertTrue(result);
            verify(userRepo, times(1)).existsByUserName(username);
        }

        @Test
        void testGetExistsByUsername_DoesNotExist() {
            String username = "nonexistent";

            when(userRepo.existsByUserName(username)).thenReturn(Optional.of((Boolean) false));

            boolean result = userService.getExistsByUsername(username);

            assertFalse(result);
            verify(userRepo, times(1)).existsByUserName(username);
        }
    }

    @Nested
    class GetUserProjectionByNameTests {
        @Test
        void testGetUserProjectionByName_UserExists() {
            String username = user1.getUserName();

            when(userRepo.findUserProjectionByUserName(username)).thenReturn(Optional.of(userProjection));

            UserProjection result = userService.getUserProjectionByName(username);

            assertNotNull(result);
            assertEquals(userProjection, result);
            verify(userRepo, times(1)).findUserProjectionByUserName(username);
        }

        @Test
        void testGetUserProjectionByName_UserDoesNotExist() {
            String username = "nonexistent";

            when(userRepo.findUserProjectionByUserName(username)).thenReturn(Optional.empty());

            UserProjection result = userService.getUserProjectionByName(username);

            assertNull(result);
            verify(userRepo, times(1)).findUserProjectionByUserName(username);
        }
    }

    @Nested
    class GetUserNamesByPageStateTests {
        @Test
        void testGetUserNamesByPageState_Success() {
            PageState pageState = PageState.builder().build();
            Page<String> page = new PageImpl<>(Arrays.asList(user1.getUserName(), user2.getUserName(), user3.getUserName(), user4.getUserName()));

            when(userRepo.findAllUserNames(PageRequest.of(0, 10))).thenReturn(page);

            Page<String> result = userService.getUserNamesByPageState(pageState);

            assertNotNull(result);
            assertEquals(4, result.getTotalElements());
            assertEquals("user1", result.getContent().get(0));
            verify(userRepo, times(1)).findAllUserNames(PageRequest.of(0, 10));
        }

        @Test
        void testGetUserNamesByPageState_NullPageState() {
            Page<String> result = userService.getUserNamesByPageState(null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepo, never()).findAllUserNames(any());
        }
    }

    @Nested
    class GetDeadByNameTests {
        @Test
        void testGetDeadByName_UserIsDead() {
            String username = user1.getUserName();

            when(userRepo.findDeadByUserName(username)).thenReturn(Optional.of(true));

            Boolean result = userService.getDeadByName(username);

            assertNotNull(result);
            assertTrue(result);
            verify(userRepo, times(1)).findDeadByUserName(username);
        }

        @Test
        void testGetDeadByName_UserIsAlive() {
            String username = user2.getUserName();

            when(userRepo.findDeadByUserName(username)).thenReturn(Optional.of(false));

            Boolean result = userService.getDeadByName(username);

            assertNotNull(result);
            assertFalse(result);
            verify(userRepo, times(1)).findDeadByUserName(username);
        }

        @Test
        void testGetDeadByName_UserDoesNotExist() {
            String username = "nonexistent";

            when(userRepo.findDeadByUserName(username)).thenReturn(Optional.empty());

            Boolean result = userService.getDeadByName(username);

            assertNull(result);
            verify(userRepo, times(1)).findDeadByUserName(username);
        }
    }

    @Nested
    class DeleteAllUsersTests {
        @Test
        void testDeleteAllUsers_Success() {
            doNothing().when(userRepo).deleteAll();

            boolean result = userService.deleteAllUsers();

            assertTrue(result);
            verify(userRepo, times(1)).deleteAll();
        }

        @Test
        void testDeleteAllUsers_Exception() {
            doThrow(new RuntimeException("Database error")).when(userRepo).deleteAll();

            boolean result = userService.deleteAllUsers();

            assertFalse(result);
            verify(userRepo, times(1)).deleteAll();
        }
    }

    @Nested
    class RegisterUserTests{
        private String username;
        private String password;
        private String hashedPassword;
        private User savedUser;

        @BeforeEach
        void setupRegisterUser() {
            username = user1.getUserName();
            password = "password123";
            hashedPassword = user1.getHashedPassword();
            savedUser = User.builder().id(1).userName(username).hashedPassword(hashedPassword).build();
            lenient().when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        }

        @Test
        void testRegisterUser_Success() {
            when(userRepo.findByUserName(username)).thenReturn(Optional.empty());
            when(userRepo.save(any(User.class))).thenReturn(savedUser);

            User result = userService.registerUser(username, password);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals(username, result.getUserName());
            assertEquals(hashedPassword, result.getHashedPassword());
            verify(userRepo, times(1)).findByUserName(username);
            verify(passwordEncoder, times(1)).encode(password);
            verify(userRepo, times(1)).save(any(User.class));
        }

        @Test
        void testRegisterUser_UserAlreadyExists() {
            when(userRepo.findByUserName(username)).thenReturn(Optional.of(user1));

            User result = userService.registerUser(username, password);

            assertNull(result);
            verify(userRepo, times(1)).findByUserName(username);
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepo, never()).save(any(User.class));
        }
    }

    @Nested
    class SpinWheelTests {

        private PageState pageState;
        private List<String> usernames;
        private Page<String> page;
        private Map<String, User> userMap;

        @BeforeEach
        void setupSpinWheel() throws Exception {
            pageState = PageState.builder().build();

            usernames = Arrays.asList(user2.getUserName(), user3.getUserName(), user4.getUserName());
            page = new PageImpl<>(usernames);

            userMap = Map.of(
                user1.getUserName(), user1,
                user2.getUserName(), user2,
                user3.getUserName(), user3,
                user4.getUserName(), user4
            );

            // Common stubs
            lenient().when(userRepo.findDeadByUserName(user1.getUserName())).thenReturn(Optional.of(false));
            lenient().when(userRepo.findAllUserNames(any(Pageable.class))).thenReturn(page);
            lenient().when(userRepo.findByUserName(anyString())).thenAnswer(invocation -> {
                String arg = invocation.getArgument(0);
                return Optional.ofNullable(userMap.get(arg));
            });
            lenient().when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }

        @Test
        void testSpinWheel_Success() {
            WheelSpinResult result = userService.spinWheel(user1.getUserName(), pageState);

            assertNotNull(result);
            assertEquals(user4.getUserName(), result.getWinnerName());
            assertTrue(result.getParticipants().containsAll(usernames));
            assertTrue(result.getParticipants().contains(user1.getUserName()));
            assertTrue(user4.isDead());
            assertEquals(1, user1.getAwCrudsPerformed());

            verify(userRepo, times(1)).findDeadByUserName(user1.getUserName());
            verify(userRepo, times(1)).findAllUserNames(any(Pageable.class));
            verify(userRepo, atLeast(2)).findByUserName(anyString());
        }

        @Test
        void testSpinWheel_UserIsDead() {
            String deadUser = "deadUser";
            when(userRepo.findDeadByUserName(deadUser)).thenReturn(Optional.of(true));

            WheelSpinResult result = userService.spinWheel(deadUser, pageState);

            assertNull(result);
            verify(userRepo, times(1)).findDeadByUserName(deadUser);
            verify(userRepo, never()).findByUserName(anyString());
        }

        @Test
        void testSpinWheel_UserNotFound() {
            String missingUser = "nonexistent";
            when(userRepo.findDeadByUserName(missingUser)).thenReturn(Optional.of(false));
            when(userRepo.findByUserName(missingUser)).thenReturn(Optional.empty());

            WheelSpinResult result = userService.spinWheel(missingUser, pageState);

            assertNull(result);
            verify(userRepo, times(1)).findDeadByUserName(missingUser);
            verify(userRepo, times(1)).findByUserName(missingUser);
        }

        @Test
        void testSpinWheel_NoUsernamesFound() {
            Page<String> emptyPage = Page.empty();
            when(userRepo.findByUserName(user1.getUserName())).thenReturn(Optional.of(user1));
            when(userRepo.findAllUserNames(PageRequest.of(0, 10))).thenReturn(emptyPage);

            WheelSpinResult result = userService.spinWheel(user1.getUserName(), pageState);

            assertNull(result);
            verify(userRepo, times(1)).findDeadByUserName(user1.getUserName());
            verify(userRepo, times(1)).findByUserName(user1.getUserName());
            verify(userRepo, times(1)).findAllUserNames(PageRequest.of(0, 10));
        }

        @Test
        void testSpinWheel_CurrentUserNotInParticipants() {
            Page<String> pageWithoutCurrent = new PageImpl<>(Arrays.asList(user2.getUserName(), user3.getUserName(), user4.getUserName()));
            when(userRepo.findByUserName(user1.getUserName())).thenReturn(Optional.of(user1));
            when(userRepo.findAllUserNames(PageRequest.of(0, 10))).thenReturn(pageWithoutCurrent);
            when(userRepo.findByUserName(user4.getUserName())).thenReturn(Optional.of(user4));

            WheelSpinResult result = userService.spinWheel(user1.getUserName(), pageState);

            assertNotNull(result);
            assertTrue(result.getParticipants().contains(user1.getUserName()));
            verify(userRepo, times(1)).findDeadByUserName(user1.getUserName());
            verify(userRepo, times(1)).findByUserName(user1.getUserName());
            verify(userRepo, times(1)).findAllUserNames(PageRequest.of(0, 10));
            verify(userRepo, times(1)).findByUserName(user4.getUserName());
        }

        @Test
        void testSpinWheel_WinnerNotFound() {
            Page<String> pageWithMissingWinner = new PageImpl<>(Arrays.asList(user2.getUserName(), user3.getUserName(), "nonexistentWinner"));
            when(userRepo.findByUserName(user1.getUserName())).thenReturn(Optional.of(user1));
            when(userRepo.findAllUserNames(PageRequest.of(0, 10))).thenReturn(pageWithMissingWinner);
            when(userRepo.findByUserName("nonexistentWinner")).thenReturn(Optional.empty());

            WheelSpinResult result = userService.spinWheel(user1.getUserName(), pageState);

            assertNull(result);
            verify(userRepo, times(1)).findDeadByUserName(user1.getUserName());
            verify(userRepo, times(1)).findByUserName(user1.getUserName());
            verify(userRepo, times(1)).findAllUserNames(PageRequest.of(0, 10));
            verify(userRepo, times(1)).findByUserName("nonexistentWinner");
        }
    }

    @Nested
    class CreateTestUsersTests {
        private List<String> testUsernames;
        private String testPassword;
        private String hashedPassword;

        @BeforeEach
        void setupCreateTestUsers() throws Exception {
            testUsernames = Arrays.asList("testuser1", "testuser2", "testuser3");
            testPassword = "testpassword123";
            hashedPassword = "hashedTestPassword";

            // Mock the resource handler
            lenient().when(resourceHandler.getTestUserDbUsernames()).thenReturn(testUsernames);
            lenient().when(resourceHandler.getTestUserDbPasswords()).thenReturn(Collections.singletonList(testPassword));

            // Mock password encoding
            lenient().when(passwordEncoder.encode(testPassword)).thenReturn(hashedPassword);
        }

        @Test
        void testCreateTestUsers_Success() {
            when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1); // Simulate ID assignment
                return user;
            });

            boolean result = userService.createTestUsers();

            assertTrue(result);
            verify(userRepo, times(3)).save(any(User.class));
            verify(passwordEncoder, times(1)).encode(testPassword);
        }

        @Test
        void testCreateTestUsers_Exception() {
            when(resourceHandler.getTestUserDbUsernames()).thenThrow(new RuntimeException("File not found"));

            boolean result = userService.createTestUsers();

            assertFalse(result);
            verify(userRepo, never()).save(any(User.class));
        }
    }

    @Nested
    class DeleteTestUsersTests {
        private List<String> testUsernames;

        @BeforeEach
        void setupDeleteTestUsers() throws Exception {
            testUsernames = Arrays.asList("testuser1", "testuser2", "testuser3");

            // Mock the resource handler
            when(resourceHandler.getTestUserDbUsernames()).thenReturn(testUsernames);

            // Mock users that exist
            lenient().when(userRepo.findByUserName("testuser1")).thenReturn(Optional.of(
                User.builder().id(101).userName("testuser1").hashedPassword("hash1").build()
            ));
            lenient().when(userRepo.findByUserName("testuser2")).thenReturn(Optional.of(
                User.builder().id(102).userName("testuser2").hashedPassword("hash2").build()
            ));
            lenient().when(userRepo.findByUserName("testuser3")).thenReturn(Optional.of(
                User.builder().id(103).userName("testuser3").hashedPassword("hash3").build()
            ));
        }

        @Test
        void testDeleteTestUsers_Success() throws Exception {
            boolean result = userService.deleteTestUsers();

            assertTrue(result);
            verify(userRepo, times(3)).findByUserName(anyString());
            verify(userRepo, times(3)).deleteById(anyInt());
        }

        @Test
        void testDeleteTestUsers_Exception() {
            when(resourceHandler.getTestUserDbUsernames()).thenThrow(new RuntimeException("File not found"));

            boolean result = userService.deleteTestUsers();

            assertFalse(result);
            verify(userRepo, never()).findByUserName(anyString());
        }

        @Test
        void testDeleteTestUsers_UserNotFound() {
            when(userRepo.findByUserName("testuser2")).thenReturn(Optional.empty());

            boolean result = userService.deleteTestUsers();

            assertFalse(result);
            verify(userRepo, times(2)).findByUserName(anyString());
        }
    }
}
