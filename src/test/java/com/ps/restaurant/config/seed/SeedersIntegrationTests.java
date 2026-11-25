package com.ps.restaurant.config.seed;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.ps.restaurant.model.*;
import com.ps.restaurant.repository.*;

/** Starts all seeders against an isolated database and verifies a second run is safe. */
@SpringBootTest(properties = {
        "app.seed.enabled=true",
        "app.seed.user.password=test-only-password",
        "spring.datasource.url=jdbc:h2:mem:all-seeders-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
@ActiveProfiles("test")
@Transactional
class SeedersIntegrationTests {
    @Autowired private RolesSeeder rolesSeeder;
    @Autowired private UserSeeder userSeeder;
    @Autowired private FloorPlanSeeder floorSeeder;
    @Autowired private TableSeeder tableSeeder;
    @Autowired private ItemsSeeder itemsSeeder;
    @Autowired private OrderSeeder orderSeeder;
    @Autowired private FloorRepository floors;
    @Autowired private ElementTypeRepository types;
    @Autowired private ElementPlanRepository elements;
    @Autowired private TableRepository tables;
    @Autowired private ItemsRepository items;
    @Autowired private OrderRepository orders;
    @Autowired private RoleRepository roles;
    @Autowired private UserRepository users;
    @Autowired private CategoryRepository categories;
    @Autowired private ItemAttributesRepository attributes;
    @Autowired private ItemSizeRepository sizes;
    @Autowired private AllergyRepository allergies;

    @Test
    void seedsEveryModelWithValidRelationshipsAndDoesNotDuplicateOnRerun() {
        assertThat(floors.count()).isEqualTo(3);
        assertThat(types.count()).isEqualTo(6);
        assertThat(elements.count()).isEqualTo(30);
        assertThat(tables.count()).isEqualTo(19);
        assertThat(items.count()).isEqualTo(32);
        assertThat(orders.count()).isEqualTo(20);
        assertThat(roles.findAll()).allMatch(Role::isActive);
        assertThat(users.findAll()).allMatch(User::isActive);
        assertThat(types.findByType("wall").orElseThrow().getLabel()).containsEntry("en", "Wall").containsEntry("es", "Pared");
        assertThat(tables.findAll()).allSatisfy(table -> assertThat(table.getFloor().getId()).isPositive());
        assertThat(elements.findAll()).allSatisfy(element -> {
            assertThat(element.getFloor().getId()).isPositive();
            assertThat(element.getElementType().getId()).isPositive();
        });
        assertThat(items.findAll()).allSatisfy(item -> assertThat(item.getSizes()).isNotEmpty());
        assertThat(items.findAll()).filteredOn(item -> item.getSku().startsWith("EXTRA-"))
                .allSatisfy(item -> assertThat(item.getSizes()).extracting(ItemSize::getSizeKey).containsExactly("FULL"));
        List<Long> before = counts();

        rolesSeeder.run();
        userSeeder.run();
        floorSeeder.run();
        tableSeeder.run();
        itemsSeeder.run();
        orderSeeder.run();

        assertThat(counts()).isEqualTo(before);
        assertThat(items.findAll()).allSatisfy(item -> assertThat(item.getSizes()).doesNotHaveDuplicates());
    }

    private List<Long> counts() {
        return List.of(floors.count(), types.count(), elements.count(), tables.count(), items.count(), orders.count(),
                roles.count(), users.count(), categories.count(), attributes.count(), sizes.count(), allergies.count());
    }
}
