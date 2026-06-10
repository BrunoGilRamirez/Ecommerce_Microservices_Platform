package com.aspiresys.fp_micro_productservice.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Abstract base class representing a product entity.
 * <p>
 * This class is mapped as a JPA entity with joined inheritance strategy,
 * allowing subclasses to represent specific types of products.
 * </p>
 *
 * Fields:
 * <ul>
 *   <li><b>id</b>: Unique identifier for the product (auto-generated).</li>
 *   <li><b>stock</b>: Quantity of the product available in inventory.</li>
 *   <li><b>name</b>: Name of the product.</li>
 *   <li><b>price</b>: Price of the product.</li>
 *   <li><b>category</b>: Category to which the product belongs.</li>
 *   <li><b>imageUrl</b>: URL of the product's image.</li>
 * </ul>
 *
 * Annotations:
 * <ul>
 *   <li>{@code @Entity}: Marks this class as a JPA entity.</li>
 *   <li>{@code @Inheritance(strategy = InheritanceType.JOINED)}: Specifies joined table inheritance for subclasses.</li>
 *   <li>Lombok annotations for getters, setters, constructors.</li>
 * </ul>
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        columnNames = {
            "name",
            "category",
            "imageUrl"
        }
    ))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int stock;
    private String name;
    private Double price;
    private String category;
    private String imageUrl;
}
