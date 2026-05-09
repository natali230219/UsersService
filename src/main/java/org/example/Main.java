package org.example;

import org.example.dao.UserDao;
import org.example.entities.User;
import org.example.util.HibernateUtil;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void main(String[] args) {

        // Добавление Shutdown Hook для корректного закрытия соединения с БД
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nЗакрываю соединение с БД...");
            HibernateUtil.shutdown();
        }));

        boolean exit = true;

        while (exit) {
            showMenu();
            int choice = getIntInput("Выберите действие: ");
            switch (choice) {
                case 1:
                    createUser();
                    break;
                case 2:
                    findUserById();
                    break;
                case 3:
                    findUserByEmail();
                    break;
                case 4:
                    showAllUsers();
                    break;
                case 5:
                    updateUser();
                    break;
                case 6:
                    deleteUser();
                    break;
                case 0:
                    exit = false;
                    System.out.println("Выход их программы...");
                    HibernateUtil.shutdown();
                    break;
                default:
                    System.out.println("Неверный выбор! Попробуйте снова");
            }
            System.out.println();
        }
    }

    private static void showMenu() {
        System.out.println("================== Главное меню: ==================");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по его ID");
        System.out.println("3. Найти пользователя по email");
        System.out.println("4. Показать всех пользователей");
        System.out.println("5. Обновить данные пользователя");
        System.out.println("6. Удалить пользователя");
        System.out.println("0. Выход");
        System.out.println("===================================================");
    }

    // 1. Создание нового пользователя
    private static void createUser() {
        System.out.println("-----Создание нового пользователя-----");
        String name = getStringInput("Имя: ");
        String email = getStringInput("Email: ");
        int age = getIntInput("Возраст: ");

        if (userDao.findByEmail(email) != null) {
            System.out.println("Ошибка: пользователь с таким email уже существует!");
            return;
        }
        User user = new User(name, email, age);
        User saved = userDao.saveUser(user);

        if (saved != null) {
            System.out.println("Пользователь сохранен в базе данных. ID: " + saved.getId());
        } else {
            System.out.println("Ошибка сохранения!");
        }
    }

    // 2. Поиск пользователя по его ID
    private static void findUserById() {
        System.out.println("-----Поиск пользователя по его ID-----");
        long id = getLongInput("ID пользователя: ");
        User user = userDao.findById(id);
        if (user != null) {
            printUser(user);
        } else {
            System.out.println("Пользователь не найден!");
        }
    }

    // 3. Поиск пользователя по его email
    private static void findUserByEmail() {
        System.out.println("-----Поиск пользователя по его email-----");
        String email = getStringInput("Email: ");
        User user = userDao.findByEmail(email);
        if (user != null) {
            printUser(user);
        } else {
            System.out.println("Пользователь не найден");
        }
    }

    // 4. Список всех пользователей
    private static void showAllUsers() {
        System.out.println("-----Все пользователи: -----");
        List<User> users = userDao.findAll();
        if (users.isEmpty()) {
            System.out.println("Нет пользователей в базе данных");
            return;
        }

        System.out.println("Найдено: " + users.size());
        for (User user : users) {
            System.out.printf("%d | %s | %s | %d%n", user.getId(), user.getUserName(), user.getEmail(), user.getAge());
        }
    }

    // 5. Обновление данных пользователя
    private static void updateUser() {
        System.out.println("-----Обновление данных-----");
        long id = getLongInput("ID пользователя:");
        User user = userDao.findById(id);

        if (user == null) {
            System.out.println("Пользователь не найден");
            return;
        }

        System.out.println("Текущие данные пользователя: " +
                user.getUserName() + ", " +
                user.getEmail() + ", " +
                user.getAge()
        );

        String newName = getStringInput("Новое имя: ");
        if (!newName.isEmpty()) {
            user.setUserName(newName);
        }
        String newEmail = getStringInput("Новый email: ");
        if (!newEmail.isEmpty()) {
            User existingUser = userDao.findByEmail(newEmail);
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                System.out.println("Email принадлежит другому пользователю!");
                return;
            }
            user.setEmail(newEmail);
        }

        String newAge = getStringInput("Новый возраст: ");

        if (!newAge.isEmpty()) {
            try {
                user.setAge(Integer.parseInt(newAge));
            } catch (NumberFormatException e) {
                System.out.println("Некорректный возраст");
            }
        }

        User updatedUser = userDao.updateUser(user);
        if (updatedUser != null) {
            System.out.println("Пользователь обновлен!");
        }
    }

    // 6. Удаление пользователя
    private static void deleteUser() {
        System.out.println("-----Удаление пользователя-----");
        long id = getLongInput("ID пользователя:");
        User user = userDao.findById(id);

        if (user == null) {
            System.out.println("Пользователь не найден");
            return;
        }

        System.out.println("Удалить пользователя: " + user.getUserName() + "?");
        String confirm = getStringInput("Да/Нет:");
        if(confirm.equalsIgnoreCase("да")){
            boolean result = userDao.deleteUser(id);
            System.out.println(result ? "Пользователь удален" : "Ошибка удаления");
        } else {
            System.out.println("Удаление отменено");
        }
    }

    private static String getStringInput(String s) {
        System.out.print(s);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String i) {
        while (true) {
            System.out.print(i);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Введите число!");
            }
        }
    }

    private static long getLongInput(String l) {
        while (true) {
            System.out.print(l);
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Введите число!");
            }
        }
    }

    private static void printUser(User user) {
        System.out.println("ID: " + user.getId());
        System.out.println("Имя: " + user.getUserName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Возраст: " + user.getAge());
        System.out.println("Создан: " + user.getCreatedTime());
    }
}