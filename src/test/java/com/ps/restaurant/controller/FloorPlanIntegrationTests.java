package com.ps.restaurant.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.ps.restaurant.dto.*;
import com.ps.restaurant.model.ElementType;
import com.ps.restaurant.model.Floor;
import com.ps.restaurant.model.TableShape;
import com.ps.restaurant.repository.*;
import tools.jackson.databind.json.JsonMapper;

/** No ambient test transaction: each HTTP operation must commit or roll back on its own. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:floor-plan-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.open-in-view=false"
})
@ActiveProfiles("test")
class FloorPlanIntegrationTests {
    private static final JsonMapper JSON = JsonMapper.builder().findAndAddModules().build();
    private static final String LAYOUT = "/floor/floors/floor-plan";
    @Autowired private WebApplicationContext context;
    @Autowired private FloorRepository floors;
    @Autowired private ElementTypeRepository types;
    @Autowired private ElementPlanRepository elements;
    @Autowired private TableRepository tables;
    private MockMvc mvc;
    private Floor floor;
    private ElementType type;
    private String tableName;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        String suffix = UUID.randomUUID().toString();
        tableName = "Table-" + suffix;
        floor = new Floor();
        floor.setName("Floor-" + suffix);
        floor = floors.save(floor);
        type = new ElementType();
        type.setType("wall-" + suffix);
        type.setLabel(Map.of("es", "Pared", "en", "Wall"));
        type.setWidth(200);
        type.setHeight(12);
        type = types.save(type);
    }

    @Test
    void createsUpdatesAndDeletesLayoutEntitiesAndReturnsDtoGraphs() throws Exception {
        var created = save(new LayoutRequest(List.of(new TableLayoutRequest(null, tableRequest(0))),
                List.of(new ElementLayoutRequest(null, elementRequest())), null));
        long tableId = created.get("data").get("tables").get(0).get("id").asLong();
        long elementId = created.get("data").get("elements").get(0).get("id").asLong();
        assertThat(created.get("data").get("tables").get(0).get("floor").get("id").asLong()).isEqualTo(floor.getId());
        assertThat(created.get("data").get("elements").get(0).get("elementType").get("id").asLong()).isEqualTo(type.getId());

        save(new LayoutRequest(List.of(new TableLayoutRequest(tableId, tableRequest(50))),
                List.of(new ElementLayoutRequest(elementId,
                        new ElementPlanRequest(floor.getId(), type.getType(), 30, 40, 100, 20, 90, "Updated"))), null));
        assertThat(tables.findById(tableId).orElseThrow().getX()).isEqualTo(50);
        assertThat(elements.findById(elementId).orElseThrow().getLabel()).isEqualTo("Updated");
        mvc.perform(get("/table/tables").param("id", Long.toString(tableId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].floor.id").value(floor.getId()));
        mvc.perform(get("/plan-element/plan-elements").param("id", Long.toString(elementId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].elementType.id").value(type.getId()));

        save(new LayoutRequest(null, null, List.of(elementId)));
        assertThat(elements.existsById(elementId)).isFalse();
        assertThat(tables.existsById(tableId)).isTrue();
        assertThat(types.existsById(type.getId())).isTrue();
    }

    @Test
    void rollsBackDeletionsAndTableChangesWhenAnotherReferenceIsMissing() throws Exception {
        var created = save(new LayoutRequest(List.of(new TableLayoutRequest(null, tableRequest(0))),
                List.of(new ElementLayoutRequest(null, elementRequest())), null));
        long tableId = created.get("data").get("tables").get(0).get("id").asLong();
        long elementId = created.get("data").get("elements").get(0).get("id").asLong();
        LayoutRequest invalid = new LayoutRequest(List.of(new TableLayoutRequest(tableId, tableRequest(500))),
                List.of(new ElementLayoutRequest(Long.MAX_VALUE, elementRequest())), List.of(elementId));
        mvc.perform(put(LAYOUT).contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(invalid)))
                .andExpect(status().isNotFound());
        assertThat(tables.findById(tableId).orElseThrow().getX()).isZero();
        assertThat(elements.existsById(elementId)).isTrue();
    }

    @Test
    void rejectsNestedInvalidValuesAndUnknownElementTypes() throws Exception {
        LayoutRequest invalid = new LayoutRequest(List.of(new TableLayoutRequest(null, tableRequest(-1))), null, null);
        mvc.perform(put(LAYOUT).contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/plan-element/plan-elements").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(new ElementPlanRequest(floor.getId(), "missing-type", 0, 0, 100, 10, 0, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsConflictingSaveAndDeleteWithoutDeletingTheElement() throws Exception {
        var created = save(new LayoutRequest(null, List.of(new ElementLayoutRequest(null, elementRequest())), null));
        long id = created.get("data").get("elements").get(0).get("id").asLong();
        LayoutRequest conflict = new LayoutRequest(null, List.of(new ElementLayoutRequest(id, elementRequest())), List.of(id));
        mvc.perform(put(LAYOUT).contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(conflict)))
                .andExpect(status().isConflict());
        assertThat(elements.existsById(id)).isTrue();
    }

    @Test
    void supportsEmptyLayoutsAndPreservesFloorDefaults() throws Exception {
        save(new LayoutRequest(null, null, null));
        mvc.perform(post("/floor/floors").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Minimal floor\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.width").value(1200))
                .andExpect(jsonPath("$.height").value(800)).andExpect(jsonPath("$.sort_order").value(0));
    }

    private tools.jackson.databind.JsonNode save(LayoutRequest request) throws Exception {
        String response = mvc.perform(put(LAYOUT).contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();
        return JSON.readTree(response);
    }

    private TableRequest tableRequest(int x) {
        return new TableRequest(tableName, 4, floor.getId(), x, 0, 100, 80, 0, TableShape.CIRCULAR);
    }

    private ElementPlanRequest elementRequest() {
        return new ElementPlanRequest(floor.getId(), type.getType(), 0, 0, 200, 12, null, "Wall");
    }
}
