package org.example.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }

    public User() {}
    public User(String username, String email, Integer age) {
        this.username = username;
        this.email = email;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s', age=%d, createdTime=%s}",
                id, username, email, age, createdTime);
    }
}
