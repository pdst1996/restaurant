package com.ps.restaurant.config.seed;

import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.ps.restaurant.model.ElementType;
import com.ps.restaurant.model.Floor;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.model.TableShape;
import com.ps.restaurant.model.TableStatus;
import com.ps.restaurant.model.Element;
import com.ps.restaurant.repository.ElementPlanRepository;
import com.ps.restaurant.repository.ElementTypeRepository;
import com.ps.restaurant.repository.FloorRepository;
import com.ps.restaurant.repository.TableRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@org.springframework.core.annotation.Order(2)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class FloorPlanSeeder implements CommandLineRunner {

    private final FloorRepository floorRepository;
    private final ElementTypeRepository elementTypeRepository;
    private final ElementPlanRepository elementRepository;
     private final TableRepository tableRepository;

    @Override
    @Transactional
    public void run(String... args) {
        createFloors();
        createElementTypes();
        createElements();
        createTables();
    }

    public void createFloors() {
        createFloor("Piso 1", 1200, 800, 1);
        createFloor("Azotea", 1200, 800, 2);
        createFloor("Backroom", 1200, 800, 3);
    }

    public void createFloor(String name, Integer width, Integer height, Integer sort_order) {
        if (floorRepository.existsByName(name)) {
            log.debug("Seed: floor {} already exists", name);
            return;
        }

        Floor f = new Floor();
        f.setName(name);
        f.setWidth(width);
        f.setHeight(height);
        f.setSort_order(sort_order);
        floorRepository.save(f);
        log.info("Seed: floor {} created", name);
    }

    public void createElementTypes() {
        createElementType("wall", "Pared", "Wall", 200, 12, "#475569", "#334155", false);
        createElementType("door", "Puerta", "Door", 60, 10, "#fde68a", "#d97706", false);
        createElementType("column", "Columna", "Column", 40, 40, "#94a3b8", "#64748b", true);
        createElementType("bar", "Barra", "Bar", 200, 50, "#d6b48a", "#92400e", false);
        createElementType("plant", "Planta", "Plant", 30, 30, "#bbf7d0", "#16a34a", true);
        createElementType("text", "Texto", "Text", 200, 12, null, null, false);
    }

    public void createElementType(String type, String es, String en, Integer width, Integer height, String fill,
            String stroke, boolean round) {
        if (elementTypeRepository.existsByType(type)) {
            log.debug("Seed: element type {} already exists", type);
            return;
        }

        ElementType e = new ElementType();
        e.setType(type);
        e.setLabel(Map.of("en", en, "es", es));
        e.setWidth(width);
        e.setHeight(height);
        e.setFill(fill);
        e.setStroke(stroke);
        e.setRound(round);
        elementTypeRepository.save(e);
        log.info("Seed: element type {} created", type);
    }

    public void createElements() {
        // ==================== PISO 1 ====================

        // Walls
        createElement("Piso 1", "wall", 0, 0, 1200, 12, 0, null);
        createElement("Piso 1", "wall", 0, 0, 12, 850, 0, null);
        createElement("Piso 1", "wall", 1188, 0, 12, 850, 0, null);
        createElement("Piso 1", "wall", 0, 838, 1200, 12, 0, null);
        createElement("Piso 1", "wall", 400, 0, 12, 400, 0, null);
        createElement("Piso 1", "wall", 800, 0, 12, 400, 0, null);

        // Doors
        createElement("Piso 1", "door", 560, 780, 80, 10, 0, "Entrada");
        createElement("Piso 1", "door", 400, 300, 80, 10, 90, "Cocina");
        createElement("Piso 1", "door", 800, 300, 80, 10, 90, "Baños");

        // Furniture and areas
        createElement("Piso 1", "bar", 900, 100, 200, 50, 90, "Barra");
        createElement("Piso 1", "plant", 50, 50, 30, 30, 0, null);
        createElement("Piso 1", "plant", 1100, 750, 30, 30, 0, null);
        createElement("Piso 1", "text", 500, 50, 200, 30, 0, "Comedor principal");

        // ==================== AZOTEA ====================

        createElement("Azotea", "wall", 0, 0, 1200, 12, 0, null);
        createElement("Azotea", "wall", 0, 0, 12, 850, 0, null);
        createElement("Azotea", "wall", 1188, 0, 12, 850, 0, null);
        createElement("Azotea", "wall", 0, 838, 1200, 12, 0, null);

        createElement("Azotea", "door", 550, 780, 100, 10, 0, "Acceso");
        createElement("Azotea", "plant", 50, 50, 50, 50, 0, null);
        createElement("Azotea", "plant", 1100, 50, 50, 50, 0, null);
        createElement("Azotea", "plant", 50, 750, 50, 50, 0, null);
        createElement("Azotea", "plant", 1100, 750, 50, 50, 0, null);
        createElement("Azotea", "bar", 900, 450, 200, 60, 0, "Bar exterior");
        createElement("Azotea", "text", 400, 50, 300, 30, 0, "Terraza panorámica");

        // ==================== BACKROOM ====================

        createElement("Backroom", "wall", 0, 0, 1000, 12, 0, null);
        createElement("Backroom", "wall", 0, 0, 12, 700, 0, null);
        createElement("Backroom", "wall", 988, 0, 12, 700, 0, null);
        createElement("Backroom", "wall", 0, 688, 1000, 12, 0, null);

        createElement("Backroom", "door", 450, 680, 100, 10, 0, "Acceso");
        createElement("Backroom", "text", 50, 50, 200, 30, 0, "Personal");
        createElement("Backroom", "text", 400, 50, 200, 30, 0, "Almacén");
        createElement("Backroom", "bar", 100, 350, 300, 60, 0, "Estantería");
        createElement("Backroom", "plant", 900, 600, 30, 30, 0, null);
    }

    public void createElement(String floorName, String elementType, Integer x, Integer y, Integer w, Integer h,
            Integer r, String label) {
        Floor floor = floorRepository.findFirstByNameOrderByIdAsc(floorName)
                .orElseThrow(() -> new IllegalStateException("Seed: floor " + floorName + " was not found"));
        ElementType type = elementTypeRepository.findByType(elementType)
                .orElseThrow(() -> new IllegalStateException("Seed: element type " + elementType + " was not found"));
        if (elementRepository.existsByFloorIdAndElementTypeTypeAndXAndY(floor.getId(), elementType, x, y))
            return;
        Element element = new Element();
        element.setFloor(floor);
        element.setElementType(type);
        element.setX(x);
        element.setY(y);
        element.setWidth(w);
        element.setHeight(h);
        element.setRotation(r);
        element.setLabel(label);
        elementRepository.save(element);
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
