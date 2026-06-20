package org.example.kafka;

import org.example.common.dto.UserNotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "user-notifications";

    @Autowired
    private KafkaTemplate<String, UserNotificationDto> kafkaTemplate;

    public void sendNotification(String operation, String email, String name) {
        UserNotificationDto notification = new UserNotificationDto(operation, email, name);
        kafkaTemplate.send(TOPIC, notification);
        System.out.println("Отправлено в Kafka: " + operation + " - " + email);
    }
}