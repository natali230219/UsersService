package org.example.service;

import org.example.dao.UserDao;
import org.example.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao);
    }

    @Test
    void createUser_Success_ShouldReturnSavedUser() {

        when(userDao.findByEmail("ivan@mail.com")).thenReturn(null);  // email свободен
        when(userDao.saveUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });


        User result = userService.createUser("Иван Иванов", "ivan@mail.com", 30);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Иван Иванов", result.getUserName());

        verify(userDao, times(1)).findByEmail("ivan@mail.com");
        verify(userDao, times(1)).saveUser(any(User.class));
    }

    @Test
    void createUser_WithEmptyName_ShouldThrowException() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("", "test@test.com", 25));

        assertEquals("Имя не может быть пустым", exception.getMessage());

        verify(userDao, never()).saveUser(any(User.class));
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {

        User existingUser = new User("Существующий", "existing@mail.com", 20);
        existingUser.setId(1L);
        when(userDao.findByEmail("existing@mail.com")).thenReturn(existingUser);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("Новый", "existing@mail.com", 30));

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userDao, never()).saveUser(any(User.class));
    }

    @Test
    void getUserById_Success_ShouldReturnUser() {

        User expectedUser = new User("Тестовый", "test@test.com", 25);
        expectedUser.setId(1L);
        when(userDao.findById(1L)).thenReturn(expectedUser);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Тестовый", result.getUserName());
    }

    @Test
    void getUserById_UserNotFound_ShouldThrowException() {

        when(userDao.findById(999L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(999L));

        assertEquals("Пользователь с ID 999 не существует", exception.getMessage());
    }

    @Test
    void updateUser_Success_ShouldUpdateFields() {

        User existingUser = new User("Старое Имя", "old@mail.com", 20);
        existingUser.setId(1L);

        when(userDao.findById(1L)).thenReturn(existingUser);
        when(userDao.findByEmail("new@mail.com")).thenReturn(null);
        when(userDao.updateUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, "Новое Имя", "new@mail.com", 25);

        assertEquals("Новое Имя", result.getUserName());
        assertEquals("new@mail.com", result.getEmail());
        assertEquals(25, result.getAge());
    }

    @Test
    void getAllUsers_Success_ShouldReturnList() {

        when(userDao.findAll()).thenReturn(java.util.List.of(
                new User("User1", "u1@test.com", 20),
                new User("User2", "u2@test.com", 30)
        ));

        var result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void deleteUser_Success_ShouldReturnTrue() {

        User user = new User("Для удаления", "delete@test.com", 20);
        user.setId(1L);
        when(userDao.findById(1L)).thenReturn(user);
        when(userDao.deleteUser(1L)).thenReturn(true);

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userDao, times(1)).deleteUser(1L);
    }
}