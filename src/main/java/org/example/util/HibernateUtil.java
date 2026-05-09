package org.example.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateUtil {

    // SessionFactory - фабрика для создания сессий
    private static final SessionFactory sessionFactory = buildSessionFactory();

    // Создаём SessionFactory из hibernate.cfg.xml
    // Вызывается 1 раз при загрузке класса
    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Ошибка при создании SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Возвращает SessionFactory для работы с БД
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
