package com.ps.restaurant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.model.Role;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.UserRepository;

import jakarta.persistence.EntityManager;

/** Verifies application startup and user persistence against the isolated test database. */
@SpringBootTest
@ActiveProfiles("test")
class RestaurantApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager entityManager;

	/** Passes when Spring can initialize the complete application context. */
	@Test
	void contextLoads() {
	}

	/** Persists a user and role, clears the persistence cache, and verifies email and username lookups. */
	@Test
	@Transactional
	void savesAndFindsUserByEmailAndUsername() {
		String username = "test-" + UUID.randomUUID();
		Role role = new Role();
		role.setName("role-" + username);
		role.setActive(true);
		entityManager.persist(role);
		User user = new User();
		user.setEmail(username + "@example.com");
		user.setUser(username);
		user.setPassword("test-password");
		user.setFullName("Test User");
		user.setRole(role);
		user.setActive(true);
		userRepository.saveAndFlush(user);
		entityManager.clear();

		assertThat(userRepository.findByEmail(user.getEmail()))
			.isPresent()
			.get()
			.satisfies(saved -> {
				assertThat(saved.getId()).isEqualTo(user.getId());
				assertThat(saved.getUser()).isEqualTo(username);
				assertThat(saved.getFullName()).isEqualTo("Test User");
				assertThat(saved.getRole().getId()).isEqualTo(role.getId());
			});
		assertThat(userRepository.findByUser(username)).isPresent();
	}

}
