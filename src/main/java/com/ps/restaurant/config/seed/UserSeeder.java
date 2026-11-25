package com.ps.restaurant.config.seed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.model.Role;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.RoleRepository;
import com.ps.restaurant.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1) 
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.user.password}")
    private String seedPassword;

    @Override
    @Transactional
    public void run(String... args) {
        createRoles();
        createUsers();
    }

    private void createRoles() {
        createRole("admin");
        createRole("waiter");
        createRole("cleaner");
        createRole("manager");
        createRole("cocker");
    }

    private void createRole(String name) {
        if (roleRepository.existsByName(name)) {
            log.debug("Seed: role {} already exists", name);
            return;
        }

        Role r = new Role();
        r.setName(name);
        r.setActive(true);
        roleRepository.save(r);
        log.info("Seed: role {} created", name);
    }

    private void createUsers() {
        createUser("admin", "Daniel Saldaña", "Sistema", "admin@psrestaurant.com", "admin");
        createUser("poncho", "Diego Ochoa", "Ochoa", "poncho@psrestaurant.com", "waiter");
        createUser("ana", "Ana Perez", "López", "ana@psrestaurant.com", "manager");
        createUser("carlos", "Carlos Mendez", "Méndez", "carlos@psrestaurant.com", "cocker");
    }

    private void createUser(String username, String name, String surName, String email, String roleName) {
        if (userRepository.existsByEmail(email) || userRepository.findByUser(username).isPresent()) {
            log.debug("Seed: user {} already exists", email);
            return;
        }
        User u = new User();
        u.setUser(username);
        u.setFullName(name);
        u.setEmail(email);
        u.setRole(roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Seed: role " + roleName + " was not found")));
        u.setActive(true);
        u.setPassword(passwordEncoder.encode(seedPassword));
        userRepository.save(u);
        log.info("Seed: user {} created", username);
    }
}
