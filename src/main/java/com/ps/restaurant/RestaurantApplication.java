package com.ps.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Boots the restaurant API and enables Spring Boot component discovery. */
@SpringBootApplication
public class RestaurantApplication {

	/** Starts the Spring application context and its embedded web server. */
	public static void main(String[] args) {
		SpringApplication.run(RestaurantApplication.class, args);
	}

}
