package com.aspiresys.fp_micro_userservice.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aspiresys.fp_micro_userservice.common.dto.AppResponse;
import com.aspiresys.fp_micro_userservice.aop.annotation.Auditable;
import com.aspiresys.fp_micro_userservice.aop.annotation.ExecutionTime;
import com.aspiresys.fp_micro_userservice.aop.annotation.ValidateParameters;

import lombok.extern.java.Log;

/**
 * UserController is a REST controller that handles user-related requests.
 * It provides endpoints for user management operations such as creating,
 * retrieving, updating, and deleting users.
 */
@RestController
@RequestMapping("/users")
@Log
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * Hello endpoint.
     * This endpoint returns a simple greeting message.
     * @param email
     * @return a greeting message
     */
    @GetMapping("/hello")
    public ResponseEntity<AppResponse<String>> hello(@RequestParam(required = false) String email) {
        return ResponseEntity.ok(new AppResponse<>("Hello, " + email + "!", null));
    } 

    /**
     * <pre>
     * <b>Get a user by their email.</b><br>
     * This endpoint retrieves a user based on their email address.<br>
     * - It validates the email format and returns a 400 Bad Request if the format is invalid.<br>
     * - If the user is found, it returns a 200 OK response with the user data.<br>
     * - If the user is not found, it returns a 404 Not Found response.<br>
     * <br>
     * The request would look like this:<br>
     * <code>GET /users/find?email=mail@example.com</code><br>
     * <br>
     * @param email the email of the user to retrieve<br>
     * @return the user with the specified email
     * </pre>
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')") // Ensure that only users with the 'USER' role can access this endpoint
    public ResponseEntity<AppResponse<User>> getUserByEmail(@RequestParam(required = false) String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            log.warning("Invalid or missing email format: " + email);
            return ResponseEntity.badRequest()
                .body(new AppResponse<>("Invalid or missing email format", null));
        }
        
        User user = userService.getUserByEmail(email);
        if (user == null) {
            log.warning("User not found for email: " + email);
            return ResponseEntity.status(404)
                .body(new AppResponse<>("User not found", null));
        }
        log.info("User found: " + user);
        return ResponseEntity.ok(new AppResponse<>("User found", user));
    }

    /**
     * Create a new user.
     * This endpoint allows the creation of a new user.
     * 
     * @param user the user to create
     * @return the created user
     */
    @PostMapping("/me/create")
    @PreAuthorize("hasRole('USER')") // Ensure that only users with the 'USER' role can access this endpoint
    @Auditable(operation = "CREATE_USER_ENDPOINT", entityType = "User", logParameters = true, logResult = true)
    @ExecutionTime(operation = "Create User API", warningThreshold = 1000, detailed = true)
    @ValidateParameters(notNull = true, message = "User data cannot be null")
    public ResponseEntity<AppResponse<User>> createUser(@RequestBody User user) {
        if (user == null || user.getEmail() == null || user.getFirstName() == null) {
            return ResponseEntity.badRequest()
                .body(new AppResponse<>("Invalid user data", null));
        }
        if (userService.getUserByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(409)
                .body(new AppResponse<>("User with this email already exists", null));
        }
        User createdUser = userService.saveUser(user);
        return ResponseEntity.ok(new AppResponse<>("User created successfully", createdUser));
    }

    /**
     * Update an existing user.
     * This endpoint allows updating an existing user's information.
     * 
     * @param user the user with updated information
     * @return the updated user
     */
    @PutMapping("/me/update")
    @PreAuthorize("hasRole('USER')") // Ensure that only users with the 'USER' role can access this endpoint
    @Auditable(operation = "UPDATE_USER_ENDPOINT", entityType = "User", logParameters = true, logResult = true)
    @ExecutionTime(operation = "Update User API", warningThreshold = 800, detailed = true)
    @ValidateParameters(notNull = true, message = "User data cannot be null")
    public ResponseEntity<AppResponse<User>> updateUser(@RequestBody User user) {
        if (user == null || user.getId() == null) {
            log.warning("Invalid user data for update: " + user);
            return ResponseEntity.badRequest()
                .body(new AppResponse<>("Invalid user data", null));
        }
        User updatedUser = userService.updateUser(user);
        if (updatedUser == null) {
            return ResponseEntity.status(404)
                .body(new AppResponse<>("User not found", null));
        }
        return ResponseEntity.ok(new AppResponse<>("User updated successfully", updatedUser));
    }

    /**
     * Delete a user by their ID.
     * This endpoint allows deleting a user based on their ID.
     * 
     * @param id the ID of the user to delete
     * @return a response indicating the result of the deletion
     */
    @DeleteMapping("/me/delete")
    @PreAuthorize("hasRole('USER')") // Ensure that only users with the 'USER' role can access this endpoint
    @Auditable(operation = "DELETE_USER_ENDPOINT", entityType = "User", logParameters = false, logResult = true)
    @ExecutionTime(operation = "Delete User API", warningThreshold = 600, detailed = true)
    public ResponseEntity<AppResponse<Boolean>> deleteUser(Authentication authentication) {
        String email = ((Jwt) authentication.getPrincipal()).getClaimAsString("email");
        if (email == null || email.isEmpty()) {
            log.warning("Email not found in authentication token");
            return ResponseEntity.badRequest()
                .body(new AppResponse<>("Email not found in authentication token", false));
        }
        User user = userService.getUserByEmail(email);
        if (user == null) {
            log.warning("User not found for email: " + email);
            return ResponseEntity.status(404)
                .body(new AppResponse<>("User not found", false));
        }
        boolean deleted = userService.deleteUserByEmail(user.getEmail());
        if (!deleted) {
            log.warning("Failed to delete user with ID: " + user.getId());
            return ResponseEntity.status(500)
                .body(new AppResponse<>("Failed to delete user", false));
        }
        log.info("User deleted successfully: " + user.getEmail());
        return ResponseEntity.ok(new AppResponse<>("User deleted successfully", true));
    }

    /**
     * Get current authenticated user information from JWT token.
     * This endpoint retrieves the current user information based on the JWT token.
     * 
     * @param authentication the authentication object containing JWT information
     * @return the current authenticated user information
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/me/current")
    public ResponseEntity<AppResponse<Object>> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            
            // Extraer información relevante del JWT
            String subject = jwt.getSubject(); // Normalmente contiene el username o user ID
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");
            var authorities = authentication.getAuthorities();
            
            // Crear respuesta con información del usuario
            var userInfo = new java.util.HashMap<String, Object>();
            userInfo.put("subject", subject);
            userInfo.put("email", email);
            userInfo.put("name", name);
            userInfo.put("authorities", authorities);
            userInfo.put("isAuthenticated", authentication.isAuthenticated());

            log.info("Current user information: " + userInfo);
            
            return ResponseEntity.ok(new AppResponse<>("Current user information", userInfo));
        }
        log.warning("User not authenticated");
        
        return ResponseEntity.status(401)
            .body(new AppResponse<>("User not authenticated", null));
    }
    /**
     * <pre>
     * <b>Get a user by their email - Internal endpoint for microservices validation.</b><br>
     * This endpoint is used by other microservices to validate user existence.<br>
     * It's a public endpoint for internal service-to-service communication.<br>
     * <br>
     * The request would look like this:<br>
     * <code>GET /users/find?email=mail@example.com</code><br>
     * <br>
     * @param email the email of the user to retrieve<br>
     * @return the user with the specified email
     * </pre>
     */
    @GetMapping("/find")
    public ResponseEntity<AppResponse<User>> getUserByEmailInternal(@RequestParam(required = false) String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            log.warning("Invalid or missing email format: " + email);
            return ResponseEntity.badRequest()
                .body(new AppResponse<>("Invalid or missing email format", null));
        }
        
        User user = userService.getUserByEmail(email);
        if (user == null) {
            log.warning("User not found for email: " + email);
            return ResponseEntity.status(404)
                .body(new AppResponse<>("User not found", null));
        }
        log.info("User found for internal validation: " + user.getEmail());
        return ResponseEntity.ok(new AppResponse<>("User found", user));
    }

}
