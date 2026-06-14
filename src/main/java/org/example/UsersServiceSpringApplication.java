package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UsersServiceSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsersServiceSpringApplication.class, args);
        System.out.println("Spring приложение запущено на http://localhost:8080");
    }
}