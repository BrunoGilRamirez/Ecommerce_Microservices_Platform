package com.aspiresys.fp_micro_productservice.product.subclasses.electronics;

import java.util.List;

public interface ElectronicsService {
    Electronics saveElectronics(Electronics electronics);
    List<Electronics> getAllElectronics();
    Electronics getElectronicsById(Long id);
    void deleteElectronics(Long id);
}
