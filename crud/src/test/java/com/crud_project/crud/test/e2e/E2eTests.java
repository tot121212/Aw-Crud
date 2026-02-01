package com.crud_project.crud.test.e2e;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.crud_project.crud.entity.User;
import com.crud_project.crud.repository.UserProjection;
import com.crud_project.crud.repository.UserRepo;
import com.crud_project.crud.service.UserService;
import com.crud_project.crud.test.config.DatabaseTestcontainersConfiguration;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(DatabaseTestcontainersConfiguration.class)
public class E2eTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "TestPass123!";
    private static final String VALID_USERNAME = "validuser123";
    private static final String VALID_PASSWORD = "StrongPass123!";

    @Nested
    @DisplayName("Authentication Tests")
    public class AuthenticationTests {

        @Test
        @DisplayName("User registration with valid credentials should succeed")
        public void testUserRegistrationWithValidCredentials() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .param("username", VALID_USERNAME)
                    .param("password", VALID_PASSWORD)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login"));

            // Verify user was created in database
            User createdUser = userService.getUserByName(VALID_USERNAME);
            assertNotNull(createdUser);
            assertEquals(createdUser.getUserName(), VALID_USERNAME);
            assertFalse(createdUser.isDead());
            assertEquals(0, createdUser.getAwCrudsPerformed());
        }

        @Test
        @DisplayName("User registration with invalid username should fail")
        public void testUserRegistrationWithInvalidUsername() throws Exception {
            // Username too short
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .param("username", "ab")
                    .param("password", VALID_PASSWORD)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/register?error=true"));

            // Verify user was not created
            User user = userService.getUserByName("ab");
            assertNull(user);
        }

        @Test
        @DisplayName("User registration with invalid password should fail")
        public void testUserRegistrationWithInvalidPassword() throws Exception {
            // Password doesn't meet requirements
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .param("username", VALID_USERNAME)
                    .param("password", "weak")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/register?error=true"));

            // Verify user was not created
            User user = userService.getUserByName(VALID_USERNAME);
            assertNull(user);
        }

        @Test
        @DisplayName("User registration with existing username should fail")
        public void testUserRegistrationWithExistingUsername() throws Exception {
            // First registration should succeed
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .param("username", VALID_USERNAME)
                    .param("password", VALID_PASSWORD)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

            // Second registration with same username should fail
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .param("username", VALID_USERNAME)
                    .param("password", VALID_PASSWORD)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/register?error=true"));
        }

        @Test
        @DisplayName("User login with valid credentials should succeed")
        public void testUserLoginWithValidCredentials() throws Exception {
            // Register user first
            userService.registerUser(VALID_USERNAME, VALID_PASSWORD);

            MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .param("username", VALID_USERNAME)
                    .param("password", VALID_PASSWORD)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud"))
                    .andReturn();

            // Verify session contains user
            HttpSession session = loginResult.getRequest().getSession();
            assertNotNull(session);
        }

        @Test
        @DisplayName("User login with invalid credentials should fail")
        public void testUserLoginWithInvalidCredentials() throws Exception {
            // Register user first
            userService.registerUser(VALID_USERNAME, VALID_PASSWORD);

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .param("username", VALID_USERNAME)
                    .param("password", "WrongPassword123!")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login?error=true"));
        }

        @Test
        @DisplayName("CSRF token is required for registration")
        public void testCsrfTokenRequiredForRegistration() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .param("username", VALID_USERNAME)
                    .param("password", VALID_PASSWORD))
                    .andExpect(MockMvcResultMatchers.status().isForbidden());
        }

        @Test
        @DisplayName("CSRF token is required for login")
        public void testCsrfTokenRequiredForLogin() throws Exception {
            // Register user first
            userService.registerUser(VALID_USERNAME, VALID_PASSWORD);

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .param("username", VALID_USERNAME)
                    .param("password", VALID_PASSWORD))
                    .andExpect(MockMvcResultMatchers.status().isForbidden());
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    @WithMockUser(username = TEST_USERNAME)
    public class CrudOperationsTests {

        @BeforeEach
        public void setUpAuthenticatedUser() {
            // Create test user for authenticated operations
            userService.registerUser(TEST_USERNAME, TEST_PASSWORD);
        }

        @Test
        @DisplayName("Accessing CRUD page should succeed for authenticated user")
        public void testAccessCrudPageForAuthenticatedUser() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/crud"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.view().name("crud"));
        }

        @Test
        @DisplayName("Create test users should succeed")
        public void testCreateTestUsers() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/crud/create-test-users")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud"));

            // Verify test users were created
            List<UserProjection> users = userRepo.findAllUserProjectionBy(PageRequest.of(0, 10)).getContent();
            assertFalse(users.isEmpty());
        }

        @Test
        @DisplayName("Request page should set page state in session")
        public void testRequestPageSetsPageState() throws Exception {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/crud/requestPage")
                    .param("pageNumber", "1")
                    .param("pageSize", "20")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud#user-table"))
                    .andReturn();

            HttpSession session = result.getRequest().getSession();
            assertNotNull(session.getAttribute("currentUser_pageState"));
        }

        @Test
        @DisplayName("Wheel spin should increment awCrudsPerformed")
        public void testWheelSpinIncrementsAwCrudsPerformed() throws Exception {
            User userBefore = userService.getUserByName(TEST_USERNAME);
            assertNotNull(userBefore);
            int awCrudsBefore = userBefore.getAwCrudsPerformed();

            mockMvc.perform(MockMvcRequestBuilders.post("/crud/spinWheel")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud#user-wheel"));

            User userAfter = userService.getUserByName(TEST_USERNAME);
            assertNotNull(userAfter);
            assertTrue(userAfter.getAwCrudsPerformed() > awCrudsBefore);
        }

        @Test
        @DisplayName("Delete test users should succeed")
        public void testDeleteTestUsers() throws Exception {
            Set<Integer> userIds = userService.createTestUsers();

            Set<User> usersBefore = userRepo.findAllByIdIn(userIds);
            assertFalse(usersBefore.isEmpty());

            mockMvc.perform(MockMvcRequestBuilders.post("/crud/delete-test-users")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud"));

            Set<User> usersAfter = userRepo.findAllByIdIn(userIds);
            assertTrue(usersAfter.isEmpty());
        }

        @Test
        @DisplayName("Page state validation should work correctly")
        public void testPageStateValidation() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/crud/requestPage")
                    .param("pageNumber", "0")
                    .param("pageSize", "10")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud#user-table"));

            mockMvc.perform(MockMvcRequestBuilders.post("/crud/requestPage")
                    .param("pageNumber", "0")
                    .param("pageSize", "0")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers
                            .redirectedUrl("/crud?userTableError=true#request-page-form-container"));

            mockMvc.perform(MockMvcRequestBuilders.post("/crud/requestPage")
                    .param("pageNumber", "0")
                    .param("pageSize", "101")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers
                            .redirectedUrl("/crud?userTableError=true#request-page-form-container"));
        }
    }

    @Nested
    @DisplayName("Security Tests")
    public class SecurityTests {

        @Test
        @DisplayName("Unauthenticated access to CRUD should redirect to login")
        public void testUnauthenticatedAccessToCrudRedirectsToLogin() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/crud"))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login"));
        }

        @Test
        @DisplayName("Authenticated access to home should succeed")
        public void testAuthenticatedAccessToHome() throws Exception {
            // Register and login user
            userService.registerUser(VALID_USERNAME, VALID_PASSWORD);

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .param("username", VALID_USERNAME)
                    .param("password", VALID_PASSWORD)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud"));

            mockMvc.perform(MockMvcRequestBuilders.get("/home"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.view().name("home"));
        }

        @Test
        @DisplayName("CSRF protection should prevent unauthorized requests")
        public void testCsrfProtection() throws Exception {
            // Register user first
            userService.registerUser(VALID_USERNAME, VALID_PASSWORD);

            // Try to access CRUD without authentication
            mockMvc.perform(MockMvcRequestBuilders.get("/crud"))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login"));
        }

        @Test
        @DisplayName("Logout should invalidate session")
        public void testLogoutInvalidatesSession() throws Exception {
            // Register and login user
            userService.registerUser(VALID_USERNAME, VALID_PASSWORD);

            MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .param("username", VALID_USERNAME)
                    .param("password", VALID_PASSWORD)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andReturn();

            HttpSession session = loginResult.getRequest().getSession();
            assertNotNull(session);

            // Logout
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login?logout=true"));

            // Try to access CRUD after logout
            mockMvc.perform(MockMvcRequestBuilders.get("/crud"))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login"));
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("User Lifecycle Tests")
    public class UserLifecycleTests {

        @Test
        @DisplayName("Complete user lifecycle from registration to logout")
        public void testCompleteUserLifecycle() throws Exception {
            String username = "lifecycleuser";
            String password = "TestPass123!";

            // Register user
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                    .param("username", username)
                    .param("password", password)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login"));

            // Verify user was created
            User registeredUser = userService.getUserByName(username);
            assertNotNull(registeredUser);

            // Login user
            MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .param("username", username)
                    .param("password", password)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud"))
                    .andReturn();

            HttpSession session = loginResult.getRequest().getSession();
            assertNotNull(session);

            // Access CRUD operations
            mockMvc.perform(MockMvcRequestBuilders.get("/crud")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.view().name("crud"));

            // Create test users
            mockMvc.perform(MockMvcRequestBuilders.post("/crud/create-test-users")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud"));

            // Request page and spin wheel
            mockMvc.perform(MockMvcRequestBuilders.post("/crud/requestPage")
                    .param("pageNumber", "0")
                    .param("pageSize", "10")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud#user-table"));

            mockMvc.perform(MockMvcRequestBuilders.post("/crud/spinWheel")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/crud#user-wheel"));

            // Logout
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login?logout=true"));

            // Verify user still exists after logout
            User userAfterLogout = userService.getUserByName(username);
            assertNotNull(userAfterLogout);
            assertEquals(username, userAfterLogout.getUserName());
        }

        @Test
        @DisplayName("User can be deleted and recreated")
        public void testUserCanBeDeletedAndRecreated() throws Exception {
            String username = "deletetestuser";
            String password = "TestPass123!";

            // Register user
            userService.registerUser(username, password);
            User user = userService.getUserByName(username);
            assertNotNull(user);

            // Delete user
            userService.deleteUserById(user.getId());
            User deletedUser = userService.getUserByName(username);
            assertNull(deletedUser);

            // Recreate user
            userService.registerUser(username, password);
            User recreatedUser = userService.getUserByName(username);
            assertNotNull(recreatedUser);
            assertEquals(username, recreatedUser.getUserName());
        }

        @Test
        @DisplayName("Multiple users can coexist")
        public void testMultipleUsersCanCoexist() throws Exception {
            String user1 = "user1";
            String user2 = "user2";
            String password = "TestPass123!";

            // Register both users
            userService.registerUser(user1, password);
            userService.registerUser(user2, password);

            // Verify both exist
            User firstUser = userService.getUserByName(user1);
            User secondUser = userService.getUserByName(user2);

            assertNotNull(firstUser);
            assertNotNull(secondUser);
            assertEquals(user1, firstUser.getUserName());
            assertEquals(user2, secondUser.getUserName());
            assertNotEquals(firstUser.getId(), secondUser.getId());
        }
    }

    @Nested
    @DisplayName("Database State Tests")
    public class DatabaseStateTests {

        @Test
        @DisplayName("Database should be clean after each test due to @Transactional")
        public void testDatabaseIsCleanAfterEachTest() throws Exception {
            // This test verifies that the @Transactional annotation works correctly
            // by ensuring the database is rolled back after each test

            // Count users before
            List<UserProjection> usersBefore = userRepo
                    .findAllUserProjectionBy(PageRequest.of(0, 100)).getContent();
            int countBefore = usersBefore.size();

            // Create a test user
            userService.registerUser("tempuser", "TestPass123!");

            // Count users after
            List<UserProjection> usersAfter = userRepo
                    .findAllUserProjectionBy(PageRequest.of(0, 100))
                    .getContent();
            int countAfter = usersAfter.size();

            // Verify user was created
            assertTrue(countAfter > countBefore);

            // The user should be automatically rolled back when this test method completes
            // due to the @Transactional annotation on the test class
        }

        @Test
        @DisplayName("User projections should work correctly")
        public void testUserProjections() throws Exception {
            // Create test user
            userService.registerUser("projectiontest", "TestPass123!");

            // Get user projection
            Optional<UserProjection> optional = userRepo.findUserProjectionByUserName("projectiontest");
            UserProjection projection = optional.get();
            assertNotNull(projection);
            assertEquals("projectiontest", projection.getUserName());
            assertEquals(0, projection.getAwCrudsPerformed());
            assertFalse(projection.isDead());

            // Update user and verify projection reflects changes
            User user = userService.getUserByName("projectiontest");
            user.setAwCrudsPerformed(5);
            userService.updateUser(user);

            optional = userRepo.findUserProjectionByUserName("projectiontest");
            UserProjection updatedProjection = optional.get();
            assertNotNull(updatedProjection);
            assertEquals(5, updatedProjection.getAwCrudsPerformed());
        }
    }
}
