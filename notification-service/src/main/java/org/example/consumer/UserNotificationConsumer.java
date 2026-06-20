package org.example.consumer;

import org.example.common.dto.UserNotificationDto;
import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserNotificationConsumer {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "user-notifications", groupId = "notification-group")
    public void consume(UserNotificationDto userNotificationDto) {

        System.out.println("Получено сообщение из Kafka: " + userNotificationDto);

        if("CREATE".equalsIgnoreCase(userNotificationDto.getOperation())) {
            emailService.sendAccountCreatedEmail(userNotificationDto.getEmail(), userNotificationDto.getName());
        } else if ("DELETE".equalsIgnoreCase(userNotificationDto.getOperation())) {
            emailService.sendAccountDeletedEmail(userNotificationDto.getEmail(), userNotificationDto.getName());
        }
    }

}
