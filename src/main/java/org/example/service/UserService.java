package org.example.service;

import org.example.dao.UserDao;
import org.example.entities.User;

import java.util.List;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUser(String name, String email, int age) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Возраст должен быть в пределе от 0 до 150 лет");
        }

        User exiting = userDao.findByEmail(email);

        if (exiting != null) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = new User(name, email, age);
        return userDao.saveUser(user);
    }

    public User getUserById(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным числом");
        }

        User user = userDao.findById(id);

        if (user == null) {
            throw new IllegalArgumentException("Пользователь с ID " + id + " не существует");
        }
        return user;
    }

    public User getUserByEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        User user = userDao.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("Пользователь с email " + email + " не найден");
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public User updateUser(Long id, String newName, String newEmail, Integer newAge) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным");
        }

        User user = getUserById(id);

        if (newName != null) {
            String trimmedName = newName.trim();
            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("Имя не может быть пустым");
            }
            user.setUserName(trimmedName);
        }

        if (newEmail != null) {
            String trimmedEmail = newEmail.trim();
            if (trimmedEmail.isEmpty()) {
                throw new IllegalArgumentException("Email не может быть пустым");
            }

            User existing = userDao.findByEmail(trimmedEmail);
            if (existing != null && !existing.getId().equals(user.getId())) {
                throw new IllegalArgumentException(
                        "Пользователь с email '" + trimmedEmail + "' уже существует"
                );
            }
            user.setEmail(trimmedEmail);
        }

        if (newAge != null) {
            if (newAge < 0 || newAge > 150) {
                throw new IllegalArgumentException("Возраст должен быть от 0 до 150");
            }
            user.setAge(newAge);
        }

        return userDao.updateUser(user);
    }

    public boolean deleteUser(Long id) {
        getUserById(id);
        return userDao.deleteUser(id);
    }
}
