package com.ps.restaurant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ps.restaurant.config.JwtService;
import com.ps.restaurant.repository.UserRepository;

@WebMvcTest(controllers = AuthController.class, properties = {"debug=false", "logging.level.root=WARN"})
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties(SpringDocConfigProperties.class)
@ImportAutoConfiguration({SpringDocConfiguration.class, SpringDocWebMvcConfiguration.class})
class AuthOpenApiTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void documentsLoginErrorAndPreservesSuccessSchema() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['401']"
                        + ".content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ErrorResponse"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['200']"
                        + ".content['application/json'].schema['$ref']")
                        .value("#/components/schemas/LoginResponse"));
    }

    @Test
    void documentsRegistrationConflictAndCreatedStatus() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/auth/register'].post.responses['409']"
                        + ".content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ErrorResponse"))
                .andExpect(jsonPath("$.paths['/api/auth/register'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/register'].post.responses['200']").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.status").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.origin").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.errorCode").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.errorMessage").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.messageVariables").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.timestamp").exists());
    }
}
