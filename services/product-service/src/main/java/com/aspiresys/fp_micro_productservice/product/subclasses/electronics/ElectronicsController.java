package com.aspiresys.fp_micro_productservice.product.subclasses.electronics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.aspiresys.fp_micro_productservice.common.dto.AppResponse;
import com.aspiresys.fp_micro_productservice.product.ProductUtils;
import com.aspiresys.fp_micro_productservice.product.ProductUtils.TupleResponse;
import com.aspiresys.fp_micro_productservice.product.ProductException;
import com.aspiresys.fp_micro_productservice.kafka.producer.ProductProducerService;

import lombok.extern.java.Log;

/**
 * ElectronicsController handles REST API endpoints for managing electronics products.
 * It provides CRUD operations for electronics category.
 *
 * Endpoints:
 * <ul>
 *   <li>POST /products/electronics - Create a new Electronics item</li>
 *   <li>GET /products/electronics - Retrieve all Electronics items</li>
 *   <li>GET /products/electronics/{id} - Retrieve an Electronics item by ID</li>
 *   <li>PUT /products/electronics/{id} - Update an Electronics item by ID</li>
 *   <li>DELETE /products/electronics/{id} - Delete an Electronics item by ID</li>
 * </ul>
 *
 * All responses are wrapped in {@link AppResponse} for consistent API responses.
 *
 * Example usage:
 * <pre>
 *   POST /products/electronics
 *   {
 *     "name": "Laptop",
 *     "price": 1500.0,
 *     "category": "electronics",
 *     "imageUrl": "url",
 *     "stock": 3,
 *     "brand": "Dell",
 *     "model": "XPS 13",
 *     "warrantyPeriod": "24 months"
 *   }
 * </pre>
 *
 * @author bruno.gil
 */
@RestController
@RequestMapping("/products/electronics")
@Log
public class ElectronicsController {
    
    @Autowired
    private ElectronicsService electronicsService;

    @Autowired
    private ProductProducerService productProducerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<Electronics>> createElectronics(@RequestBody Electronics electronics) {
        electronics.setCategory("electronics"); // Ensure category is set
        TupleResponse<Boolean, String> validation = ProductUtils.isAValidProduct(electronics);
        if (!validation.getFirst()) {
            return ResponseEntity.badRequest().body(new AppResponse<>("Invalid electronics data: " + validation.getSecond(), null));
        }
        try {
            Electronics created = electronicsService.saveElectronics(electronics);
            
            // Send product created event to Kafka
            try {
                productProducerService.sendProductCreated(created);
                log.info("Product created event sent to Kafka for electronics ID: " + created.getId());
            } catch (Exception kafkaException) {
                log.warning("Failed to send product created event to Kafka: " + kafkaException.getMessage());
                // Product was created successfully, but Kafka failed - continue with success response
            }
            
            return ResponseEntity.ok(new AppResponse<>("Electronics created successfully", created));
        } catch (ProductException ex) {
            log.warning("Error creating electronics: " + ex.getMessage());
            return ResponseEntity.badRequest().body(new AppResponse<>(("Electronics with these attributes already exists: " + 
                "name=" + electronics.getName() + 
                ", category=" + electronics.getCategory() + 
                ", imageUrl=" + electronics.getImageUrl() + 
                ", brand=" + electronics.getBrand() + 
                ", model=" + electronics.getModel() + 
                ", warrantyPeriod=" + electronics.getWarrantyPeriod()), null));
        }
    }
    /**
     * Retrieves all Electronics items.
     * <p>
     * This endpoint must be public and accessible without authentication. When auth is implemented for this application,
     * it should be accessible to all users, including unauthenticated ones.
     * 
     * @return ResponseEntity containing a list of Electronics items wrapped in AppResponse
     * If no Electronics items are found, returns an empty list with a success message.
     * </p>
     */
    @GetMapping
    public ResponseEntity<AppResponse<List<Electronics>>> getAllElectronics() {
        List<Electronics> list = electronicsService.getAllElectronics();
        return ResponseEntity.ok(new AppResponse<>("Electronics list retrieved successfully", list));
    }

    /**
     * Retrieves an Electronics item by its ID.
     * <p>
     * This endpoint must be public and accessible without authentication. When auth is implemented for this application,
     * it should be accessible to all users, including unauthenticated ones.
     * 
     * @param id the ID of the Electronics item to retrieve
     * @return ResponseEntity containing the Electronics item wrapped in AppResponse
     * If the Electronics item is not found, returns a 404 Not Found response.
     * </p>
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppResponse<Electronics>> getElectronicsById(@PathVariable Long id) {
        Electronics electronics = electronicsService.getElectronicsById(id);
        if (electronics == null) {
            return ResponseEntity.status(404).body(new AppResponse<>("Electronics not found", null));
        }
        return ResponseEntity.ok(new AppResponse<>("Electronics found", electronics));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<Electronics>> updateElectronics(@PathVariable Long id, @RequestBody Electronics electronics) {
        Electronics existing = electronicsService.getElectronicsById(id);
        TupleResponse<Boolean, String> validation = ProductUtils.isAValidProduct(electronics);
        if (!validation.getFirst()) {
            return ResponseEntity.badRequest().body(new AppResponse<>("Invalid electronics data: " + validation.getSecond(), null));
        }
        if (existing == null) {
            return ResponseEntity.status(404).body(new AppResponse<>("Electronics not found", null));
        }
        electronics.setId(id);
        Electronics updated = electronicsService.saveElectronics(electronics);
        return ResponseEntity.ok(new AppResponse<>("Electronics updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<Boolean>> deleteElectronics(@PathVariable Long id) {
        Electronics existing = electronicsService.getElectronicsById(id);
        if (existing == null) {
            return ResponseEntity.status(404).body(new AppResponse<>("Electronics not found", false));
        }
        electronicsService.deleteElectronics(id);
        return ResponseEntity.ok(new AppResponse<>("Electronics deleted successfully", true));
    }
}
