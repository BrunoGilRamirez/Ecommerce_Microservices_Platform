package com.aspiresys.fp_micro_userservice.aop;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.aspiresys.fp_micro_userservice.user.User;
import com.aspiresys.fp_micro_userservice.user.UserRepository;
import com.aspiresys.fp_micro_userservice.user.UserService;

/**
 * Test simple para verificar que los aspectos de validación funcionan.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.config.import=optional:configserver:",
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
    "spring.aop.auto=true",
    "spring.aop.proxy-target-class=true"
})
public class SimpleValidationTest {

    @SuppressWarnings("removal")
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        assertNotNull(userService);
    }

    @Test
    void testSaveUserWithValidUser() {
        // Arrange
        User validUser = createValidUser();
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act & Assert - No debería lanzar excepción
        assertDoesNotThrow(() -> {
            userService.saveUser(validUser);
        });
    }

    @Test
    void testSaveUserWithNullUser() {
        // Act & Assert - Debería lanzar IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.saveUser(null);
        });
        
        // Verificar que se lanzó IllegalArgumentException
        assertNotNull(exception);
        System.out.println("Exception type: " + exception.getClass().getSimpleName());
        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    void testGetUserByIdWithNullId() {
        // Act & Assert - Debería lanzar IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(null);
        });
        
        // Verificar que se lanzó IllegalArgumentException
        assertNotNull(exception);
        System.out.println("Exception type: " + exception.getClass().getSimpleName());
        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    void testGetUserByEmailWithInvalidEmail() {
        // Act & Assert - Debería lanzar IllegalArgumentException
        // Usar un email que contenga @ pero tenga formato inválido
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByEmail("invalid-email@");
        });
        
        // Verificar que se lanzó IllegalArgumentException
        assertNotNull(exception);
        System.out.println("Exception type: " + exception.getClass().getSimpleName());
        System.out.println("Exception message: " + exception.getMessage());
    }

    private User createValidUser() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        return user;
    }
}
