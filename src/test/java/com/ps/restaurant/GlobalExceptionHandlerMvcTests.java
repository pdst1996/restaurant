package com.ps.restaurant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ps.restaurant.config.JwtService;
import com.ps.restaurant.controller.AuthController;
import com.ps.restaurant.repository.UserRepository;

/** Exercises error resolution through MVC, where throwing from another handler fails. */
@WebMvcTest(controllers = AuthController.class, properties = {"debug=false", "logging.level.root=WARN"})
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerMvcTests {

    @MockitoBean
    private com.ps.restaurant.repository.RoleRepository roleRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void returnsStructuredNotFoundForMissingResource() throws Exception {
        mockMvc.perform(get("/missing-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.origin").value("/missing-resource"))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.stackTrace").isNotEmpty())
                .andExpect(jsonPath("$.errorMessage.es").value("Recurso no encontrado."));
    }

    @Test
    void returnsStructuredBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.stackTrace").isArray())
                .andExpect(jsonPath("$.stackTrace[0].className").isNotEmpty())
                .andExpect(jsonPath("$.stackTrace[0].methodName").isNotEmpty());
    }

    @Test
    void preservesStructuredApplicationErrors() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"user\":\"\",\"password\":\"\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("401"))
                .andExpect(jsonPath("$.errorCode").value("AUTH_USER_NOT_FOUND"));
    }
}
