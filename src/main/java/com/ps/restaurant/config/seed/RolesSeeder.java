package com.ps.restaurant.config.seed;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.ps.restaurant.config.Permissions;
import com.ps.restaurant.model.Role;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.RoleRepository;
import com.ps.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@Order(0)
@RequiredArgsConstructor
public class RolesSeeder implements CommandLineRunner {
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    @Value("${app.bootstrap.admin-email:admin@psrestaurant.com}")
    private String adminEmail;
    @Value("${app.bootstrap.admin-password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        for (String name : List.of("admin", "gerente", "mesero", "cocina", "cajero", "manager", "waiter", "cocker", "cleaner")) {
            Role role = roles.findByName(name).orElseGet(() -> {
                Role created = new Role();
                created.setName(name);
                created.setActive(true);
                return created;
            });
            if (!role.isPermissionsInitialized()) {
                role.getPermissions().addAll(Permissions.defaults(name));
                role.setPermissionsInitialized(true);
                roles.save(role);
            }
        }
        if (adminPassword.isBlank() || users.existsByRoleNameAndActiveTrue("admin")) return;
        if (adminPassword.length() < 12 || adminPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Bootstrap password must have at least 12 characters and at most 72 UTF-8 bytes");
        }
        Role role = roles.findByName("admin").orElseThrow();
        if (!role.isActive() || users.findByUserOrEmail(adminEmail).isPresent()
                || users.findByUserOrEmail("admin").isPresent()) {
            throw new IllegalStateException("Cannot bootstrap admin: inactive role or identifier already in use");
        }
        User admin = new User();
        admin.setUser("admin");
        admin.setEmail(adminEmail);
        admin.setFullName("Administrador");
        admin.setRole(role);
        admin.setActive(true);
        admin.setPassword(encoder.encode(adminPassword));
        users.save(admin);
    }
}
