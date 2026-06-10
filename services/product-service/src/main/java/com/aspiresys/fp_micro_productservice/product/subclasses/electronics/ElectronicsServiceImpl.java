package com.aspiresys.fp_micro_productservice.product.subclasses.electronics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing Electronics entities.
 * <p>
 * This class provides methods to save, retrieve, and delete electronics products
 * by interacting with the {@link ElectronicsRepository}.
 * </p>
 *
 * <ul>
 *   <li>{@link #saveElectronics(Electronics)} - Saves a new or existing electronics entity.</li>
 *   <li>{@link #getAllElectronics()} - Retrieves all electronics entities.</li>
 *   <li>{@link #getElectronicsById(Long)} - Retrieves an electronics entity by its ID.</li>
 *   <li>{@link #deleteElectronics(Long)} - Deletes an electronics entity by its ID.</li>
 * </ul>
 *
 * @author bruno.gil
 * @see ElectronicsService
 * @see ElectronicsRepository
 */
@Service
public class ElectronicsServiceImpl implements ElectronicsService {
    @Autowired
    private ElectronicsRepository electronicsRepository;

    @Override
    public Electronics saveElectronics(Electronics electronics) {
        return electronicsRepository.save(electronics);
    }

    @Override
    public List<Electronics> getAllElectronics() {
        return electronicsRepository.findAll();
    }

    @Override
    public Electronics getElectronicsById(Long id) {
        Optional<Electronics> electronics = electronicsRepository.findById(id);
        return electronics.orElse(null);
    }

    @Override
    public void deleteElectronics(Long id) {
        electronicsRepository.deleteById(id);
    }
}
