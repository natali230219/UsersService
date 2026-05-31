package org.example.common.dto;

public class UserNotificationDto {
    private String operation;
    private String email;
    private String name;

    public UserNotificationDto() {}

    public UserNotificationDto(String operation, String email, String name) {
        this.operation = operation;
        this.email = email;
        this.name = name;
    }

    // Геттеры и сеттеры
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserNotificationDto{" +
                "operation='" + operation + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}