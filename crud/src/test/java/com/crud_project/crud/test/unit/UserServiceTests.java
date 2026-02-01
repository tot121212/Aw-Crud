package com.crud_project.crud.test.unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import com.crud_project.crud.dvo.PageState;
import com.crud_project.crud.dvo.WheelSpinResult;
import com.crud_project.crud.entity.User;
import com.crud_project.crud.repository.UserProjection;
import com.crud_project.crud.repository.UserRepo;
import com.crud_project.crud.repository.impl.UserProjectionImpl;
import com.crud_project.crud.service.ResourceHandler;
import com.crud_project.crud.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ResourceHandler resourceHandler;

    @Mock
    private Random randomMock;

    // Base set of USERS for all tests
    private static final int USER_COUNT = 4; // ensure "USER_COUNT" <= "default PageState size"
    private static final int CURRENT_USER_IDX = 0;
    private List<User> USERS = new ArrayList<>();

    @BeforeEach
    public void beforeEach() throws Exception {
        USERS = new ArrayList<>();
        for (int i = 1; i <= USER_COUNT; i++) {
            User user = User.builder()
                    .id(i)
                    .userName("user" + Integer.toString(i))
                    .hashedPassword("hash")
                    .dead(false)
                    .awCrudsPerformed(0)
                    .build();
            USERS.add(user);
        }
    }

    @Nested
    public class GetAllUsersTests {

        @Test
        void testGetAllUsers_WithUsers() {
            when(userRepo.findAll()).thenReturn(USERS);

            List<User> result = userService.getAllUsers();

            assertNotNull(result);
            assertEquals(USER_COUNT, result.size());
            assertTrue(result.containsAll(USERS));
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
    public class GetUserByNameTests {

        @Test
        void testGetUserByName_UserExists() {
            User user = USERS.get(CURRENT_USER_IDX);
            String username = user.getUserName();

            when(userRepo.findByUserName(username)).thenReturn(Optional.of(user));

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
            verify(userRepo, never()).findByUserName(null);
        }
    }

    @Nested
    public class GetUserByIdTests {

        @Test
        void testGetUserById_UserExists() {
            User user = USERS.get(CURRENT_USER_IDX);

            when(userRepo.findById(user.getId())).thenReturn(Optional.of(USERS.get(CURRENT_USER_IDX)));

            User result = userService.getUserById(user.getId());

            assertNotNull(result);
            assertEquals(result.getId(), user.getId());
            assertEquals(result.getUserName(), user.getUserName());
            assertEquals(result.getHashedPassword(), user.getHashedPassword());
            verify(userRepo, times(1)).findById(user.getId());
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
    public class CreateUserTests {

        @Test
        void testCreateUser_Success() {
            User user = USERS.get(0);

            when(userRepo.save(user)).thenReturn(user);

            User result = userService.createUser(user);

            assertNotNull(result);
            assertEquals(user.getUserName(), result.getUserName());
            verify(userRepo, times(1)).save(user);
        }
    }

    @Nested
    public class UpdateUserTests {

        @Test
        void testUpdateUser_UserExists() {
            User user = USERS.get(CURRENT_USER_IDX);
            int oldAwCrudsPerformed = user.getAwCrudsPerformed();
            User updatedUser = User.builder()
                    .id(user.getId())
                    .userName(user.getUserName())
                    .hashedPassword(user.getHashedPassword())
                    .awCrudsPerformed(user.getAwCrudsPerformed() + 1)
                    .build();

            when(userRepo.existsById(user.getId())).thenReturn(true);
            when(userRepo.save(updatedUser)).thenReturn(updatedUser);

            User result = userService.updateUser(updatedUser);

            assertNotNull(result);
            assertEquals(oldAwCrudsPerformed + 1, result.getAwCrudsPerformed());
            verify(userRepo, times(1)).existsById(user.getId());
            verify(userRepo, times(1)).save(updatedUser);
        }

        @Test
        void testUpdateUser_UserDoesNotExist() {
            User user = USERS.get(CURRENT_USER_IDX);

            when(userRepo.existsById(user.getId())).thenReturn(false);

            User result = userService.updateUser(user);

            assertNull(result);
            verify(userRepo, times(1)).existsById(user.getId());
            verify(userRepo, never()).save(any(User.class));
        }
    }

    @Nested
    public class DeleteUserByIdTests {

        @Test
        void testDeleteUserById_Success() {
            int id = USERS.get(CURRENT_USER_IDX).getId();

            doNothing().when(userRepo).deleteById(id);
            when(userRepo.findById(id)).thenReturn(Optional.empty());

            boolean result = userService.deleteUserById(id);

            assertTrue(result);
            verify(userRepo, times(1)).deleteById(id);
            verify(userRepo, times(1)).findById(id);
        }

        @Test
        void testDeleteUserById_UserStillExists() {
            User user = USERS.get(CURRENT_USER_IDX);

            doNothing().when(userRepo).deleteById(user.getId());
            when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));

            boolean result = userService.deleteUserById(user.getId());

            assertFalse(result);
            verify(userRepo, times(1)).deleteById(user.getId());
            verify(userRepo, times(1)).findById(user.getId());
        }
    }

    @Nested
    public class GetUserProjectionsByPageStateTests {

        @Test
        void testGetUserProjectionsByPageState_Success() {
            PageState pageState = PageState.builder().build();
            Page<UserProjection> page = new PageImpl<>(
                    USERS.stream()
                            .map(UserProjectionImpl::from)
                            .map(p -> (UserProjection) p)
                            .toList());

            when(userRepo.findAllUserProjectionBy(PageRequest.of(pageState.getPage(), pageState.getSize())))
                    .thenReturn(page);

            Page<UserProjection> result = userService
                    .getUserProjectionsByPageState(pageState);

            assertNotNull(result);
            assertEquals(USERS.size(), result.getTotalElements());
            verify(userRepo, times(1)).findAllUserProjectionBy(any());
        }

        @Test
        void testGetUserProjectionsByPageState_NullPageState() {
            Page<UserProjection> result = userService.getUserProjectionsByPageState(null);

            assertNull(result);
            verify(userRepo, never()).findAllUserProjectionBy(any());
        }

        @Test
        void testGetUserProjectionsByPageState_EmptyPage() {
            PageState pageState = PageState.builder().build();
            Page<UserProjection> emptyPage = Page.empty();

            when(userRepo.findAllUserProjectionBy(PageRequest.of(pageState.getPage(), pageState.getSize())))
                    .thenReturn(emptyPage);

            Page<UserProjection> result = userService.getUserProjectionsByPageState(pageState);

            assertTrue(result.isEmpty());
            verify(userRepo, times(1)).findAllUserProjectionBy(any());
        }
    }

    @Nested
    public class GetExistsByUsernameTests {

        @Test
        void testGetExistsByUsername_Exists() {
            String username = USERS.get(CURRENT_USER_IDX).getUserName();

            when(userRepo.existsByUserName(username)).thenReturn(true);

            boolean result = userService.getExistsByUsername(username);

            assertTrue(result);
            verify(userRepo, times(1))
                    .existsByUserName(username);
        }

        @Test
        void testGetExistsByUsername_DoesNotExist() {
            String username = "nonexistent";

            when(userRepo.existsByUserName(username))
                    .thenReturn(false);

            boolean result = userService.getExistsByUsername(username);

            assertFalse(result);
            verify(userRepo, times(1))
                    .existsByUserName(username);
        }
    }

    @Nested
    public class GetUserProjectionByNameTests {

        @Test
        void testGetUserProjectionByName_UserExists() {
            User user = USERS.get(CURRENT_USER_IDX);
            String username = user.getUserName();
            UserProjectionImpl userProjectionImpl = UserProjectionImpl.from(user);

            when(userRepo.findUserProjectionByUserName(username))
                    .thenReturn(Optional.of(userProjectionImpl));

            UserProjection result = userService.getUserProjectionByName(username);

            assertNotNull(result);
            assertEquals(userProjectionImpl, result);
            verify(userRepo, times(1))
                    .findUserProjectionByUserName(username);
        }

        @Test
        void testGetUserProjectionByName_UserDoesNotExist() {
            String username = "nonexistent";

            when(userRepo.findUserProjectionByUserName(username)).thenReturn(Optional.empty());

            UserProjection result = userService.getUserProjectionByName(username);

            assertNull(result);
            verify(userRepo, times(1))
                    .findUserProjectionByUserName(username);
        }
    }

    @Nested
    public class GetUserNamesByPageStateTests {

        @Test
        void testGetUserNamesByPageState_Success() {
            PageState pageState = PageState.builder().build();
            List<String> usernames = USERS.stream().map(User::getUserName).toList();
            Page<String> page = new PageImpl<>(usernames);

            when(userRepo.findAllUserNames(
                    PageRequest.of(pageState.getPage(), pageState.getSize()))).thenReturn(page);

            Page<String> result = userService.getUserNamesByPageState(pageState);

            assertNotNull(result);
            assertEquals(page.getTotalElements(), result.getTotalElements());
            assertEquals(usernames, result.getContent());
            verify(userRepo, times(1))
                    .findAllUserNames(PageRequest.of(pageState.getPage(), pageState.getSize()));
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
    public class GetDeadByNameTests {

        @Test
        void testGetDeadByName_UserIsDead() {
            User user = USERS.get(CURRENT_USER_IDX);
            String username = user.getUserName();

            when(userRepo.findDeadByUserName(username)).thenReturn(Optional.of(true));

            Boolean result = userService.getDeadByName(username);

            assertNotNull(result);
            assertTrue(result);
            verify(userRepo, times(1)).findDeadByUserName(username);
        }

        @Test
        void testGetDeadByName_UserIsAlive() {
            User user = USERS.get(CURRENT_USER_IDX);
            String username = user.getUserName();

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
    public class DeleteAllUsersTests {

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
    public class RegisterUserTests {

        private final Integer id = 1;
        private final String username = "nameToRegister";
        private final String password = "password";
        private final String hashedPassword = "hashedPassword";

        @Test
        public void testRegisterUser_Success() {
            User savedUser = User.builder().id(id).userName(username).hashedPassword(hashedPassword).build();

            when(userRepo.findByUserName(username)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
            when(userRepo.save(any(User.class))).thenReturn(savedUser);

            User result = userService.registerUser(username, password);

            assertNotNull(result);
            assertEquals(savedUser, result);

            verify(userRepo, times(1)).findByUserName(username);
            verify(passwordEncoder, times(1)).encode(password);
            verify(userRepo, times(1)).save(any(User.class));
        }

        @Test
        public void testRegisterUser_UserAlreadyExists() {
            User existingUser = User.builder().id(id).userName(username).hashedPassword(hashedPassword).build();

            when(userRepo.findByUserName(username)).thenReturn(Optional.of(existingUser));

            User result = userService.registerUser(username, password);

            assertNull(result);

            verify(userRepo, times(1)).findByUserName(username);
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepo, never()).save(any(User.class));
        }
    }

    @Nested
    public class SpinWheelTests {

        private User currentUser;
        private PageState pageState;
        private Page<String> page;
        private List<String> usernames;

        @BeforeEach
        public void setupSpinWheel() throws Exception {
            currentUser = USERS.get(CURRENT_USER_IDX);

            pageState = PageState.builder().build();
            usernames = USERS.stream().map(User::getUserName).toList();
            page = new PageImpl<>(usernames);

            lenient().when(
                    userRepo.findAllUserNames(any(Pageable.class)))
                    .thenReturn(page);

            lenient().when(randomMock.nextInt(anyInt()))
                    .thenReturn(0);
        }

        @DisplayName("User spins wheel and lives")
        @Test
        public void testSpinWheel_Success() {
            int winnerIdx = CURRENT_USER_IDX + 1;
            User winnerUser = USERS.get(winnerIdx);
            int prevAwCrudsPerformed = currentUser.getAwCrudsPerformed();

            when(randomMock.nextInt(anyInt()))
                    .thenReturn(winnerIdx);
            when(userRepo.findByUserName(currentUser.getUserName())).thenReturn(Optional.of(currentUser));
            when(userRepo.findByUserName(winnerUser.getUserName())).thenReturn(Optional.of(winnerUser));

            WheelSpinResult result = userService.spinWheel(
                    currentUser.getUserName(), pageState);

            assertNotNull(result);
            assertTrue(result.getParticipants().containsAll(usernames));
            assertTrue(result.getParticipants().contains(currentUser.getUserName()));
            // expected user2 but was user1
            assertEquals(winnerUser.getUserName(), result.getWinnerName());
            assertEquals(prevAwCrudsPerformed + 1, currentUser.getAwCrudsPerformed());

            verify(userRepo, times(1)).findByUserName(currentUser.getUserName());
            verify(userRepo, times(1)).findAllUserNames(any(Pageable.class));
            verify(userRepo, times(1)).findByUserName(winnerUser.getUserName());
        }

        @DisplayName("Current user must not be dead to spin wheel")
        @Test
        public void testSpinWheel_UserIsDead() {
            User deadUser = USERS.get(CURRENT_USER_IDX);
            deadUser.setDead(true);
            when(userRepo.findByUserName(deadUser.getUserName())).thenReturn(Optional.of(deadUser));

            WheelSpinResult result = userService.spinWheel(deadUser.getUserName(), pageState);

            assertNull(result);

            verify(userRepo, times(1)).findByUserName(deadUser.getUserName());
            verify(userRepo, never()).findAllUserNames(any(Pageable.class));
        }

        @DisplayName("Current user must exist to spin wheel")
        @Test
        public void testSpinWheel_UserNotFound() {
            String missingUserName = "nonexistent";
            when(userRepo.findByUserName(missingUserName)).thenReturn(Optional.empty());

            WheelSpinResult result = userService.spinWheel(missingUserName, pageState);

            assertNull(result);

            verify(userRepo, times(1)).findByUserName(missingUserName);
            verify(userRepo, never()).findAllUserNames(any(Pageable.class));
        }

        @DisplayName("Page current user is trying to spin is empty")
        @Test
        public void testSpinWheel_NoUsernamesFound() {
            when(randomMock.nextInt(anyInt()))
                    .thenReturn(0);
            when(userRepo.findAllUserNames(any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Mock repository methods
            when(userRepo.findByUserName(currentUser.getUserName())).thenReturn(Optional.of(currentUser));

            WheelSpinResult result = userService.spinWheel(currentUser.getUserName(), pageState);

            assertEquals(currentUser.getUserName(), result.getWinnerName());
            assertEquals(
                    Collections.singletonList(currentUser.getUserName()),
                    result.getParticipants());
            // currentUser is winner because no other users to add to spin, haha
            verify(userRepo, times(1)).findByUserName(currentUser.getUserName());
            verify(userRepo, times(1)).findAllUserNames(any(Pageable.class));
        }
    }

    @Nested
    public class CreateTestUsersTests {

        private List<String> usernames;
        private String password;
        private String hashedPassword;

        @BeforeEach
        public void beforeEach() {
            usernames = USERS.stream().map(User::getUserName).toList();
            password = "password";
            hashedPassword = "hashedPassword";
        }

        @Test
        void testCreateTestUsers_Success() {
            when(resourceHandler.getTestUserDbUsernames()).thenReturn(usernames);
            when(resourceHandler.getTestUserDbPasswords())
                    .thenReturn(Collections.singletonList(password));

            when(passwordEncoder.encode(password)).thenReturn(hashedPassword);

            when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
                User newUser = invocation.getArgument(0);
                return USERS.get(usernames.indexOf(newUser.getUserName()));
            });

            Set<Integer> result = userService.createTestUsers();

            assertArrayEquals(USERS.stream().map(User::getId).toArray(), result.toArray());
            verify(resourceHandler, times(1)).getTestUserDbUsernames();
            verify(resourceHandler, times(1)).getTestUserDbPasswords();
            verify(passwordEncoder, times(1)).encode(password);
            verify(userRepo, times(USERS.size())).save(any(User.class));
        }

        @Test
        void testCreateTestUsers_Failure() {
            when(resourceHandler.getTestUserDbUsernames()).thenReturn(null);

            Set<Integer> result = userService.createTestUsers();

            assertNull(result);
            verify(resourceHandler, times(1)).getTestUserDbUsernames();
            verify(passwordEncoder, never()).encode(password);
            verify(userRepo, never()).save(any(User.class));
        }
    }

    @Nested
    public class DeleteTestUsersTests {

        private List<String> usernames;

        @BeforeEach
        public void beforeEach() {
            usernames = USERS.stream().map(User::getUserName).toList();
        }

        @Test
        void testDeleteTestUsers_DeleteAllTestUsers() throws Exception {
            when(resourceHandler.getTestUserDbUsernames()).thenReturn(usernames);

            for (int i = 0; i < usernames.size(); i++) {
                String username = usernames.get(i);
                User testUser = USERS.get(i);
                when(userRepo.findByUserName(username)).thenReturn(Optional.of(testUser));
                // when(userRepo.deleteById(testUser.getId())).thenReturn(true);
            }

            boolean result = userService.deleteTestUsers();

            assertTrue(result);
            verify(resourceHandler, times(1)).getTestUserDbUsernames();
            for (int i = 0; i < usernames.size(); i++) {
                verify(userRepo, times(1)).findByUserName(usernames.get(i));
                verify(userRepo, times(1)).deleteById(USERS.get(i).getId());
            }
        }

        @Test
        void testDeleteTestUsers_UsernamesFileNotFound() {
            when(resourceHandler.getTestUserDbUsernames()).thenReturn(null);

            boolean result = userService.deleteTestUsers();

            assertFalse(result);
            verify(resourceHandler, times(1)).getTestUserDbUsernames();
            verify(userRepo, never()).findByUserName(anyString());
            verify(userRepo, never()).deleteById(anyInt());
        }

        @Test
        void testDeleteTestUsers_OneOfXUsersNotFound() {
            when(resourceHandler.getTestUserDbUsernames()).thenReturn(usernames);

            // Mock users using existing USERS list
            for (int i = 0; i < usernames.size(); i++) {
                String username = usernames.get(i);
                User testUser = USERS.get(i); // Use existing user from USERS list
                when(userRepo.findByUserName(username)).thenReturn(Optional.of(testUser));
                // when(userRepo.deleteById(testUser.getId())).thenReturn(true);
            }
            // Override specific user to return null (not found)
            when(userRepo.findByUserName(usernames.get(0))).thenReturn(Optional.empty());

            boolean result = userService.deleteTestUsers();

            assertTrue(result);
            verify(resourceHandler, times(1)).getTestUserDbUsernames();
            for (int i = 0; i < usernames.size(); i++) {
                verify(userRepo, times(1)).findByUserName(usernames.get(i));
            }
            // Only verify deleteUserById for users that were found (not the first one)
            for (int i = 1; i < usernames.size(); i++) {
                verify(userRepo, times(1)).deleteById(USERS.get(i).getId());
            }
        }
    }
}