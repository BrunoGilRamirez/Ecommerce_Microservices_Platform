package com.aspiresys.fp_micro_productservice.product;

import com.aspiresys.fp_micro_productservice.common.dto.AppResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link ProductController} class.
 * <p>
 * This test class verifies the behavior of the ProductController methods,
 * ensuring that they return proper {@link ResponseEntity} containing {@link AppResponse}
 * with the expected data and status codes.
 * <p>
 * The test uses AssertJ for more fluent assertions and follows best practices for unit testing.
 * 
 * @author bruno.gil
 */
@DisplayName("ProductController Tests")
public class ProductControllerTest {

    private ProductController productController;

    @BeforeEach
    void setUp() {
        productController = new ProductController();
    }

    @Test
    @DisplayName("getCategories should return all available categories")
    void testGetCategoriesReturnsAllCategories() {
        // When
        ResponseEntity<AppResponse<java.util.List<String>>> response = productController.getCategories();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        AppResponse<java.util.List<String>> appResponse = response.getBody();
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getMessage()).isEqualTo("Categories retrieved successfully");
        
        java.util.List<String> categories = appResponse.getData();
        assertThat(categories).isNotNull()
                             .hasSize(3)
                             .containsExactly("clothes", "electronics", "smartphone");
    }
}