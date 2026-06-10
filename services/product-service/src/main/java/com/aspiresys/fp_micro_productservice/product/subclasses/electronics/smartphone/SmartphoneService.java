package com.aspiresys.fp_micro_productservice.product.subclasses.electronics.smartphone;

import java.util.List;

public interface SmartphoneService {
    /**
     * Saves a smartphone to the database.
     * 
     * @param smartphone the smartphone object to save
     * @return the saved smartphone object
     */
    Smartphone saveSmartphone(Smartphone smartphone);

    /**
     * Retrieves all smartphones from the database.
     * 
     * @return a list of all smartphones
     */
    List<Smartphone> getAllSmartphones();

    /**
     * Retrieves a smartphone by its ID.
     * 
     * @param id the ID of the smartphone to retrieve
     * @return the smartphone object if found, or null if not found
     */
    Smartphone getSmartphoneById(Long id);

    /**
     * Updates an existing smartphone in the database.
     * 
     * @param id the ID of the smartphone to update
     * @param smartphone the updated smartphone object
     * @return the updated smartphone object
     */
    Smartphone updateSmartphone(Long id, Smartphone smartphone);
    /**
     * Deletes a smartphone by its ID.
     * 
     * @param id the ID of the smartphone to delete
     */
    void deleteSmartphone(Long id);
    /**
     * Checks if a smartphone already exists in the database.
     * This method compares the smartphone's attributes to determine if it is a duplicate.
     * 
     * @param smartphone
     * @return
     */
    boolean exists(Smartphone smartphone);
}
