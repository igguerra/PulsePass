package edu.unimag.pulsepass.persistence.controller;

import edu.unimag.pulsepass.persistence.dto.response.UserResponse;
import edu.unimag.pulsepass.persistence.exception.DuplicateResourceException;
import edu.unimag.pulsepass.persistence.exception.GlobalExceptionHandler;
import edu.unimag.pulsepass.persistence.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    private UserResponse buildUserResponse() {
        return new UserResponse(
                1L, "andrea", "andrea@email.com", true,
                "Andrea", "Lopez", "3001234567", "Santa Marta",
                LocalDate.of(2000, 5, 10));
    }

    private String registerBody(String email) {
        return """
                {
                    "username": "andrea",
                    "email": "%s",
                    "firstName": "Andrea",
                    "lastName": "Lopez",
                    "phone": "3001234567",
                    "city": "Santa Marta",
                    "birthDate": "2000-05-10"
                }
                """.formatted(email);
    }

    // TEST-CTRL-USR-001: valid registration -> 201
    @Test
    void shouldRegisterUser() throws Exception {
        when(service.register(any())).thenReturn(buildUserResponse());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("andrea@email.com")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@email.com"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).register(any());
    }

    // TEST-CTRL-USR-002: invalid email -> 400 and the service is never called
    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details.email").value("Email format is invalid"));

        verify(service, never()).register(any());
    }

    // TEST-CTRL-USR-003: duplicated username -> 409
    @Test
    void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
        when(service.register(any())).thenThrow(
                new DuplicateResourceException("Username already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("andrea@email.com")))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username already exists"));

        verify(service).register(any());
    }

    // TEST-CTRL-USR-004: find by email -> 200
    @Test
    void shouldFindUserByEmail() throws Exception {
        when(service.findByEmail("andrea@email.com")).thenReturn(buildUserResponse());

        mockMvc.perform(get("/api/users/by-email")
                        .param("email", "andrea@email.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("andrea@email.com"))
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(service).findByEmail("andrea@email.com");
    }

    // TEST-CTRL-USR-005: find by username -> 200
    @Test
    void shouldFindUserByUsername() throws Exception {
        when(service.findByUsername("andrea")).thenReturn(buildUserResponse());

        mockMvc.perform(get("/api/users/by-username")
                        .param("username", "andrea"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@email.com"));

        verify(service).findByUsername("andrea");
    }
}
