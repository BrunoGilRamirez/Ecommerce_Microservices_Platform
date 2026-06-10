package com.aspiresys.fp_micro_productservice.aop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.aspiresys.fp_micro_productservice.product.Product;
import com.aspiresys.fp_micro_productservice.product.ProductRepository;
import com.aspiresys.fp_micro_productservice.product.ProductService;
import com.aspiresys.fp_micro_productservice.product.subclasses.electronics.smartphone.Smartphone;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Integration test to verify that AOP works correctly
 * in the product service. Uses the full Spring
 * context so that the aspects function properly.
 * 
 * @author bruno.gil
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.aop.audit.enabled=true",
    "app.aop.performance.enabled=true", 
    "app.aop.validation.enabled=true",
    "spring.aop.auto=true",
    "spring.aop.proxy-target-class=true",
    "spring.cloud.config.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false"
})
public class AopIntegrationTest {

    @SuppressWarnings("removal")
    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Test
    void testAopAnnotationsOnSaveProduct() {
        // Arrange
        Smartphone testProduct = createMockSmartphone(1L, "Test Smartphone", 999.99);
        
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act - This method should trigger AOP aspects
        Product result = productService.saveProduct(testProduct);

        // Assert
        assertNotNull(result, "Product should be saved successfully");
        assertEquals("Test Smartphone", result.getName(), "Product name should match");
        assertEquals(999.99, result.getPrice(), "Product price should match");

        // AOP aspects should have been executed:
        // 1. @Auditable - should have logged the save operation
        // 2. @ExecutionTime - should have measured the execution time  
        // 3. @ValidateParameters - should have validated that the product is not null

        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void testAopAnnotationsOnGetAllProducts() {
        // Arrange
        List<Product> mockProducts = Arrays.asList(
            createMockSmartphone(1L, "Smartphone 1", 500.0),
            createMockSmartphone(2L, "Smartphone 2", 800.0)
        );
        
        when(productRepository.findAll()).thenReturn(mockProducts);

        // Act - This method should trigger the execution time aspect
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result, "Product list should not be null");
        assertEquals(2, result.size(), "Should return 2 products");

        // The @ExecutionTime aspect should have measured the execution time
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testAopAnnotationsOnGetProductById() {
        // Arrange
        Long productId = 1L;
        Smartphone mockProduct = createMockSmartphone(productId, "Test Smartphone", 799.0);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act - This method should trigger validation and timing aspects
        Product result = productService.getProductById(productId);

        // Assert
        assertNotNull(result, "Product should be found");
        assertEquals(productId, result.getId(), "Product ID should match");

        // The aspects should have been executed:
        // 1. @ExecutionTime - execution time measurement
        // 2. @ValidateParameters - validation of non-null ID
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testValidationAspectWithNullProduct() {
        // Act & Assert - Should throw exception due to AOP validation
        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(null);
        }, "Should throw IllegalArgumentException for null product");

        // Should not call the repository if validation fails
        verify(productRepository, never()).save(any());
    }

    @Test
    void testValidationAspectWithNullId() {
        // Act & Assert - Should throw exception due to AOP validation
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductById(null);
        }, "Should throw IllegalArgumentException for null ID");

        // Should not call the repository if validation fails
        verify(productRepository, never()).findById(any());
    }

    @Test
    void testAopAnnotationsOnDeleteProduct() {
        // Arrange
        Long productId = 1L;
        
        doNothing().when(productRepository).deleteById(productId);

        // Act - This method should trigger auditing, timing, and validation aspects
        assertDoesNotThrow(() -> {
            productService.deleteProduct(productId);
        });

        // AOP aspects should have been executed:
        // 1. @Auditable - delete audit
        // 2. @ExecutionTime - execution time measurement
        // 3. @ValidateParameters - validation of non-null ID
        verify(productRepository, times(1)).deleteById(productId);
    }

    /**
     * Helper method to create test smartphones
     */
    private Smartphone createMockSmartphone(Long id, String name, Double price) {
        Smartphone smartphone = new Smartphone();
        smartphone.setId(id);
        smartphone.setName(name);
        smartphone.setPrice(price);
        smartphone.setCategory("Electronics");
        smartphone.setBrand("TestBrand");
        smartphone.setModel("TestModel");
        smartphone.setOperatingSystem("Android");
        smartphone.setStorageCapacity(128);
        smartphone.setRam(8);
        smartphone.setProcessor("TestProcessor");
        smartphone.setScreenSize(6.1);
        smartphone.setStock(10);
        return smartphone;
    }
}
