package org.example.dao;

import org.example.entities.User;

import java.util.List;

public interface UserDao {
    User saveUser(User user);
    User findById(Long id);
    User findByEmail(String email);
    List<User> findAll();
    User updateUser(User user);
    boolean deleteUser(Long id);
}
