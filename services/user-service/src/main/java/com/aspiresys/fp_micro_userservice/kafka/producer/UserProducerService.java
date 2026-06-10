package com.aspiresys.fp_micro_userservice.kafka.producer;

import com.aspiresys.fp_micro_userservice.kafka.dto.UserMessage;
import com.aspiresys.fp_micro_userservice.user.User;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Producer service for sending user messages to Kafka topics.
 * Handles user synchronization events between User Service and Order Service.
 * 
 * @author bruno.gil
 */
@Service
@Log
public class UserProducerService {

    @Autowired
    private KafkaTemplate<String, UserMessage> userKafkaTemplate;

    @Value("${kafka.topic.user:user}")
    private String userTopic;

    /**
     * Sends a single user message to Kafka topic.
     * 
     * @param user User entity to send
     * @param eventType Type of event (USER_CREATED, USER_UPDATED, USER_DELETED)
     */
    public void sendUserMessage(User user, String eventType) {
        try {
            UserMessage userMessage = UserMessage.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .address(user.getAddress())
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("KAFKA USER: Sending " + eventType + " message for user: " + user.getEmail());
            
            CompletableFuture<SendResult<String, UserMessage>> future = 
                userKafkaTemplate.send(userTopic, user.getEmail(), userMessage);
                
            future.thenAccept(result -> {
                log.info("User message sent successfully: " + eventType + " for user " + user.getEmail() + 
                        " to topic " + userTopic + " at offset " + result.getRecordMetadata().offset());
            }).exceptionally(ex -> {
                log.warning("Failed to send user message: " + eventType + " for user " + user.getEmail() + 
                           ". Error: " + ex.getMessage());
                return null;
            });
            
        } catch (Exception e) {
            log.warning("Error creating user message for user " + user.getEmail() + ": " + e.getMessage());
        }
    }

    /**
     * Sends multiple user messages for initial load.
     * 
     * @param users List of users to send
     */
    public void sendInitialUserLoad(List<User> users) {
        log.info("KAFKA USER: Starting initial load of " + users.size() + " users");
        
        users.forEach(user -> {
            sendUserMessage(user, "INITIAL_LOAD");
        });
        
        log.info("KAFKA USER: Initial load completed for " + users.size() + " users");
    }

    /**
     * Sends user created event.
     * 
     * @param user Created user
     */
    public void sendUserCreated(User user) {
        sendUserMessage(user, "USER_CREATED");
    }

    /**
     * Sends user updated event.
     * 
     * @param user Updated user
     */
    public void sendUserUpdated(User user) {
        sendUserMessage(user, "USER_UPDATED");
    }

    /**
     * Sends user deleted event.
     * 
     * @param user Deleted user
     */
    public void sendUserDeleted(User user) {
        sendUserMessage(user, "USER_DELETED");
    }
}
