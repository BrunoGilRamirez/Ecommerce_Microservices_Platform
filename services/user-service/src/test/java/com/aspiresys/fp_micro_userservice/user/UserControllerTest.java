package com.aspiresys.fp_micro_userservice.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.aspiresys.fp_micro_userservice.common.dto.AppResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;





/**
 * Unit tests for the {@link UserController} class.
 * <p>
 * This test class uses Mockito to mock dependencies and verifies the behavior of the UserController endpoints,
 * including user creation, retrieval, update, and deletion, as well as input validation and error handling.
 * </p>
 *
 * <ul>
 *   <li>Tests the hello endpoint with and without email input.</li>
 *   <li>Tests retrieval of user body information.</li>
 *   <li>Tests user retrieval by email, including valid, invalid, null, and not found scenarios.</li>
 *   <li>Tests user creation, including success, invalid data, and duplicate email cases.</li>
 *   <li>Tests user update, including success, invalid data, and user not found cases.</li>
 *   <li>Tests user deletion, including success, invalid ID, and user not found cases.</li>
 * </ul>
 *
 * <p>
 * Dependencies are injected using Mockito annotations. Each test verifies the HTTP response status, message,
 * and data returned by the controller methods.
 * </p>
 */
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Authentication createMockAuthentication(String email) {
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("email")).thenReturn(email);
        return authentication;
    }

    @Test
    public void testHello_withEmail() {
        ResponseEntity<AppResponse<String>> response = userController.hello("test@example.com");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Hello, test@example.com!", response.getBody().getMessage());
    }

    @Test
    public void testHello_withoutEmail() {
        ResponseEntity<AppResponse<String>> response = userController.hello(null);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Hello, null!", response.getBody().getMessage());
    }


    @Test
    public void testGetUserByEmail_validEmail_userFound() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        ResponseEntity<AppResponse<User>> response = userController.getUserByEmail("test@example.com");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("User found", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());
    }

    @Test
    public void testGetUserByEmail_invalidEmail() {
        ResponseEntity<AppResponse<User>> response = userController.getUserByEmail("invalid-email");
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid or missing email format", response.getBody().getMessage());
    }

    @Test
    public void testGetUserByEmail_nullEmail() {
        ResponseEntity<AppResponse<User>> response = userController.getUserByEmail(null);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid or missing email format", response.getBody().getMessage());
    }

    @Test
    public void testGetUserByEmail_userNotFound() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(null);

        ResponseEntity<AppResponse<User>> response = userController.getUserByEmail("test@example.com");
        assertEquals(404, response.getStatusCode().value());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    public void testCreateUser_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        when(userService.getUserByEmail("test@example.com")).thenReturn(null);
        when(userService.saveUser(user)).thenReturn(user);

        ResponseEntity<AppResponse<User>> response = userController.createUser(user);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("User created successfully", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());
    }

    @Test
    public void testCreateUser_invalidUserData() {
        ResponseEntity<AppResponse<User>> response = userController.createUser(null);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid user data", response.getBody().getMessage());
    }

    @Test
    public void testCreateUser_existingEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        ResponseEntity<AppResponse<User>> response = userController.createUser(user);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("User with this email already exists", response.getBody().getMessage());
    }

    @Test
    public void testUpdateUser_success() {
        User user = new User();
        user.setId(1L);
        when(userService.updateUser(user)).thenReturn(user);

        ResponseEntity<AppResponse<User>> response = userController.updateUser(user);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("User updated successfully", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());
    }

    @Test
    public void testUpdateUser_invalidUserData() {
        ResponseEntity<AppResponse<User>> response = userController.updateUser(null);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid user data", response.getBody().getMessage());
    }

    @Test
    public void testUpdateUser_userNotFound() {
        User user = new User();
        user.setId(1L);
        when(userService.updateUser(user)).thenReturn(null);

        ResponseEntity<AppResponse<User>> response = userController.updateUser(user);
        assertEquals(404, response.getStatusCode().value());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    public void testDeleteUser_success() {
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        
        Authentication authentication = createMockAuthentication(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(userService.deleteUserByEmail(email)).thenReturn(true);

        ResponseEntity<AppResponse<Boolean>> response = userController.deleteUser(authentication);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("User deleted successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData());
    }

    @Test
    public void testDeleteUser_invalidAuthentication() {
        Authentication authentication = createMockAuthentication(null);

        ResponseEntity<AppResponse<Boolean>> response = userController.deleteUser(authentication);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email not found in authentication token", response.getBody().getMessage());
        assertFalse(response.getBody().getData());
    }

    @Test
    public void testDeleteUser_userNotFound() {
        String email = "test@example.com";
        Authentication authentication = createMockAuthentication(email);
        when(userService.getUserByEmail(email)).thenReturn(null);

        ResponseEntity<AppResponse<Boolean>> response = userController.deleteUser(authentication);
        assertEquals(404, response.getStatusCode().value());
        assertEquals("User not found", response.getBody().getMessage());
        assertFalse(response.getBody().getData());
    }
}