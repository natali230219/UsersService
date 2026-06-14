package org.example.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private final EmailService emailService = new EmailService();

    @Test
    void sendAccountCreatedEmail_ShouldNotThrowException() {
        assertDoesNotThrow(() -> emailService.sendAccountCreatedEmail("test@example.com", "Иван"));
    }

    @Test
    void sendAccountDeletedEmail_ShouldNotThrowException() {
        assertDoesNotThrow(() -> emailService.sendAccountDeletedEmail("test@example.com", "Петр"));
    }
}