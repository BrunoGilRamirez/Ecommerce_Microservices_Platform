package com.aspiresys.fp_micro_productservice.kafka.controller;

import com.aspiresys.fp_micro_productservice.common.dto.AppResponse;
import com.aspiresys.fp_micro_productservice.kafka.producer.ProductProducerService;
import com.aspiresys.fp_micro_productservice.product.Product;
import com.aspiresys.fp_micro_productservice.product.ProductService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Kafka synchronization operations.
 * Provides administrative endpoints for forcing product synchronization with other services.
 * 
 * This controller is restricted to ADMIN users only and provides:
 * - Force full product synchronization to Kafka
 * - Sync status information
 * 
 * @author bruno.gil
 */
@RestController
@RequestMapping("/products/kafka/sync")
@Log
public class KafkaSyncController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductProducerService productProducerService;

    /**
     * Forces a full synchronization of all products to Kafka.
     * This endpoint should be used when:
     * - Setting up a new order service instance
     * - Recovering from Kafka/messaging issues
     * - Manual data synchronization is required
     * 
     * ⚠️ Warning: This will send ALL products to Kafka. Use with caution to avoid message flooding.
     * 
     * @return ResponseEntity with sync results
     */
    @PostMapping("/force-full-sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<String>> forceFullProductSync() {
        try {
            log.info("Admin initiated full product synchronization to Kafka");
            
            // Get all existing products
            List<Product> allProducts = productService.getAllProducts();
            
            if (allProducts.isEmpty()) {
                String message = "No products found in database. Nothing to synchronize.";
                log.info(message);
                return ResponseEntity.ok(new AppResponse<>(message, "0 products synchronized"));
            }
            
            // Send all products to Kafka with INITIAL_LOAD event type
            productProducerService.sendInitialProductList(allProducts);
            
            String successMessage = "Full product synchronization completed successfully";
            String details = allProducts.size() + " products sent to Kafka topic";
            
            log.info(successMessage + ". " + details);
            
            return ResponseEntity.ok(new AppResponse<>(successMessage, details));
            
        } catch (Exception e) {
            String errorMessage = "Failed to perform full product synchronization: " + e.getMessage();
            log.severe(errorMessage);
            e.printStackTrace();
            
            return ResponseEntity.status(500)
                    .body(new AppResponse<>(errorMessage, "Synchronization failed"));
        }
    }

    /**
     * Gets the current status of products available for synchronization.
     * Returns basic statistics about products in the database.
     * 
     * @return ResponseEntity with product statistics
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<String>> getSyncStatus() {
        try {
            List<Product> allProducts = productService.getAllProducts();
            
            String status = String.format(
                "Product synchronization status:\n" +
                "Total products available: %d\n" +
                "Categories: clothes, smartphone\n" +
                "Kafka topic: product\n" +
                "Use /force-full-sync to synchronize all products",
                allProducts.size()
            );
            
            log.info("Admin requested sync status. Total products: " + allProducts.size());
            
            return ResponseEntity.ok(new AppResponse<>("Sync status retrieved successfully", status));
            
        } catch (Exception e) {
            String errorMessage = "Failed to retrieve sync status: " + e.getMessage();
            log.severe(errorMessage);
            
            return ResponseEntity.status(500)
                    .body(new AppResponse<>(errorMessage, "Status check failed"));
        }
    }
}
