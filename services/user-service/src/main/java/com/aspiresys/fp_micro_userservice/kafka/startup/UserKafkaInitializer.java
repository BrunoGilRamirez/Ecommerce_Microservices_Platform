package com.aspiresys.fp_micro_userservice.kafka.startup;

import com.aspiresys.fp_micro_userservice.kafka.producer.UserProducerService;
import com.aspiresys.fp_micro_userservice.user.User;
import com.aspiresys.fp_micro_userservice.user.UserRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes Kafka by sending existing users on application startup.
 * This ensures Order Service has all existing users synchronized.
 * 
 * @author bruno.gil
 */
@Component
@Log
public class UserKafkaInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProducerService userProducerService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            log.info("KAFKA USER INITIALIZER: Starting initial user load...");
            
            List<User> existingUsers = (List<User>) userRepository.findAll();
            
            if (!existingUsers.isEmpty()) {
                log.info("KAFKA USER INITIALIZER: Found " + existingUsers.size() + " existing users to synchronize");
                userProducerService.sendInitialUserLoad(existingUsers);
                log.info("KAFKA USER INITIALIZER: Initial load completed successfully");
            } else {
                log.info("KAFKA USER INITIALIZER: No existing users found to synchronize");
            }
            
        } catch (Exception e) {
            log.warning("KAFKA USER INITIALIZER: Error during initial load: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
