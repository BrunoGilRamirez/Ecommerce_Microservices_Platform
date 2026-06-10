package com.aspiresys.fp_micro_productservice.product.subclasses.clothes;

import java.util.List;

public interface ClothesService {
    Clothes saveClothes(Clothes clothes);
    List<Clothes> getAllClothes();
    Clothes getClothesById(Long id);
    void deleteClothes(Long id);
    boolean exist(Clothes clothes);
}
