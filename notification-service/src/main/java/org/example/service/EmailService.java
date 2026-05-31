package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendAccountCreatedEmail (String to, String name){
        System.out.println("=============== Имитация отправки сообщения на email:  ===============");
        String message = String.format("Письмо для пользователя: %s, с email:  %s. Ваш аккаунт создан!", name, to);
        logger.info(message);
    }

    public void sendAccountDeletedEmail (String to, String name){
        System.out.println("=============== Имитация отправки сообщения на email:  ===============");
        String message = String.format("Письмо для пользователя: %s, с email:  %s. Ваш аккаунт удален!", name, to);
        logger.info(message);
    }
}
