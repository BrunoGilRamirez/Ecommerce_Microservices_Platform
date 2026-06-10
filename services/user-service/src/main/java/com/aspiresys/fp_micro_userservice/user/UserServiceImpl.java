package com.aspiresys.fp_micro_userservice.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aspiresys.fp_micro_userservice.aop.annotation.Auditable;
import com.aspiresys.fp_micro_userservice.aop.annotation.ExecutionTime;
import com.aspiresys.fp_micro_userservice.aop.annotation.ValidateParameters;
import com.aspiresys.fp_micro_userservice.kafka.producer.UserProducerService;
import lombok.extern.java.Log;

/**
 * Service implementation for managing User entities.
 * Provides methods for saving, retrieving, updating, and deleting users,
 * as well as checking for user existence by email.
 *
 * <p>This class interacts with the {@link UserRepository} to perform CRUD operations
 * on {@link User} objects.</p>
 *
 * <ul>
 *   <li>{@code saveUser(User user)} - Saves a new user or updates an existing user.</li>
 *   <li>{@code getUserByEmail(String email)} - Retrieves a user by their email address.</li>
 *   <li>{@code getUserById(Long id)} - Retrieves a user by their unique ID.</li>
 *   <li>{@code getAllUsers()} - Retrieves all users.</li>
 *   <li>{@code deleteUserById(Long id)} - Deletes a user by their ID.</li>
 *   <li>{@code deleteUserByEmail(String email)} - Deletes a user by their email address.</li>
 *   <li>{@code updateUser(User user)} - Updates an existing user.</li>
 *   <li>{@code userExistsByEmail(String email)} - Checks if a user exists by email.</li>
 * </ul>
 *
 * @author bruno.gil
 * @see UserService
 * @see UserRepository
 */
@Service
@Log
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    
    @Autowired
    private UserProducerService userProducerService;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Auditable(operation = "SAVE_USER", entityType = "User", logParameters = true, logResult = false)
    @ExecutionTime(operation = "Save User", warningThreshold = 500, detailed = true)
    @ValidateParameters(notNull = true, message = "User cannot be null")
    public User saveUser(User user) {
        boolean isNewUser = user.getId() == null;
        User savedUser = userRepository.save(user);
        
        // Send Kafka message based on whether it's a new user or update
        try {
            if (isNewUser) {
                log.info("KAFKA USER: Sending USER_CREATED event for user: " + savedUser.getEmail());
                userProducerService.sendUserCreated(savedUser);
            } else {
                log.info("KAFKA USER: Sending USER_UPDATED event for user: " + savedUser.getEmail());
                userProducerService.sendUserUpdated(savedUser);
            }
        } catch (Exception e) {
            log.warning("Failed to send Kafka message for user " + savedUser.getEmail() + ": " + e.getMessage());
        }
        
        return savedUser;
    }

    @Override
    @ExecutionTime(operation = "Get User By Email", warningThreshold = 300)
    @ValidateParameters(notNull = true, validateEmail = true, message = "Email cannot be null and must be valid")
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    @ExecutionTime(operation = "Get User By ID", warningThreshold = 200)
    @ValidateParameters(notNull = true, message = "User ID cannot be null")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @ExecutionTime(operation = "Get All Users", warningThreshold = 1000)
    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    @Override
    @Auditable(operation = "DELETE_USER_BY_ID", entityType = "User", logParameters = true, logResult = true)
    @ExecutionTime(operation = "Delete User By ID", warningThreshold = 400)
    @ValidateParameters(notNull = true, message = "User ID cannot be null")
    public boolean deleteUserById(Long id) {
        // Get user before deletion for Kafka message
        User userToDelete = getUserById(id);
        
        userRepository.deleteById(id);
        boolean deleted = !userRepository.existsById(id);
        
        // Send Kafka message if deletion was successful
        if (deleted && userToDelete != null) {
            try {
                log.info("KAFKA USER: Sending USER_DELETED event for user: " + userToDelete.getEmail());
                userProducerService.sendUserDeleted(userToDelete);
            } catch (Exception e) {
                log.warning("Failed to send Kafka delete message for user " + userToDelete.getEmail() + ": " + e.getMessage());
            }
        }
        
        return deleted;
    }
    @Override
    @Auditable(operation = "DELETE_USER_BY_EMAIL", entityType = "User", logParameters = true, logResult = true)
    @ExecutionTime(operation = "Delete User By Email", warningThreshold = 600)
    @ValidateParameters(notNull = true, validateEmail = true, message = "Email cannot be null and must be valid")
    public boolean deleteUserByEmail(String email) {
        User userToDelete = getUserByEmail(email);
        if (userToDelete != null) {
            userRepository.delete(userToDelete);
            
            // Send Kafka message if deletion was successful
            try {
                log.info("KAFKA USER: Sending USER_DELETED event for user: " + userToDelete.getEmail());
                userProducerService.sendUserDeleted(userToDelete);
            } catch (Exception e) {
                log.warning("Failed to send Kafka delete message for user " + userToDelete.getEmail() + ": " + e.getMessage());
            }
        }
        return !userRepository.findByEmail(email).isPresent();
    }
    @Override
    @Auditable(operation = "UPDATE_USER", entityType = "User", logParameters = true, logResult = false)
    @ExecutionTime(operation = "Update User", warningThreshold = 500, detailed = true)
    @ValidateParameters(notNull = true, message = "User cannot be null")
    public User updateUser(User user) {
        if (user.getId() == null || !userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User does not exist");
        }
        
        User updatedUser = userRepository.save(user);
        
        // Send Kafka message for user update
        try {
            log.info("KAFKA USER: Sending USER_UPDATED event for user: " + updatedUser.getEmail());
            userProducerService.sendUserUpdated(updatedUser);
        } catch (Exception e) {
            log.warning("Failed to send Kafka update message for user " + updatedUser.getEmail() + ": " + e.getMessage());
        }
        
        return updatedUser;
    }
    @Override
    @ExecutionTime(operation = "Check User Exists By Email", warningThreshold = 200)
    @ValidateParameters(notNull = true, validateEmail = true, message = "Email cannot be null and must be valid")
    public boolean userExistsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
}
