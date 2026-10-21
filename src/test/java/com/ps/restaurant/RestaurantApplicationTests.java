package com.ps.restaurant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
class RestaurantApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void savesAndFindsUserByEmailAndUsername() {
		String username = "test-" + UUID.randomUUID();
		User user = new User();
		user.setEmail(username + "@example.com");
		user.setUser(username);
		user.setPassword("test-password");
		user.setName("Test");
		user.setSurName("User");
		userRepository.saveAndFlush(user);
		entityManager.clear();

		assertThat(userRepository.findByEmail(user.getEmail()))
			.isPresent()
			.get()
			.satisfies(saved -> {
				assertThat(saved.getId()).isEqualTo(user.getId());
				assertThat(saved.getUser()).isEqualTo(username);
			});
		assertThat(userRepository.findByUser(username)).isPresent();
	}

}
