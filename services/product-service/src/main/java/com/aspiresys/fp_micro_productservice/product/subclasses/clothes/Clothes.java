package com.aspiresys.fp_micro_productservice.product.subclasses.clothes;


import com.aspiresys.fp_micro_productservice.product.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;


/**
 * Represents a clothing product with specific attributes such as size, color, and fabric type.
 * Inherits common product properties from the {@link Product} class.
 * <p>
 * This entity is mapped to a database table for persistence.
 * </p>
 *
 * @author bruno.gil
 */
@Entity
@Table(
    uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        columnNames = {
            "name", 
            "category",
            "imageUrl",
            "brand", 
            "size", 
            "color", 
            "fabricType"}
    )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Clothes extends Product {
    private String brand;
    private String size;
    private String color;
    private String fabricType;
}