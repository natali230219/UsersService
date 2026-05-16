package org.example.dao;

import org.example.entities.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test_user")
            .withPassword("test_password");

    private SessionFactory sessionFactory;
    private UserDao userDao;

    @BeforeAll
    void setUpAll() {
        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");

        configuration.addAnnotatedClass(User.class);

        sessionFactory = configuration.buildSessionFactory();
        userDao = new UserDaoImpl(sessionFactory);
    }

    @AfterAll
    void tearDownAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            transaction.commit();
        }
    }

    @Test
    void saveUser_ShouldSaveAndReturnUserWithId() {
        User user = new User("Тест Тестов", "test@test.com", 25);
        User saved = userDao.saveUser(user);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Тест Тестов", saved.getUserName());
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        User saved = userDao.saveUser(new User("Найди Меня", "find@test.com", 30));
        User found = userDao.findById(saved.getId());

        assertNotNull(found);
        assertEquals("Найди Меня", found.getUserName());
    }

    @Test
    void findById_ShouldReturnNull_WhenUserNotExists() {
        User found = userDao.findById(999L);
        assertNull(found);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        userDao.saveUser(new User("Email Тест", "unique@test.com", 35));
        User found = userDao.findByEmail("unique@test.com");

        assertNotNull(found);
        assertEquals("unique@test.com", found.getEmail());
    }

    @Test
    void findByEmail_ShouldReturnNull_WhenEmailNotExists() {
        User found = userDao.findByEmail("notexist@test.com");
        assertNull(found);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        userDao.saveUser(new User("User1", "user1@test.com", 20));
        userDao.saveUser(new User("User2", "user2@test.com", 25));

        List<User> users = userDao.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        User saved = userDao.saveUser(new User("До Обновления", "before@test.com", 25));

        saved.setUserName("После Обновления");
        saved.setEmail("after@test.com");
        saved.setAge(30);

        User updated = userDao.updateUser(saved);

        assertEquals("После Обновления", updated.getUserName());
        assertEquals("after@test.com", updated.getEmail());
        assertEquals(30, updated.getAge());

        User fromDb = userDao.findById(saved.getId());
        assertEquals("После Обновления", fromDb.getUserName());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        User saved = userDao.saveUser(new User("Удали Меня", "delete@test.com", 40));

        boolean result = userDao.deleteUser(saved.getId());

        assertTrue(result);
        assertNull(userDao.findById(saved.getId()));
    }

    @Test
    void deleteUser_ShouldReturnFalse_WhenUserNotExists() {
        boolean result = userDao.deleteUser(999L);
        assertFalse(result);
    }
}
