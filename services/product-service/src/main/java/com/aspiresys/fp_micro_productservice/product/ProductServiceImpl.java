package com.aspiresys.fp_micro_productservice.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

// AOP imports
import com.aspiresys.fp_micro_productservice.aop.annotation.Auditable;
import com.aspiresys.fp_micro_productservice.aop.annotation.ExecutionTime;
import com.aspiresys.fp_micro_productservice.aop.annotation.ValidateParameters;

/**
 * Service implementation for managing products.
 * <p>
 * This class provides methods to save, retrieve, and delete products
 * by interacting with the {@link ProductRepository}.
 * </p>
 *
 * <p>
 * The following operations are supported:
 * <ul>
 *   <li>Save a new product</li>
 *   <li>Retrieve all products</li>
 *   <li>Retrieve a product by its ID</li>
 *   <li>Delete a product by its ID</li>
 * </ul>
 * </p>
 *
 * @author bruno.gil
 * @see ProductService
 * @see ProductRepository
 */
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    @Auditable(operation = "SAVE_PRODUCT", entityType = "Product", logParameters = true, logResult = true)
    @ExecutionTime(operation = "Save Product", warningThreshold = 500, detailed = true)
    @ValidateParameters(notNull = true, message = "Product cannot be null")
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @ExecutionTime(operation = "Get All Products", warningThreshold = 1000)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @ExecutionTime(operation = "Get Product by ID")
    @ValidateParameters(notNull = true, message = "Product ID cannot be null")
    public Product getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    @Override
    @Auditable(operation = "DELETE_PRODUCT", entityType = "Product", logParameters = true)
    @ExecutionTime(operation = "Delete Product", warningThreshold = 500)
    @ValidateParameters(notNull = true, message = "Product ID cannot be null")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
