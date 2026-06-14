package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.dto.EmailRequestDto;
import org.example.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    // Тест №1
    @Test
    void sendCreatedEmail_ShouldReturnOk() throws Exception {
        EmailRequestDto request = new EmailRequestDto();
        request.setEmail("test@example.com");
        request.setName("Иван");

        mockMvc.perform(post("/api/notifications/send-created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email отправлен (имитация)"))
                .andExpect(jsonPath("$.to").value("test@example.com"))
                .andExpect(jsonPath("$.type").value("created"));

        verify(emailService, times(1)).sendAccountCreatedEmail("test@example.com", "Иван");
    }

    // Тест №2
    @Test
    void sendCreatedEmail_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        EmailRequestDto request = new EmailRequestDto();
        request.setEmail("");
        request.setName("Иван");

        mockMvc.perform(post("/api/notifications/send-created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(emailService, never()).sendAccountCreatedEmail(anyString(), anyString());
    }

    // Тест №3
    @Test
    void sendCreatedEmail_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        EmailRequestDto request = new EmailRequestDto();
        request.setEmail("invalid-email");
        request.setName("Иван");

        mockMvc.perform(post("/api/notifications/send-created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(emailService, never()).sendAccountCreatedEmail(anyString(), anyString());
    }

    // Тест №4
    @Test
    void sendCreatedEmail_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        EmailRequestDto request = new EmailRequestDto();
        request.setEmail("test@example.com");
        request.setName("");

        mockMvc.perform(post("/api/notifications/send-created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(emailService, never()).sendAccountCreatedEmail(anyString(), anyString());
    }


    // Тест №5
    @Test
    void sendDeletedEmail_ShouldReturnOk() throws Exception {
        EmailRequestDto request = new EmailRequestDto();
        request.setEmail("test@example.com");
        request.setName("Петр");

        mockMvc.perform(post("/api/notifications/send-deleted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email отправлен (имитация)"))
                .andExpect(jsonPath("$.to").value("test@example.com"))
                .andExpect(jsonPath("$.type").value("deleted"));

        verify(emailService, times(1)).sendAccountDeletedEmail("test@example.com", "Петр");
    }
}