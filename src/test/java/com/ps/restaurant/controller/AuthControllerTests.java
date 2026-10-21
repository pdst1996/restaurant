package com.ps.restaurant.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ps.restaurant.config.JwtService;
import com.ps.restaurant.dto.LoginRequest;
import com.ps.restaurant.dto.LoginResponse;
import com.ps.restaurant.dto.UserRequest;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthControllerTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new AuthController(userRepository, passwordEncoder, jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ErrorResponseHandler())
                .build();
    }

    @Test
    void registerSavesAllUserFieldsAndEncodesPassword() {
        UserRequest request = new UserRequest("ana@example.com", "password", "ana", "Ana", "Pérez");
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        ResponseEntity<?> response = controller.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "Usuario creado"));
        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue()).satisfies(user -> {
            assertThat(user.getEmail()).isEqualTo(request.email());
            assertThat(user.getUser()).isEqualTo(request.user());
            assertThat(user.getName()).isEqualTo(request.name());
            assertThat(user.getSurName()).isEqualTo(request.surName());
            assertThat(user.getPassword()).isEqualTo("encoded-password");
        });
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        UserRequest request = new UserRequest("ana@example.com", "password", "ana", "Ana", "Pérez");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value("409"))
            .andExpect(jsonPath("$.origin").value("AuthController.register"))
            .andExpect(jsonPath("$.errorCode").value("AUTH_EMAIL_ALREADY_EXISTS"))
            .andExpect(jsonPath("$.errorMessage.es").value("El correo ya existe."))
            .andExpect(jsonPath("$.errorMessage.en").value("The email already exists."))
            .andExpect(jsonPath("$.messageVariables").isEmpty())
            .andExpect(jsonPath("$.timestamp").exists());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        UserRequest request = new UserRequest("ana@example.com", "password", "ana", "Ana", "Pérez");
        when(userRepository.findByUserOrEmail(request.user())).thenReturn(Optional.of(existingUser()));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value("409"))
            .andExpect(jsonPath("$.errorCode").value("AUTH_USER_ALREADY_EXISTS"))
            .andExpect(jsonPath("$.errorMessage.es").value("El usuario ya existe."))
            .andExpect(jsonPath("$.errorMessage.en").value("The username already exists."));
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void loginAuthenticatesByUsernameAndReturnsCorrectUserFields() {
        User user = existingUser();
        when(userRepository.findByUserOrEmail("ana")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getEmail())).thenReturn("jwt-token");

        ResponseEntity<LoginResponse> response = controller.login(new LoginRequest("ana", "password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponse body = response.getBody();
        assertThat(body).isNotNull();
        LoginResponse login = body;
        assertThat(login.token()).isEqualTo("jwt-token");
        assertThat(login.user()).satisfies(result -> {
            assertThat(result.getId()).isEqualTo(user.getId());
            assertThat(result.getEmail()).isEqualTo(user.getEmail());
            assertThat(result.getUser()).isEqualTo(user.getUser());
            assertThat(result.getName()).isEqualTo(user.getName());
            assertThat(result.getSurName()).isEqualTo(user.getSurName());
        });
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void loginAuthenticatesByEmail() {
        User user = existingUser();
        when(userRepository.findByUserOrEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getEmail())).thenReturn("jwt-token");

        ResponseEntity<LoginResponse> response = controller.login(new LoginRequest(user.getEmail(), "password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo("jwt-token");
    }

    @Test
    void loginRejectsUnknownUsername() throws Exception {
        when(userRepository.findByUserOrEmail("unknown")).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"user":"unknown","password":"secret-password"}
                        """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value("401"))
            .andExpect(jsonPath("$.origin").value("AuthController.login"))
            .andExpect(jsonPath("$.errorCode").value("AUTH_USER_NOT_FOUND"))
            .andExpect(jsonPath("$.errorMessage.es").value("El usuario no existe."))
            .andExpect(jsonPath("$.errorMessage.en").value("The user does not exist."))
            .andExpect(jsonPath("$.messageVariables").isEmpty())
            .andExpect(jsonPath("$.timestamp").exists())
            .andReturn();
        assertThat(response.getResponse().getContentAsString()).doesNotContain("secret-password");
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void loginRejectsIncorrectPasswordWithoutGeneratingToken() throws Exception {
        User user = existingUser();
        when(userRepository.findByUserOrEmail("ana")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPassword())).thenReturn(false);

        MvcResult response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"user":"ana","password":"wrong-password"}
                        """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value("401"))
            .andExpect(jsonPath("$.origin").value("AuthController.login"))
            .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_PASSWORD"))
            .andExpect(jsonPath("$.errorMessage.es").value("La contraseña es incorrecta."))
            .andExpect(jsonPath("$.errorMessage.en").value("The password is incorrect."))
            .andExpect(jsonPath("$.messageVariables").isEmpty())
            .andExpect(jsonPath("$.timestamp").exists())
            .andReturn();
        assertThat(response.getResponse().getContentAsString())
                .doesNotContain("wrong-password", "encoded-password", "stackTrace");
        verifyNoInteractions(jwtService);
    }

    private String registrationJson() {
        return """
                {"email":"ana@example.com","password":"password","user":"ana","name":"Ana","surName":"Pérez"}
                """;
    }

    private User existingUser() {
        User user = new User();
        user.setId("1");
        user.setEmail("ana@example.com");
        user.setUser("ana");
        user.setPassword("encoded-password");
        user.setName("Ana");
        user.setSurName("Pérez");
        return user;
    }
}
