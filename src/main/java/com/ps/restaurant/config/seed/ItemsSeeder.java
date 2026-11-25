package com.ps.restaurant.config.seed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ps.restaurant.model.ItemCategory;
import com.ps.restaurant.model.Item;
import com.ps.restaurant.model.ItemAttribute;
import com.ps.restaurant.model.ItemSize;
import com.ps.restaurant.repository.CategoryRepository;
import com.ps.restaurant.repository.ItemAttributesRepository;
import com.ps.restaurant.repository.ItemSizeRepository;
import com.ps.restaurant.repository.ItemsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(3)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ItemsSeeder implements CommandLineRunner {

    private final ItemSizeRepository itemsSizeRepository;
    private final ItemAttributesRepository itemsAttrRepository;
    private final ItemsRepository itemsRepository;
    private final CategoryRepository categoryRepository;

    private static BigDecimal bd(int v) {
        return BigDecimal.valueOf(v);
    }

    List<ItemSize> FULL_HALF = new ArrayList<ItemSize>();
    List<ItemSize> FULL = new ArrayList<ItemSize>();
    List<ItemSize> SMALL_MEDIUM_BIG = new ArrayList<ItemSize>();

    List<ItemAttribute> NO_ONION_CHEESE = new ArrayList<>();
    List<ItemAttribute> NO_CUTLERY = new ArrayList<>();
    List<ItemAttribute> NO_SPICY = new ArrayList<>();
    List<ItemAttribute> NO_SAUCES = new ArrayList<>();
    List<ItemAttribute> NO_VEGETABLES = new ArrayList<>();
    List<ItemAttribute> NO_TOPPINGS = new ArrayList<>();
    List<ItemAttribute> NO_PROTEINS = new ArrayList<>();
    List<ItemAttribute> NO_SEASONINGS = new ArrayList<>();
    List<ItemAttribute> ALL_REMOVABLE_INGREDIENTS = new ArrayList<>();

    @Override
    @Transactional
    public void run(String... args) {
        createItemCategories();
        createItemSizes();
        createItemAttributes();
        createItems();
    }

    private void createItemCategories() {
        createItemCategorie("BEVERAGES", "Beverages", "Bebidas");
        createItemCategorie("BREAKFAST", "Breakfast", "Desayunos");
        createItemCategorie("MEAL", "Meal", "Comida");
        createItemCategorie("DESSERT", "Dessert", "Postre");
        createItemCategorie("EXTRA", "Extra", "Extra");
    }

    private void createItemCategorie(String categoryKey, String en, String es) {
        if (!categoryRepository.findByCategoryKey(categoryKey).stream().findFirst().isEmpty()) {
            log.debug("Seed: category {} already exists", categoryKey);
            return;
        }

        ItemCategory c = new ItemCategory();
        c.setDisplayName(Map.of("en", en, "es", es));
        c.setCategoryKey(categoryKey);
        categoryRepository.save(c);
        log.info("Seed: category {} created", categoryKey);
    }

    private void createItemAttributes() {
        createItemAttribute("N_ONION", "No Onion", "Sin cebolla");
        createItemAttribute("N_CHEESE", "No Cheese", "Sin queso");
        createItemAttribute("N_CUTLERY", "No Cutlery", "Sin cubiertos");
        createItemAttribute("N_SPICY", "Not Spicy", "Sin picante");
        createItemAttribute("N_SOUR_CREAM", "No Sour Cream", "Sin crema agria");
        createItemAttribute("N_TOMATO", "No Tomato", "Sin tomate");
        createItemAttribute("N_MAYO", "No Mayonnaise", "Sin mayonesa");
        createItemAttribute("N_KETCHUP", "No Ketchup", "Sin kétchup");

        // Vegetables
        createItemAttribute("N_LETTUCE", "No Lettuce", "Sin lechuga");
        createItemAttribute("N_PICKLES", "No Pickles", "Sin pepinillos");
        createItemAttribute("N_JALAPENOS", "No Jalapeños", "Sin jalapeños");
        createItemAttribute("N_CILANTRO", "No Cilantro", "Sin cilantro");
        createItemAttribute("N_AVOCADO", "No Avocado", "Sin aguacate");
        createItemAttribute("N_MUSHROOMS", "No Mushrooms", "Sin champiñones");
        createItemAttribute("N_BELL_PEPPER", "No Bell Pepper", "Sin pimiento");
        createItemAttribute("N_OLIVES", "No Olives", "Sin aceitunas");
        createItemAttribute("N_CORN", "No Corn", "Sin maíz");

        // Sauces and dressings
        createItemAttribute("N_MUSTARD", "No Mustard", "Sin mostaza");
        createItemAttribute("N_BBQ_SAUCE", "No BBQ Sauce", "Sin salsa BBQ");
        createItemAttribute("N_HOT_SAUCE", "No Hot Sauce", "Sin salsa picante");
        createItemAttribute("N_RANCH", "No Ranch Dressing", "Sin aderezo ranch");
        createItemAttribute("N_GUACAMOLE", "No Guacamole", "Sin guacamole");
        createItemAttribute("N_DRESSING", "No Dressing", "Sin aderezo");

        // Dairy and toppings
        createItemAttribute("N_BACON", "No Bacon", "Sin tocino");
        createItemAttribute("N_EGG", "No Egg", "Sin huevo");
        createItemAttribute("N_CREAM", "No Cream", "Sin crema");
        createItemAttribute("N_PARMESAN", "No Parmesan", "Sin queso parmesano");

        // Proteins
        createItemAttribute("N_MEAT", "No Meat", "Sin carne");
        createItemAttribute("N_CHICKEN", "No Chicken", "Sin pollo");
        createItemAttribute("N_BEEF", "No Beef", "Sin res");
        createItemAttribute("N_SHRIMP", "No Shrimp", "Sin camarón");

        // Other common ingredients
        createItemAttribute("N_GARLIC", "No Garlic", "Sin ajo");
        createItemAttribute("N_LIME", "No Lime", "Sin limón");
        createItemAttribute("N_SALT", "No Salt", "Sin sal");
        createItemAttribute("N_PEPPER", "No Black Pepper", "Sin pimienta");
    }

    private void createItemAttribute(String attrKey, String en, String es) {
        if (itemsAttrRepository.existsByAttributeKey(attrKey)) {
            log.debug("Seed: item attr {} already exists", attrKey);
            return;
        }

        ItemAttribute i = new ItemAttribute();
        i.setAttributeKey(attrKey);
        i.setDisplayName(Map.of("en", en, "es", es));
        itemsAttrRepository.save(i);
        log.info("Seed: attribute {} created", attrKey);
    }

    private void createItemSizes() {
        createItemSize("BIG", "Big", "Grande");
        createItemSize("SMALL", "Small", "Pequeño");
        createItemSize("MEDIUM", "Medium", "Mediano");
        createItemSize("FULL", "Full", "Orden");
        createItemSize("HALF", "Half", "Media orden");
    }

    private void createItemSize(String sizeKey, String en, String es) {
        if (itemsSizeRepository.existsBySizeKey(sizeKey)) {
            log.debug("Seed: item size {} already exists", sizeKey);
            return;
        }

        ItemSize i = new ItemSize();
        i.setSizeKey(sizeKey);
        i.setDisplayName(Map.of("en", en, "es", es));
        itemsSizeRepository.save(i);
        log.info("Seed: user {} created", sizeKey);
    }

    @SuppressWarnings("null")
    private void createItems() {
        fillItemSizeVariables();
        fillItemAttrVariables();

        createItem("CHIL-VER", "Green chilaquiles", "Chilaquiles Verdes",
                "Totopos de masa de maíz bañados en salsa verde, con crema y queso",
                "Breakfast", FULL_HALF, bd(100), "2000000000015",
                new ArrayList<>(List.of(NO_ONION_CHEESE, NO_SAUCES, NO_SPICY)
                        .stream().flatMap(List::stream).distinct().toList()), "https://patijinich.com/es/wp-content/uploads/sites/3/2017/07/207-chilaquiles-verdes.jpg");

        createItem("CHIL-ROJ", "Red chilaquiles", "Chilaquiles Rojos",
                "Totopos de masa de maíz bañados en salsa roja casera",
                "Breakfast", FULL_HALF, bd(100), "2000000000022",
                new ArrayList<>(List.of(NO_ONION_CHEESE, NO_SAUCES, NO_SPICY)
                        .stream().flatMap(List::stream).distinct().toList()), "https://cdn7.kiwilimon.com/recetaimagen/41388/640x640/56367.jpg.webp");

        createItem("CHIL-MIX", "Mixed chilaquiles", "Chilaquiles Mixtos",
                "Totopos bañados en una combinación de salsa verde y roja",
                "Breakfast", FULL_HALF, bd(110), "2000000000039",
                new ArrayList<>(List.of(NO_ONION_CHEESE, NO_SAUCES, NO_SPICY)
                        .stream().flatMap(List::stream).distinct().toList()), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSUHFp_2C9AIoYzYD_TBUdZA7uCcf7ztx8LsiXE_tOcuuQINYQ2cZPuU8I&s=10");

        createItem("CHIL-POL", "Chicken chilaquiles", "Chilaquiles con Pollo",
                "Chilaquiles con pollo deshebrado, crema, queso y cebolla",
                "Breakfast", FULL_HALF, bd(130), "2000000000046",
                new ArrayList<>(List.of(NO_ONION_CHEESE, NO_SAUCES, NO_SPICY, NO_PROTEINS)
                        .stream().flatMap(List::stream).distinct().toList()), "https://enmicasa.com/wp-content/uploads/2017/10/chilaquiles-de-huevo-con-pollo.jpg");

        createItem("CHIL-ARR", "Skirt steak chilaquiles", "Chilaquiles con Arrachera",
                "Chilaquiles acompañados de arrachera a la parrilla",
                "Breakfast", FULL_HALF, bd(160), "2000000000053",
                new ArrayList<>(List.of(NO_ONION_CHEESE, NO_SAUCES, NO_SPICY, NO_PROTEINS)
                        .stream().flatMap(List::stream).distinct().toList()), "https://i.pinimg.com/736x/aa/a6/77/aaa67797bbccf2b1b29553ac219483e7.jpg");

        createItem("HUE-RAN", "Ranchero eggs", "Huevos Rancheros",
                "Huevos estrellados sobre tortilla bañados en salsa ranchera",
                "Breakfast", FULL_HALF, bd(120), "2000000000060",
                new ArrayList<>(List.of(NO_VEGETABLES, NO_SPICY, NO_TOPPINGS)
                        .stream().flatMap(List::stream).distinct().toList()), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT8V_xB_KdZWBdMnQFITjoL1WkkiKn5yKJxK4dH7C0Jfw&s=10");

        createItem("HUE-JAM", "Eggs with ham", "Huevos con Jamón",
                "Huevos revueltos con jamón, frijoles y tortillas",
                "Breakfast", FULL_HALF, bd(110), "2000000000077",
                new ArrayList<>(List.of(NO_TOPPINGS, NO_PROTEINS)
                        .stream().flatMap(List::stream).distinct().toList()), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRJqXEjOO4TuKPsBojVSEiNQlH_TDoqU5zG4msITMP51Q&s=10");

        createItem("HUE-MEX", "Mexican-style eggs", "Huevos a la Mexicana",
                "Huevos revueltos con jitomate, cebolla y chile",
                "Breakfast", FULL_HALF, bd(110), "2000000000084",
                new ArrayList<>(List.of(NO_VEGETABLES, NO_SPICY, NO_TOPPINGS)
                        .stream().flatMap(List::stream).distinct().toList()), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQXw9LA4IE1WNy1AyXCy7UVUDpDLxvD-ndiYXuHuO4Axg&s=10");

        createItem("HUE-CHO", "Eggs with chorizo", "Huevos con Chorizo",
                "Huevos revueltos con chorizo mexicano y frijoles",
                "Breakfast", FULL_HALF, bd(120), "2000000000091",
                new ArrayList<>(List.of(NO_TOPPINGS, NO_PROTEINS, NO_SPICY)
                        .stream().flatMap(List::stream).distinct().toList()), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQlw0s1afSiUB7tbsX12X96kfkCLjl1Bklfl1NujHp9Jg&s=10");

        createItem("MOLLETES", "Molletes", "Molletes",
                "Pan artesanal con frijoles, queso gratinado y pico de gallo",
                "Breakfast", FULL_HALF, bd(90), "2000000000107",
                new ArrayList<>(List.of(NO_ONION_CHEESE, NO_VEGETABLES, NO_TOPPINGS)
                        .stream().flatMap(List::stream).distinct().toList()), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT5Bxi5fkeHaOl6GjM9r8R2Q9M-HKJKXgeKjhe2OFKwHw&s=10");

        createItem("PAN-DULCE", "Sweet bread", "Pan Dulce",
                "Pieza de pan dulce del día",
                "Dessert", FULL, bd(60), "2000000000114",
                new ArrayList<>(), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQyc5A89BzlqzZph6FQa3-zo0hY8E3_vpo6VVnchttzgg&s=10");

       createItem("CAFE-LAT", "Latte", "Café Latte",
        "Espresso con leche vaporizada",
        "Beverages", SMALL_MEDIUM_BIG, bd(40), "2000000000145",
        new ArrayList<>(), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTBLYL59AoTqhoyGT8hs_WUnu4DXJky_X9E4vu6rjunZg&s=10");

createItem("CAFE-AME", "Americano", "Café Americano",
        "Café de grano recién molido",
        "Beverages", SMALL_MEDIUM_BIG, bd(35), "2000000000152",
        new ArrayList<>(), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR0zK5O2iHm85m3aSw87hxPS7M72Qbfvo-M8I-2h6I6VA&s=10");

createItem("JUGO-NAR", "Orange juice", "Jugo de Naranja",
        "Jugo natural de naranja",
        "Beverages", SMALL_MEDIUM_BIG, bd(45), "2000000000169",
        new ArrayList<>(), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQXKfrzV951Vp0K7EF-i9puJ-LkcFpd0qrNooqMJ9un0g&s=10");

        createItem("ENCH-SUI", "Swiss enchiladas", "Enchiladas Suizas",
                "Enchiladas de pollo gratinadas con salsa verde y crema",
                "Meal", FULL_HALF, bd(150), "2000000000176",
                new ArrayList<>(List.of(NO_PROTEINS, NO_TOPPINGS, NO_SAUCES, NO_SPICY)
                        .stream().flatMap(List::stream).distinct().toList()), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTijYafrKThrYxS8HF62W3d2bFHwbC8WrJku4y7IEduPA&s=10");

        createItem("FLAN", "Flan", "Flan Napolitano",
                "Flan casero con caramelo",
                "Dessert", FULL, bd(55), "2000000000183",
                new ArrayList<>(), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTeCmv8swomIVTF_nqCPE4Ig5D7rk9yOzjUv5Qt5HeT6g&s=10");

        createItem("EXTRA-QUESO", "Extra cheese", "Queso extra",
                "Porción adicional de queso",
                "Extra", FULL, bd(20), "2000000000121",
                new ArrayList<>(), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSasfgW_uPaRvDJj-2QNXw-AmS208EbNKixhxZquMP4bQ&s=10");

        createItem("EXTRA-SALSA", "Extra salsa", "Salsa extra",
                "Porción adicional de salsa",
                "Extra", FULL, bd(20), "2000000000138",
                new ArrayList<>(), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTECFU7BUgj6dGop0YZI4PnNNgj8QrHu-RSgfUFb-N1Iw&s=10");

                createItem("EXTRA-AGUACATE", "Extra avocado", "Aguacate extra",
        "Porción adicional de aguacate",
        "Extra", FULL, bd(25), "2000000000190",
        new ArrayList<>(), null);

createItem("EXTRA-CREMA", "Extra cream", "Crema extra",
        "Porción adicional de crema",
        "Extra", FULL, bd(15), "2000000000206",
        new ArrayList<>(), null);

createItem("EXTRA-TOCINO", "Extra bacon", "Tocino extra",
        "Porción adicional de tocino",
        "Extra", FULL, bd(25), "2000000000213",
        new ArrayList<>(), null);

createItem("EXTRA-HUEVO", "Extra egg", "Huevo extra",
        "Un huevo adicional al gusto",
        "Extra", FULL, bd(20), "2000000000220",
        new ArrayList<>(), null);

createItem("EXTRA-FRIJOLES", "Extra beans", "Frijoles extra",
        "Porción adicional de frijoles refritos",
        "Extra", FULL, bd(15), "2000000000237",
        new ArrayList<>(), null);

createItem("EXTRA-TORTILLAS", "Extra tortillas", "Tortillas extra",
        "Orden adicional de tortillas",
        "Extra", FULL, bd(10), "2000000000244",
        new ArrayList<>(), null);

createItem("EXTRA-POLLO", "Extra chicken", "Pollo extra",
        "Porción adicional de pollo deshebrado",
        "Extra", FULL, bd(35), "2000000000251",
        new ArrayList<>(), null);

createItem("EXTRA-ARRACHERA", "Extra skirt steak", "Arrachera extra",
        "Porción adicional de arrachera a la parrilla",
        "Extra", FULL, bd(50), "2000000000268",
        new ArrayList<>(), null);

createItem("EXTRA-CHORIZO", "Extra chorizo", "Chorizo extra",
        "Porción adicional de chorizo mexicano",
        "Extra", FULL, bd(30), "2000000000275",
        new ArrayList<>(), null);

createItem("EXTRA-JAMON", "Extra ham", "Jamón extra",
        "Porción adicional de jamón",
        "Extra", FULL, bd(20), "2000000000282",
        new ArrayList<>(), null);

createItem("EXTRA-TOTOPOS", "Extra tortilla chips", "Totopos extra",
        "Porción adicional de totopos",
        "Extra", FULL, bd(15), "2000000000299",
        new ArrayList<>(), null);

createItem("EXTRA-PICO", "Extra pico de gallo", "Pico de gallo extra",
        "Porción adicional de pico de gallo",
        "Extra", FULL, bd(15), "2000000000305",
        new ArrayList<>(), null);

createItem("EXTRA-JALAPENOS", "Extra grilled peppers", "Chiles toreados extra",
        "Porción adicional de chiles toreados",
        "Extra", FULL, bd(10), "2000000000312",
        new ArrayList<>(), null);

createItem("EXTRA-SHOT", "Extra espresso shot", "Shot de espresso extra",
        "Shot adicional de espresso para tu café",
        "Extra", FULL, bd(15), "2000000000329",
        new ArrayList<>(), null);
    }

    private void createItem(String sku, String enDisplayName, String esDisplayName, String description,
            String categoryStr, List<ItemSize> sizes, BigDecimal price, String barcode,
            List<ItemAttribute> attributes, String img) {
        if (itemsRepository.existsBySku(sku)) {
            log.debug("Seed: item {} already exists", sku);
            return;
        }

        String categoryKey = switch (categoryStr) {
            case "Breakfast" -> "BREAKFAST";
            case "Beverages" -> "BEVERAGES";
            case "Meal" -> "MEAL";
            case "Dessert" -> "DESSERT";
            case "Extra" -> "EXTRA";
            default -> throw new IllegalStateException("Seed: unknown category " + categoryStr);
        };
        ItemCategory category = categoryRepository.findByCategoryKey(categoryKey)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Seed: category not found: " + categoryStr));

        Item item = new Item();
        item.setSku(sku);
        item.setDisplayName(Map.of("en", enDisplayName, "es", esDisplayName));
        item.setDescription(description);
        item.setCategory(category);
        item.setSizes(new ArrayList<>(sizes));
        item.setPrice(price);
        item.setAvailable(true);
        item.setBarcodes(Map.of("EAN-13", barcode));
        item.setAvailableAttributes(attributes);
        item.setImg(img);
        itemsRepository.save(item);
        log.info("Seed: item {} created", sku);
    }

    private void fillItemSizeVariables() {
        FULL_HALF.clear();
        FULL.clear();
        SMALL_MEDIUM_BIG.clear();
        FULL_HALF.add(itemsSizeRepository.getBySizeKey("FULL").stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Seed: item size not found: " + "FULL")));
        FULL.addAll(FULL_HALF);
        FULL_HALF.add(itemsSizeRepository.getBySizeKey("HALF").stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Seed: item size not found: " + "HALF")));
        SMALL_MEDIUM_BIG.add(itemsSizeRepository.getBySizeKey("SMALL").stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Seed: item size not found: " + "SMALL")));
        SMALL_MEDIUM_BIG.add(itemsSizeRepository.getBySizeKey("MEDIUM").stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Seed: item size not found: " + "MEDIUM")));
        SMALL_MEDIUM_BIG.add(itemsSizeRepository.getBySizeKey("BIG").stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Seed: item size not found: " + "BIG")));
    }

    private void fillItemAttrVariables() {
        for (List<ItemAttribute> group : List.of(NO_ONION_CHEESE, NO_CUTLERY, NO_SPICY, NO_SAUCES,
                NO_VEGETABLES, NO_TOPPINGS, NO_PROTEINS, NO_SEASONINGS, ALL_REMOVABLE_INGREDIENTS)) {
            group.clear();
        }

        // No onion and cheese
        NO_ONION_CHEESE.add(getAttribute("N_ONION"));
        NO_ONION_CHEESE.add(getAttribute("N_CHEESE"));

        // Cutlery
        NO_CUTLERY.add(getAttribute("N_CUTLERY"));

        // Spicy ingredients
        NO_SPICY.add(getAttribute("N_SPICY"));
        NO_SPICY.add(getAttribute("N_JALAPENOS"));
        NO_SPICY.add(getAttribute("N_HOT_SAUCE"));

        // Sauces
        NO_SAUCES.add(getAttribute("N_SOUR_CREAM"));
        NO_SAUCES.add(getAttribute("N_MAYO"));
        NO_SAUCES.add(getAttribute("N_KETCHUP"));
        NO_SAUCES.add(getAttribute("N_MUSTARD"));
        NO_SAUCES.add(getAttribute("N_BBQ_SAUCE"));
        NO_SAUCES.add(getAttribute("N_RANCH"));
        NO_SAUCES.add(getAttribute("N_DRESSING"));
        NO_SAUCES.add(getAttribute("N_GUACAMOLE"));

        // Vegetables
        NO_VEGETABLES.add(getAttribute("N_ONION"));
        NO_VEGETABLES.add(getAttribute("N_TOMATO"));
        NO_VEGETABLES.add(getAttribute("N_LETTUCE"));
        NO_VEGETABLES.add(getAttribute("N_PICKLES"));
        NO_VEGETABLES.add(getAttribute("N_JALAPENOS"));
        NO_VEGETABLES.add(getAttribute("N_CILANTRO"));
        NO_VEGETABLES.add(getAttribute("N_AVOCADO"));
        NO_VEGETABLES.add(getAttribute("N_MUSHROOMS"));
        NO_VEGETABLES.add(getAttribute("N_BELL_PEPPER"));
        NO_VEGETABLES.add(getAttribute("N_OLIVES"));
        NO_VEGETABLES.add(getAttribute("N_CORN"));

        // Toppings
        NO_TOPPINGS.add(getAttribute("N_CHEESE"));
        NO_TOPPINGS.add(getAttribute("N_BACON"));
        NO_TOPPINGS.add(getAttribute("N_EGG"));
        NO_TOPPINGS.add(getAttribute("N_PARMESAN"));
        NO_TOPPINGS.add(getAttribute("N_CREAM"));
        NO_TOPPINGS.add(getAttribute("N_SOUR_CREAM"));

        // Proteins
        NO_PROTEINS.add(getAttribute("N_MEAT"));
        NO_PROTEINS.add(getAttribute("N_CHICKEN"));
        NO_PROTEINS.add(getAttribute("N_BEEF"));
        NO_PROTEINS.add(getAttribute("N_SHRIMP"));
        NO_PROTEINS.add(getAttribute("N_BACON"));

        // Seasonings
        NO_SEASONINGS.add(getAttribute("N_GARLIC"));
        NO_SEASONINGS.add(getAttribute("N_LIME"));
        NO_SEASONINGS.add(getAttribute("N_SALT"));
        NO_SEASONINGS.add(getAttribute("N_PEPPER"));

        // All removable ingredients
        ALL_REMOVABLE_INGREDIENTS.addAll(NO_VEGETABLES);
        ALL_REMOVABLE_INGREDIENTS.addAll(NO_SAUCES);
        ALL_REMOVABLE_INGREDIENTS.addAll(NO_TOPPINGS);
        ALL_REMOVABLE_INGREDIENTS.addAll(NO_PROTEINS);
        ALL_REMOVABLE_INGREDIENTS.addAll(NO_SEASONINGS);
    }

    private ItemAttribute getAttribute(String attributeKey) {
        return itemsAttrRepository.findByAttributeKey(attributeKey)
                .stream().findFirst().orElseThrow(() -> new IllegalStateException(
                        "Seed: item attribute not found: " + attributeKey));
    }

}
