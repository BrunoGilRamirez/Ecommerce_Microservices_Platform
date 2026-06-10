package com.aspiresys.fp_micro_productservice.product.subclasses.electronics.smartphone;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmartphoneRepository extends JpaRepository<Smartphone, Long> {
}
