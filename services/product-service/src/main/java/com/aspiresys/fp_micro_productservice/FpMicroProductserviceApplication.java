package com.aspiresys.fp_micro_productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import com.aspiresys.fp_micro_productservice.product.subclasses.clothes.ClothesRepository;
import com.aspiresys.fp_micro_productservice.product.subclasses.clothes.Clothes;
import com.aspiresys.fp_micro_productservice.product.subclasses.electronics.smartphone.SmartphoneRepository;
import com.aspiresys.fp_micro_productservice.product.subclasses.electronics.smartphone.Smartphone;

@SpringBootApplication
public class FpMicroProductserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FpMicroProductserviceApplication.class, args);
	}
	
	/**
	 * CommandLineRunner to load initial data into the database.
	 * This will only run when the application is started in a non-test profile.
	 */
	@Bean
	@Profile("!test")// this bean will not be loaded in the test profile
	public CommandLineRunner dataLoader(
			@Autowired ClothesRepository clothesRepository,
			@Autowired SmartphoneRepository smartphoneRepository) {
		return args -> {
		   if (clothesRepository.count() == 0) {
			   Clothes c1 = new Clothes();
			   c1.setStock(100);
			   c1.setName("Basic T-shirt");
			   c1.setPrice(199.99);
			   c1.setCategory("clothes");
			   c1.setImageUrl("https://ejemplo.com/camiseta.jpg");
			   c1.setBrand("PremiumWear");
			   c1.setSize("M");
			   c1.setColor("Blue");
			   c1.setFabricType("Cotton");
			   clothesRepository.save(c1);

			   Clothes c2 = new Clothes();
			   c2.setStock(50);
			   c2.setName("Sports Pants");
			   c2.setPrice(299.99);
			   c2.setCategory("clothes");
			   c2.setImageUrl("https://ejemplo.com/pantalon.jpg");
			   c2.setBrand("ActiveGear");
			   c2.setSize("L");
			   c2.setColor("Black");
			   c2.setFabricType("Polyester");
			   clothesRepository.save(c2);
		   }
		   if (smartphoneRepository.count() == 0) {
			   Smartphone s1 = new Smartphone();
			   s1.setStock(30);
			   s1.setName("Smartphone X1");
			   s1.setPrice(5999.99);
			   s1.setCategory("smartphone");
			   s1.setImageUrl("https://ejemplo.com/smartphone.jpg");
			   s1.setBrand("BrandX");
			   s1.setModel("X1");
			   s1.setWarrantyPeriod("1 year");
			   s1.setSpecifications("Octa-core, 128GB, 6GB RAM");
			   s1.setOperatingSystem("Android");
			   s1.setStorageCapacity(128);
			   s1.setRam(6);
			   s1.setProcessor("Snapdragon 888");
			   s1.setScreenSize(6.5);
			   smartphoneRepository.save(s1);

			   Smartphone s2 = new Smartphone();
			   s2.setStock(20);
			   s2.setName("Smartphone Y2");
			   s2.setPrice(7999.99);
			   s2.setCategory("smartphone");
			   s2.setImageUrl("https://ejemplo.com/smartphone2.jpg");
			   s2.setBrand("BrandY");
			   s2.setModel("Y2");
			   s2.setWarrantyPeriod("2 years");
			   s2.setSpecifications("Octa-core, 256GB, 8GB RAM");
			   s2.setOperatingSystem("Android");
			   s2.setStorageCapacity(256);
			   s2.setRam(8);
			   s2.setProcessor("Exynos 2100");
			   s2.setScreenSize(6.7);
			   smartphoneRepository.save(s2);
		   }
		};
	}


}
