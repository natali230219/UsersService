package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.UserRequestDto;
import org.example.entities.User;
import org.example.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("Иван Иванов");
        request.setEmail("ivan@mail.com");
        request.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.id").exists())           // ← ID должен быть
                .andExpect(jsonPath("$.createdTime").exists()) // ← дата должна быть
                .andExpect(jsonPath("$._links").exists());     // ← HATEOAS ссылки!
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        userRepository.save(new User("User1", "user1@test.com", 20));
        userRepository.save(new User("User2", "user2@test.com", 25));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userResponseDtoList.length()").value(2))  // ← ИЗМЕНЕНО!
                .andExpect(jsonPath("$._links.self").exists())      // ← ссылка на себя
                .andExpect(jsonPath("$._links.create").exists());   // ← ссылка на создание
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        User saved = userRepository.save(new User("Удали", "delete@test.com", 40));
        mockMvc.perform(delete("/api/users/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        // Проверяем, что пользователь действительно удалён
        mockMvc.perform(get("/api/users/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        User saved = userRepository.save(new User("Найти Меня", "find@test.com", 25));

        mockMvc.perform(get("/api/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Найти Меня"))
                .andExpect(jsonPath("$._links.self").exists())     // ← HATEOAS!
                .andExpect(jsonPath("$._links.all-users").exists())
                .andExpect(jsonPath("$._links.update").exists())
                .andExpect(jsonPath("$._links.delete").exists());
    }

    @Test
    void getUserByEmail_ShouldReturnUser() throws Exception {
        userRepository.save(new User("По Email", "email@test.com", 30));

        mockMvc.perform(get("/api/users/email")
                        .param("email", "email@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("По Email"))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        User saved = userRepository.save(new User("Старое Имя", "old@test.com", 20));

        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("Новое Имя");
        updateRequest.setEmail("new@test.com");
        updateRequest.setAge(30);

        mockMvc.perform(put("/api/users/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое Имя"))
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
        userRepository.save(new User("Первый", "duplicate@test.com", 25));

        UserRequestDto request = new UserRequestDto();
        request.setName("Второй");
        request.setEmail("duplicate@test.com");
        request.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Пользователь с email duplicate@test.com уже существует"));
    }

    @Test
    void getUserById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 9999 не найден"));
    }

    @Test
    void createUser_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("Тест");
        request.setEmail("");
        request.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userResponseDtoList").doesNotExist()); // ← пустой список
    }
}