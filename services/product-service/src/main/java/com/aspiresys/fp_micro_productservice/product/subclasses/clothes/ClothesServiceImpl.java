package com.aspiresys.fp_micro_productservice.product.subclasses.clothes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aspiresys.fp_micro_productservice.product.ProductException;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing Clothes entities.
 * Provides methods to save, retrieve, and delete clothes using the ClothesRepository.
 *
 * This class is annotated with {@link org.springframework.stereotype.Service} to indicate
 * that it's a service component in the Spring context.
 *
 * Methods:
 * <ul>
 *   <li>{@link #saveClothes(Clothes)} - Saves a Clothes entity to the repository.</li>
 *   <li>{@link #getAllClothes()} - Retrieves all Clothes entities from the repository.</li>
 *   <li>{@link #getClothesById(Long)} - Retrieves a Clothes entity by its ID.</li>
 *   <li>{@link #deleteClothes(Long)} - Deletes a Clothes entity by its ID.</li>
 * </ul>
 *
 * Dependencies are injected using {@link org.springframework.beans.factory.annotation.Autowired}.
 * 
 * @author bruno.gil
 * @see ClothesService
 * @see ClothesRepository
 */
@Service
public class ClothesServiceImpl implements ClothesService {
    @Autowired
    private ClothesRepository clothesRepository;

    @Override
    public Clothes saveClothes(Clothes clothes) {
        try{
            return clothesRepository.save(clothes);
        } catch (Exception ex) {
            throw new ProductException().duplicateProduct("This clothes already exists with the same attributes: " + 
                "name=" + clothes.getName() + 
                ", category=" + clothes.getCategory() + 
                ", imageUrl=" + clothes.getImageUrl() + 
                ", brand=" + clothes.getBrand() + 
                ", size=" + clothes.getSize() + 
                ", color=" + clothes.getColor() + 
                ", fabricType=" + clothes.getFabricType());
        }
    }

    @Override
    public List<Clothes> getAllClothes() {
        return clothesRepository.findAll();
    }

    @Override
    public Clothes getClothesById(Long id) {
        Optional<Clothes> clothes = clothesRepository.findById(id);
        return clothes.orElse(null);
    }

    @Override
    public void deleteClothes(Long id) {
        clothesRepository.deleteById(id);
    }

    @Override
    public boolean exist(Clothes clothes) {
        List<Clothes> existingClothes = clothesRepository.findAll();
        for (Clothes existing : existingClothes) {
            if (existing.getName().equals(clothes.getName()) &&
                existing.getPrice() == clothes.getPrice() &&
                existing.getCategory().equals(clothes.getCategory()) &&
                existing.getImageUrl().equals(clothes.getImageUrl()) &&
                existing.getBrand().equals(clothes.getBrand()) &&
                existing.getSize().equals(clothes.getSize()) &&
                existing.getColor().equals(clothes.getColor()) &&
                existing.getFabricType().equals(clothes.getFabricType())) {
                return true;
            }
        }
        return false;
    }
}
