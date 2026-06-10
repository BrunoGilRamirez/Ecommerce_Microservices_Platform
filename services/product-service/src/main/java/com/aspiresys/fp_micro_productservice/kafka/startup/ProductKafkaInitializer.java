package com.aspiresys.fp_micro_productservice.kafka.startup;

import com.aspiresys.fp_micro_productservice.kafka.producer.ProductProducerService;
import com.aspiresys.fp_micro_productservice.product.Product;
import com.aspiresys.fp_micro_productservice.product.ProductService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Startup component that sends initial product data to Kafka when the application starts.
 * This ensures that the order service has all existing product information.
 * 
 * @author bruno.gil
 */
@Component
@Log
public class ProductKafkaInitializer implements ApplicationRunner {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductProducerService productProducerService;

    /**
     * Runs when the application starts and sends all existing products to Kafka.
     * 
     * @param args Application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Starting Kafka initialization - sending existing products...");
            
            // Get all existing products
            List<Product> allProducts = productService.getAllProducts();
            
            if (allProducts.isEmpty()) {
                log.info("No existing products found. Skipping initial Kafka load.");
                return;
            }
            
            // Send all products to Kafka
            productProducerService.sendInitialProductList(allProducts);
            
            log.info("Kafka initialization completed successfully. Sent " + 
                     allProducts.size() + " products to Kafka topic.");
            
        } catch (Exception e) {
            log.severe("Error during Kafka initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
