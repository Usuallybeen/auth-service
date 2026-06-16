package com.example.auth_service.controller;

import com.example.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
    }

    @Test
    void register_user() throws Exception {
        mockMvc.perform(
                    post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\": \"test\",\"password\": \"test\"}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void register_alreadyExisting_user() throws Exception {
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\": \"test\",\"password\": \"test\"}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\": \"test\",\"password\": \"test\"}")
                )
                .andExpect(status().isConflict());
    }

    @Test
    void register_missingPass() throws Exception {
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\": \"test\",\"password\": \"\"}")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_user() throws Exception {
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\": \"test\",\"password\": \"test\"}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\": \"test\",\"password\": \"test\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_badCredentials_user() throws Exception {
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\": \"test\",\"password\": \"test\"}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\": \"test\",\"password\": \"test1\"}")
                )
                .andExpect(status().isUnauthorized());
    }
}
