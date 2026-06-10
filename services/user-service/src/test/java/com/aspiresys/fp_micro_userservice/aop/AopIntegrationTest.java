package com.aspiresys.fp_micro_userservice.aop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.aspiresys.fp_micro_userservice.user.User;
import com.aspiresys.fp_micro_userservice.user.UserRepository;
import com.aspiresys.fp_micro_userservice.user.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Test de integración para verificar que AOP funciona correctamente
 * en el servicio de usuarios. Usa el contexto completo de Spring
 * para que los aspectos funcionen correctamente.
 * 
 * @author bruno.gil
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
public class AopIntegrationTest {

    @SuppressWarnings("removal")
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void testAopAnnotationsOnSaveUser() {
        // Arrange
        User testUser = createMockUser(1L, "test@example.com", "John", "Doe");
        
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act - Este método debería activar los aspectos AOP
        User result = userService.saveUser(testUser);

        // Assert
        assertNotNull(result, "User should be saved successfully");
        assertEquals("test@example.com", result.getEmail(), "User email should match");
        assertEquals("John", result.getFirstName(), "User first name should match");

        // Los aspectos AOP deberían haber sido ejecutados:
        // 1. @Auditable - debería haber registrado la operación de guardado
        // 2. @ExecutionTime - debería haber medido el tiempo de ejecución  
        // 3. @ValidateParameters - debería haber validado que el user no es null

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testAopAnnotationsOnGetAllUsers() {
        // Arrange
        List<User> mockUsers = Arrays.asList(
            createMockUser(1L, "user1@example.com", "John", "Doe"),
            createMockUser(2L, "user2@example.com", "Jane", "Smith")
        );
        
        when(userRepository.findAll()).thenReturn(mockUsers);

        // Act - Este método debería activar el aspecto de tiempo de ejecución
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result, "User list should not be null");
        assertEquals(2, result.size(), "Should return 2 users");

        // El aspecto @ExecutionTime debería haber medido el tiempo de ejecución
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testAopAnnotationsOnGetUserById() {
        // Arrange
        Long userId = 1L;
        User mockUser = createMockUser(userId, "test@example.com", "John", "Doe");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act - Este método debería activar aspectos de validación y tiempo
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result, "User should be found");
        assertEquals(userId, result.getId(), "User ID should match");

        // Los aspectos deberían haber sido ejecutados:
        // 1. @ExecutionTime - medición de tiempo
        // 2. @ValidateParameters - validación de ID no null
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testAopAnnotationsOnGetUserByEmail() {
        // Arrange
        String email = "test@example.com";
        User mockUser = createMockUser(1L, email, "John", "Doe");
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // Act - Este método debería activar aspectos de validación de email y tiempo
        User result = userService.getUserByEmail(email);

        // Assert
        assertNotNull(result, "User should be found");
        assertEquals(email, result.getEmail(), "User email should match");

        // Los aspectos deberían haber sido ejecutados:
        // 1. @ExecutionTime - medición de tiempo
        // 2. @ValidateParameters - validación de email format y not null
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testValidationAspectWithNullUser() {
        // Arrange - Resetear el mock antes del test para ignorar las llamadas del DataLoader
        reset(userRepository);
        
        // Act & Assert - Debería lanzar excepción por validación AOP
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> userService.saveUser(null)
        );
        
        // Verificar que el mensaje contiene el texto esperado del aspecto
        assertTrue(exception.getMessage().contains("User cannot be null"));
        assertTrue(exception.getMessage().contains("User service parameter validation failed"));

        // No debería llamar al repository si la validación falla
        verify(userRepository, never()).save(any());
    }

    @Test
    void testValidationAspectWithNullId() {
        // Arrange - Resetear el mock antes del test
        reset(userRepository);
        
        // Act & Assert - Debería lanzar excepción por validación AOP
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.getUserById(null)
        );
        
        assertTrue(exception.getMessage().contains("User ID cannot be null"));
        
        // No debería llamar al repository si la validación falla
        verify(userRepository, never()).findById(any());
    }

    @Test
    void testValidationAspectWithInvalidEmail() {
        // Arrange - Resetear el mock antes del test
        reset(userRepository);
        
        // Act & Assert - Debería lanzar excepción por validación de email
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.getUserByEmail("invalid@email@format")
        );
        
        assertTrue(exception.getMessage().contains("invalid email format"));
        
        // No debería llamar al repository si la validación falla
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void testAopAnnotationsOnDeleteUserById() {
        // Arrange
        Long userId = 1L;
        
        when(userRepository.existsById(userId)).thenReturn(false); // Después de delete
        doNothing().when(userRepository).deleteById(userId);

        // Act - Este método debería activar aspectos de auditoría, tiempo y validación
        boolean result = userService.deleteUserById(userId);

        // Assert
        assertTrue(result, "User should be deleted successfully");

        // Los aspectos AOP deberían haber sido ejecutados:
        // 1. @Auditable - auditoría de eliminación
        // 2. @ExecutionTime - medición de tiempo
        // 3. @ValidateParameters - validación de ID no null
        verify(userRepository, times(1)).deleteById(userId);
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    void testAopAnnotationsOnUpdateUser() {
        // Arrange
        User userToUpdate = createMockUser(1L, "test@example.com", "John", "Doe Updated");
        
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(userToUpdate);

        // Act - Este método debería activar aspectos de auditoría, tiempo y validación
        User result = userService.updateUser(userToUpdate);

        // Assert
        assertNotNull(result, "Updated user should not be null");
        assertEquals("Doe Updated", result.getLastName(), "Last name should be updated");

        // Los aspectos AOP deberían haber sido ejecutados:
        // 1. @Auditable - auditoría de actualización
        // 2. @ExecutionTime - medición de tiempo
        // 3. @ValidateParameters - validación de user no null
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).save(userToUpdate);
    }

    @Test
    void testAopAnnotationsOnUserExistsByEmail() {
        // Arrange
        String email = "test@example.com";
        User mockUser = createMockUser(1L, email, "John", "Doe");
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // Act - Este método debería activar aspectos de validación de email y tiempo
        boolean result = userService.userExistsByEmail(email);

        // Assert
        assertTrue(result, "User should exist");

        // Los aspectos deberían haber sido ejecutados:
        // 1. @ExecutionTime - medición de tiempo
        // 2. @ValidateParameters - validación de email format y not null
        verify(userRepository, times(1)).findByEmail(email);
    }

    /**
     * Helper method para crear usuarios de prueba
     */
    private User createMockUser(Long id, String email, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAddress("Test Address");
        return user;
    }
}
