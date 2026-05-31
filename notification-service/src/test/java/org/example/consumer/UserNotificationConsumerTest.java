package org.example.consumer;

import org.example.common.dto.UserNotificationDto;
import org.example.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserNotificationConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserNotificationConsumer consumer;

    // Тест №1
    @Test
    void consume_CreateOperation_ShouldSendCreatedEmail() {

        UserNotificationDto dto = new UserNotificationDto("CREATE", "test@example.com", "Иван");

        consumer.consume(dto);

        verify(emailService, times(1)).sendAccountCreatedEmail("test@example.com", "Иван");

        verify(emailService, never()).sendAccountDeletedEmail(anyString(), anyString());
    }

    //  Тест №2
    @Test
    void consume_DeleteOperation_ShouldSendDeletedEmail() {
        UserNotificationDto dto = new UserNotificationDto("DELETE", "test@example.com", "Петр");

        consumer.consume(dto);

        verify(emailService, times(1)).sendAccountDeletedEmail("test@example.com", "Петр");

        verify(emailService, never()).sendAccountCreatedEmail(anyString(), anyString());
    }

    // Тест №3
    @Test
    void consume_UnknownOperation_ShouldNotSendAnyEmail() {
        UserNotificationDto dto = new UserNotificationDto("UNKNOWN", "test@example.com", "Тест");

        consumer.consume(dto);

        verify(emailService, never()).sendAccountCreatedEmail(anyString(), anyString());
        verify(emailService, never()).sendAccountDeletedEmail(anyString(), anyString());
    }

    // Тест №4
    @Test
    void consume_OperationCaseInsensitive_ShouldWork() {
        UserNotificationDto dto = new UserNotificationDto("create", "test@example.com", "Иван");  // маленькие буквы

        consumer.consume(dto);

        verify(emailService, times(1)).sendAccountCreatedEmail("test@example.com", "Иван");
    }
}