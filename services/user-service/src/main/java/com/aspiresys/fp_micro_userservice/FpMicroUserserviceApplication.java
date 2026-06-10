package com.aspiresys.fp_micro_userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import com.aspiresys.fp_micro_userservice.user.UserRepository;
import com.aspiresys.fp_micro_userservice.user.User;

@SpringBootApplication
@EnableDiscoveryClient
public class FpMicroUserserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FpMicroUserserviceApplication.class, args);
	}

	@Bean
	public CommandLineRunner dataLoader(@Autowired UserRepository userRepository) {
		return args -> {
			if (userRepository.count() == 0) {
				User u1 = new User();
				u1.setFirstName("John");
				u1.setLastName("Doe");
				u1.setEmail("john.doe@example.com");
				userRepository.save(u1);

				User u2 = new User();
				u2.setFirstName("Jane");
				u2.setLastName("Smith");
				u2.setEmail("jane.smith@example.com");
				userRepository.save(u2);

				User u3 = new User();
				u3.setFirstName("Alice");
				u3.setLastName("Johnson");
				u3.setEmail("alice.johnson@example.com");
				userRepository.save(u3);
			}
		};
	}

}
