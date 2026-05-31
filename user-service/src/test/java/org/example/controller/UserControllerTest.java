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

    // Тест №1
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.createdTime").exists());
    }

    // Тест №2
    @Test
    void createUser_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("");
        request.setEmail("test@test.com");
        request.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    // Тест №3
    @Test
    void createUser_WithNameTooLong_ShouldReturnBadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("a".repeat(101));
        request.setEmail("longname@test.com");
        request.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    // Тест №4
    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("Плохой Email");
        request.setEmail("invalid_email");
        request.setAge(38);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    // Тест №5
    @Test
    void createUser_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
        userRepository.save(new User("Duplicate Email", "duplicateEmail@test.com", 40));
        UserRequestDto request = new UserRequestDto();
        request.setName("Duplicate Email");
        request.setEmail("duplicateEmail@test.com");
        request.setAge(40);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Пользователь с email duplicateEmail@test.com уже существует"));
    }

    // Тест №6
    @Test
    void createUser_WithTooLowAge_ShouldReturnBadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("Negative Age");
        request.setEmail("negativeAge@test.com");
        request.setAge(-10);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.age").exists());
    }

    // Тест №7
    @Test
    void createUser_WithTooHighAge_ShouldReturnBadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("Too High Age");
        request.setEmail("tooHighAge@test.com");
        request.setAge(400);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.age").exists());
    }

    // Тест №8
    @Test
    void createUser_WithNullAge_ShouldReturnBadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("Null Age");
        request.setEmail("nullAge@test.com");
        request.setAge(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Тест №9
    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        userRepository.save(new User("User1", "user1@test.com", 20));
        userRepository.save(new User("User2", "user2@test.com", 25));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[0].createdTime").exists())
                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$[1].name").value("User2"));
    }

    // Тест №10
    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        User findIdUser = userRepository.save(new User("Find Id User", "findIdUser@test.com", 25));
        mockMvc.perform(get("/api/users/{id}", findIdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(findIdUser.getId()))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Find Id User"))
                .andExpect(jsonPath("$.email").value("findIdUser@test.com"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.createdTime").exists());
    }

    // Тест №11
    @Test
    void getUserByEmail_ShouldReturnUser() throws Exception {
        userRepository.save(new User("Find Email User", "findEmailUser@test.com", 27));
        mockMvc.perform(get("/api/users/email").param("email", "findEmailUser@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Find Email User"))
                .andExpect(jsonPath("$.email").value("findEmailUser@test.com"))
                .andExpect(jsonPath("$.age").value(27))
                .andExpect(jsonPath("$.createdTime").exists());
    }

    // Тест №12
    @Test
    void getById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 9999 не найден"));
    }

    // Тест №13
    @Test
    void getUserByEmail_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/email")
                        .param("email", "notfoundEmailUser@test.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Пользователь с email notfoundEmailUser@test.com не найден"));

    }

    // Тест №14
    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));  // Пустой список, а не null
    }

    // Тест №15
    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        User savedUser = userRepository.save(new User("Old Name User", "oldEmail@test.com", 35));
        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("New Name User");
        updateRequest.setEmail("newEmail@test.com");
        updateRequest.setAge(30);

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("New Name User"))
                .andExpect(jsonPath("$.email").value("newEmail@test.com"))
                .andExpect(jsonPath("$.age").value(30));
    }

    // Тест №16
    @Test
    void updateUser_NotFound_ShouldReturn404() throws Exception {
        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("New Name User");
        updateRequest.setEmail("newEmailUser@test.com");
        updateRequest.setAge(30);

        mockMvc.perform(put("/api/users/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 9999 не найден"));
    }

    // Тест №17
    @Test
    void updateUser_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {

        User user1 = userRepository.save(new User("User1", "user1@test.com", 25));
        User user2 = userRepository.save(new User("User2", "user2@test.com", 30));

        UserRequestDto updateRequest = new UserRequestDto();
        updateRequest.setName("User2 Updated");
        updateRequest.setEmail("user1@test.com");  // ← email user1
        updateRequest.setAge(35);

        mockMvc.perform(put("/api/users/{id}", user2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Пользователь с email " + user1.getEmail() + " уже существует"));
    }

    // Тест №18
    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        User saved = userRepository.save(new User("Delete User", "delete@test.com", 40));

        mockMvc.perform(delete("/api/users/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    // Тест №19
    @Test
    void deleteUser_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с ID 9999 не найден"));
    }
}