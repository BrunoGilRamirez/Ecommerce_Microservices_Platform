package com.aspiresys.fp_micro_productservice.product.subclasses.electronics.smartphone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aspiresys.fp_micro_productservice.product.ProductException;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing {@link Smartphone} entities.
 * Provides methods to save, retrieve, and delete smartphones using the {@link SmartphoneRepository}.
 *
 * <p>This class is annotated with {@code @Service} to indicate that it's a Spring service component.
 * It implements the {@link SmartphoneService} interface.</p>
 *
 * <ul>
 *   <li>{@link #saveSmartphone(Smartphone)} - Saves a smartphone entity to the repository.</li>
 *   <li>{@link #getAllSmartphones()} - Retrieves all smartphones from the repository.</li>
 *   <li>{@link #getSmartphoneById(Long)} - Retrieves a smartphone by its ID.</li>
 *   <li>{@link #deleteSmartphone(Long)} - Deletes a smartphone by its ID.</li>
 * </ul>
 *
 * @author bruno.gil
 */
@Service
public class SmartphoneServiceImpl implements SmartphoneService {
    @Autowired
    private SmartphoneRepository smartphoneRepository;

    @Override
    public Smartphone saveSmartphone(Smartphone smartphone) {
        try{
            return smartphoneRepository.save(smartphone);
        } catch (Exception ex) {
            throw new ProductException().duplicateProduct("This smartphone already exists with the same attributes: " + 
                "name=" + smartphone.getName() + 
                ", category=" + smartphone.getCategory() + 
                ", imageUrl=" + smartphone.getImageUrl() + 
                ", brand=" + smartphone.getBrand() + 
                ", operatingSystem=" + smartphone.getOperatingSystem() + 
                ", storageCapacity=" + smartphone.getStorageCapacity() + 
                ", ram=" + smartphone.getRam() + 
                ", processor=" + smartphone.getProcessor() + 
                ", screenSize=" + smartphone.getScreenSize());
    
        }
    }

    @Override
    public List<Smartphone> getAllSmartphones() {
        return smartphoneRepository.findAll();
    }

    @Override
    public Smartphone getSmartphoneById(Long id) {
        Optional<Smartphone> smartphone = smartphoneRepository.findById(id);
        return smartphone.orElse(null);
    }

    @Override
    public void deleteSmartphone(Long id) {
        smartphoneRepository.deleteById(id);
    }

    @Override
    public Smartphone updateSmartphone(Long id, Smartphone smartphone) {
        // TODO Auto-generated method stub
        Optional<Smartphone> existingSmartphone = smartphoneRepository.findById(id);
        if (existingSmartphone.isPresent()) {
            Smartphone updatedSmartphone = existingSmartphone.get();
            updatedSmartphone.setName(smartphone.getName());
            updatedSmartphone.setPrice(smartphone.getPrice());
            updatedSmartphone.setCategory(smartphone.getCategory());
            updatedSmartphone.setImageUrl(smartphone.getImageUrl());
            updatedSmartphone.setStock(smartphone.getStock());
            updatedSmartphone.setOperatingSystem(smartphone.getOperatingSystem());
            updatedSmartphone.setStorageCapacity(smartphone.getStorageCapacity());
            updatedSmartphone.setRam(smartphone.getRam());
            updatedSmartphone.setProcessor(smartphone.getProcessor());
            updatedSmartphone.setScreenSize(smartphone.getScreenSize());
            return smartphoneRepository.save(updatedSmartphone);
        }
        return null; // or throw an exception if preferred
    }

    @Override
    public boolean exists(Smartphone smartphone) {
        List<Smartphone> smartphones = smartphoneRepository.findAll();
        for (Smartphone existingSmartphone : smartphones) {
            boolean flag= existingSmartphone.equals(smartphone);
            System.out.println("Comparing: " + existingSmartphone + " with " + smartphone + " - Result: " + flag);
            if (flag) {
                return true; // Smartphone already exists
            }
        }
        return false; // Smartphone does not exist
    }
}
