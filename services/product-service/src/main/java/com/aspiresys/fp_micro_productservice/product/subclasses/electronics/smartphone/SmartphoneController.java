package com.aspiresys.fp_micro_productservice.product.subclasses.electronics.smartphone;

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
 * SmartphoneController handles REST API endpoints for managing smartphone products.
 * It provides CRUD operations for smartphone category.
 *
 * Endpoints:
 * <ul>
 *   <li>POST /products/smartphones - Create a new Smartphone item</li>
 *   <li>GET /products/smartphones - Retrieve all Smartphone items</li>
 *   <li>GET /products/smartphones/{id} - Retrieve a Smartphone item by ID</li>
 *   <li>PUT /products/smartphones/{id} - Update a Smartphone item by ID</li>
 *   <li>DELETE /products/smartphones/{id} - Delete a Smartphone item by ID</li>
 * </ul>
 *
 * All responses are wrapped in {@link AppResponse} for consistent API responses.
 *
 * Example usage:
 * <pre>
 *   POST /products/smartphones
 *   {
 *     "name": "iPhone 15",
 *     "price": 1200.0,
 *     "category": "smartphone",
 *     "imageUrl": "url",
 *     "stock": 5,
 *     "brand": "Apple",
 *     "operatingSystem": "iOS",
 *     "storageCapacity": 256,
 *     "ram": 8,
 *     "processor": "A17 Pro",
 *     "screenSize": 6.1
 *   }
 * </pre>
 *
 * @author bruno.gil
 */
@RestController
@RequestMapping("/products/smartphones")
@Log
public class SmartphoneController {
    
    @Autowired
    private SmartphoneService smartphoneService;

    @Autowired
    private ProductProducerService productProducerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<Smartphone>> createSmartphone(@RequestBody Smartphone smartphone) {
        smartphone.setCategory("smartphone"); // Ensure category is set
        TupleResponse<Boolean, String> validation = ProductUtils.isAValidProduct(smartphone);
        if (!validation.getFirst()) {
            return ResponseEntity.badRequest().body(new AppResponse<>("Invalid smartphone data: " + validation.getSecond(), null));
        }
        try {
            Smartphone created = smartphoneService.saveSmartphone(smartphone);
            
            // Send product created event to Kafka
            try {
                productProducerService.sendProductCreated(created);
                log.info("Product created event sent to Kafka for smartphone ID: " + created.getId());
            } catch (Exception kafkaException) {
                log.warning("Failed to send product created event to Kafka: " + kafkaException.getMessage());
                // Product was created successfully, but Kafka failed - continue with success response
            }
            
            log.info("Smartphone created successfully: " + created);
            return ResponseEntity.ok(new AppResponse<>("Smartphone created successfully", created));
        } catch (ProductException ex) {
            log.warning("Error creating smartphone: " + ex.getMessage());
            return ResponseEntity.badRequest().body(new AppResponse<>(("Smartphone with these attributes already exists: " + 
                "name=" + smartphone.getName() + 
                ", category=" + smartphone.getCategory() + 
                ", imageUrl=" + smartphone.getImageUrl() + 
                ", brand=" + smartphone.getBrand() + 
                ", operatingSystem=" + smartphone.getOperatingSystem() + 
                ", storageCapacity=" + smartphone.getStorageCapacity() + 
                ", ram=" + smartphone.getRam() + 
                ", processor=" + smartphone.getProcessor() + 
                ", screenSize=" + smartphone.getScreenSize()), null));
        }
    }

    @GetMapping
    public ResponseEntity<AppResponse<List<Smartphone>>> getAllSmartphones() {
        List<Smartphone> list = smartphoneService.getAllSmartphones();
        return ResponseEntity.ok(new AppResponse<>("Smartphone list retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppResponse<Smartphone>> getSmartphoneById(@PathVariable Long id) {
        Smartphone smartphone = smartphoneService.getSmartphoneById(id);
        if (smartphone == null) {
            return ResponseEntity.status(404).body(new AppResponse<>("Smartphone not found", null));
        }
        return ResponseEntity.ok(new AppResponse<>("Smartphone found", smartphone));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<Smartphone>> updateSmartphone(@PathVariable Long id, @RequestBody Smartphone smartphone) {
        Smartphone existing = smartphoneService.getSmartphoneById(id);
        TupleResponse<Boolean, String> validation = ProductUtils.isAValidProduct(smartphone);
        if (!validation.getFirst()) {
            return ResponseEntity.badRequest().body(new AppResponse<>("Invalid smartphone data: " + validation.getSecond(), null));
        }
        if (existing == null) {
            return ResponseEntity.status(404).body(new AppResponse<>("Smartphone not found", null));
        }
        smartphone.setId(id);
        Smartphone updated = smartphoneService.saveSmartphone(smartphone);
        
        // Send product updated event to Kafka
        try {
            productProducerService.sendProductUpdated(updated);
            log.info("Product updated event sent to Kafka for smartphone ID: " + updated.getId());
        } catch (Exception kafkaException) {
            log.warning("Failed to send product updated event to Kafka: " + kafkaException.getMessage());
            // Product was updated successfully, but Kafka failed - continue with success response
        }
        
        return ResponseEntity.ok(new AppResponse<>("Smartphone updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<Boolean>> deleteSmartphone(@PathVariable Long id) {
        Smartphone existing = smartphoneService.getSmartphoneById(id);
        if (existing == null) {
            return ResponseEntity.status(404).body(new AppResponse<>("Smartphone not found", false));
        }
        
        // Send product deleted event to Kafka before deleting
        try {
            productProducerService.sendProductDeleted(existing.getId());
            log.info("Product deleted event sent to Kafka for smartphone ID: " + existing.getId());
        } catch (Exception kafkaException) {
            log.warning("Failed to send product deleted event to Kafka: " + kafkaException.getMessage());
            // Continue with deletion even if Kafka fails
        }
        
        smartphoneService.deleteSmartphone(id);
        return ResponseEntity.ok(new AppResponse<>("Smartphone deleted successfully", true));
    }
}
