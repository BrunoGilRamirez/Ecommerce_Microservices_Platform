package com.aspiresys.fp_micro_productservice.product.subclasses.electronics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectronicsRepository extends JpaRepository<Electronics, Long> {
}
