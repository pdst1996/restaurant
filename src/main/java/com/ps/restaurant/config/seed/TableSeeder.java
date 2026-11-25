
package com.ps.restaurant.config.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.model.Floor;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.model.TableShape;
import com.ps.restaurant.model.TableStatus;
import com.ps.restaurant.repository.FloorRepository;
import com.ps.restaurant.repository.TableRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(4)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class TableSeeder implements CommandLineRunner {

    private final TableRepository tableRepository;
    private final FloorRepository floorRepository;

    @Override
    @Transactional
    public void run(String... args) {
        createTables();
    }

    private void createTables() {
        Floor firstFloor = getFloor("Piso 1");
        Floor rooftop = getFloor("Azotea");
        Floor backroom = getFloor("Backroom");

        // ==================== PISO 1 ====================

        createTable("P1-01", 4, TableShape.RECTANGULAR, firstFloor,
                100, 100, 80, 80, 0);

        createTable("P1-02", 4, TableShape.CIRCULAR, firstFloor,
                250, 100, 90, 90, 0);

        createTable("P1-03", 6, TableShape.RECTANGULAR, firstFloor,
                100, 300, 120, 80, 0);

        createTable("P1-04", 2, TableShape.CIRCULAR, firstFloor,
                250, 300, 70, 70, 0);

        createTable("P1-05", 8, TableShape.RECTANGULAR, firstFloor,
                550, 500, 160, 90, 0);

        createTable("P1-06", 4, TableShape.CIRCULAR, firstFloor,
                650, 150, 90, 90, 0);

        createTable("P1-07", 6, TableShape.RECTANGULAR, firstFloor,
                650, 300, 130, 80, 90);

        // ==================== AZOTEA ====================

        createTable("AZ-01", 4, TableShape.CIRCULAR, rooftop,
                150, 150, 100, 100, 0);

        createTable("AZ-02", 4, TableShape.CIRCULAR, rooftop,
                350, 150, 100, 100, 0);

        createTable("AZ-03", 6, TableShape.RECTANGULAR, rooftop,
                550, 150, 130, 80, 0);

        createTable("AZ-04", 2, TableShape.CIRCULAR, rooftop,
                750, 150, 80, 80, 0);

        createTable("AZ-05", 8, TableShape.RECTANGULAR, rooftop,
                950, 150, 160, 90, 0);

        createTable("AZ-06", 4, TableShape.CIRCULAR, rooftop,
                150, 450, 100, 100, 0);

        // ==================== BACKROOM ====================

        createTable("BR-01", 2, TableShape.RECTANGULAR, backroom,
                100, 150, 150, 80, 0);

        createTable("BR-02", 4, TableShape.RECTANGULAR, backroom,
                350, 150, 150, 80, 0);

        createTable("BR-03", 4, TableShape.RECTANGULAR, backroom,
                600, 150, 150, 80, 0);

        createTable("BR-04", 2, TableShape.CIRCULAR, backroom,
                100, 350, 80, 80, 0);

        createTable("BR-05", 6, TableShape.RECTANGULAR, backroom,
                350, 350, 160, 90, 0);

        createTable("BR-06", 4, TableShape.CIRCULAR, backroom,
                650, 350, 100, 100, 0);
    }

    private Floor getFloor(String floorName) {
        return floorRepository.findFirstByNameOrderByIdAsc(floorName)
                .orElseThrow(() -> new IllegalStateException(
                        "Seed: floor " + floorName + " not found. Run FloorPlanSeeder first."
                ));
    }

    private void createTable(
            String tableName,
            int capacity,
            TableShape shape,
            Floor floor,
            int x,
            int y,
            int width,
            int height,
            int rotation
    ) {
        if (tableRepository.existsByTableName(tableName)) {
            log.debug("Seed: table {} already exists", tableName);
            return;
        }

        Table table = new Table();

        table.setTableName(tableName);
        table.setCapacity(capacity);
        table.setShape(shape);
        table.setTableStatus(TableStatus.FREE);

        table.setFloor(floor);

        table.setX(x);
        table.setY(y);
        table.setWidth(width);
        table.setHeight(height);
        table.setRotation(rotation);

        tableRepository.save(table);

        log.info("Seed: table {} created on floor {}", tableName, floor.getName());
    }

}
