package org.example;

import org.example.dao.UserDaoImpl;
import org.example.entities.User;
import org.example.service.UserService;
import org.example.util.HibernateUtil;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService(new UserDaoImpl());

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

            try {
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
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Системная ошибка: " + e.getMessage());
            }
        }
    }

    private static void showMenu() {
        System.out.println();
        System.out.println("================== Главное меню: ==================");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по его ID");
        System.out.println("3. Найти пользователя по email");
        System.out.println("4. Показать всех пользователей");
        System.out.println("5. Обновить данные пользователя");
        System.out.println("6. Удалить пользователя");
        System.out.println("0. Выход");
        System.out.println("===================================================");
        System.out.println();
    }

    private static void createUser() {
        System.out.println("-----Создание нового пользователя-----");
        String name = getStringInput("Имя: ");
        String email = getStringInput("Email: ");
        int age = getIntInput("Возраст: ");

        User savedUser = userService.createUser(name, email, age);
        System.out.println("Пользователь сохранен в базе данных. ID: " + savedUser.getId());
    }

    private static void findUserById() {
        System.out.println("-----Поиск пользователя по его ID-----");
        long id = getLongInput("ID пользователя: ");
        User user = userService.getUserById(id);
        printUser(user);
    }

    // 3. Поиск пользователя по его email
    private static void findUserByEmail() {
        System.out.println("-----Поиск пользователя по его email-----");
        String email = getStringInput("Email: ");
        User user = userService.getUserByEmail(email);
        printUser(user);
    }

    // 4. Список всех пользователей
    private static void showAllUsers() {
        System.out.println("-----Все пользователи: -----");
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("Нет пользователей в базе данных");
            return;
        }

        System.out.println("Найдено: " + users.size());
        System.out.printf("%-3s | %-30s | %-25s | %-5s%n", "ID", "Имя", "Email", "Возраст");
        System.out.println("--------------------------------------------------------------------------");
        for (User user : users) {
            System.out.printf("%-3d | %-30s | %-25s | %-5d%n",
                    user.getId(),
                    user.getUserName(),
                    user.getEmail(),
                    user.getAge());
        }
    }

    private static void updateUser() {
        System.out.println("-----Обновление данных-----");
        long id = getLongInput("ID пользователя: ");

        User current = userService.getUserById(id);
        System.out.println("Текущие данные пользователя: " +
                current.getUserName() + ", " +
                current.getEmail() + ", " +
                current.getAge());

        String newName = getStringInput("Новое имя: ");
        String newEmail = getStringInput("Новый email: ");
        String newAgeStr = getStringInput("Новый возраст: ");

        Integer newAge = null;
        if (!newAgeStr.isEmpty()) {
            try {
                newAge = Integer.parseInt(newAgeStr);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный возраст, оставляем прежний");
            }
        }

        User updated = userService.updateUser(id, newName, newEmail, newAge);
        System.out.println("Пользователь обновлен!");
        System.out.println("Новые данные: " + updated.getUserName() + ", " + updated.getEmail());
    }

    private static void deleteUser() {
        System.out.println("-----Удаление пользователя-----");
        long id = getLongInput("ID пользователя:");

        User user = userService.getUserById(id);
        System.out.println("Удалить пользователя: " + user.getUserName() + "?");
        String confirm = getStringInput("Да/Нет:");

        if (confirm.equalsIgnoreCase("да")) {
            boolean result = userService.deleteUser(id);
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