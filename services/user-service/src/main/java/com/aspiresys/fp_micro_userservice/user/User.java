package com.aspiresys.fp_micro_userservice.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import lombok.*;

/**
 * Represents a user entity in the system.
 * <p>
 * This class is mapped to the "users" table in the database and contains
 * basic user information such as first name, last name, and email.
 * </p>
 *
 * <p>
 * Annotations:
 * <ul>
 *   <li>{@code @Entity} - Specifies that the class is an entity.</li>
 *   <li>{@code @Table(name = "users")} - Maps the entity to the "users" table.</li>
 *   <li>{@code @Getter}, {@code @Setter} - Lombok annotations to generate getters and setters.</li>
 *   <li>{@code @NoArgsConstructor}, {@code @AllArgsConstructor}, {@code @Builder} - Lombok annotations for constructors and builder pattern.</li>
 * </ul>
 * </p>
 *
 * Fields:
 * <ul>
 *   <li>{@code id} - The unique identifier for the user (primary key).</li>
 *   <li>{@code firstName} - The user's first name.</li>
 *   <li>{@code lastName} - The user's last name.</li>
 *   <li>{@code email} - The user's email address.</li>
 *  <li>{@code address} - The user's physical address.</li>
 * </ul>
 * 
 * @author bruno.gil
 * 
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String address;

}
