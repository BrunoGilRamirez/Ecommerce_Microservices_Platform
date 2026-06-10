package com.aspiresys.fp_micro_productservice.product.subclasses.electronics;

import com.aspiresys.fp_micro_productservice.product.Product;

import jakarta.persistence.Entity;

import lombok.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)

public abstract class Electronics extends Product {

    private String brand;
    private String model;
    private String warrantyPeriod;
    private String specifications;

}