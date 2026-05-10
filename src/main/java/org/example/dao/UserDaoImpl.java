package org.example.dao;

import jakarta.persistence.NoResultException;
import org.example.entities.User;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UserDaoImpl implements UserDao {

    @Override
    public User saveUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            session.persist(user);

            transaction.commit();
            System.out.println("Пользователь " + user.getUserName() + " сохранен");
            return user;
        } catch (Exception e) {
            System.err.println("Ошибка сохранения пользователя: " + e.getMessage());
            return null;
        }
    }

    @Override
    public User findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            System.err.println("Ошибка поиска пользователя с ID" + id + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public User findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;  // не найдено
        } catch (Exception e) {
            System.err.println("Ошибка поиска: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Ошибка получения списка пользователей: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public User updateUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            User userUpdate = session.merge(user);
            transaction.commit();
            System.out.println("Пользователь " + user.getUserName() + " обновлен");
            return userUpdate;
        } catch (Exception e) {
            System.err.println("Ошибка обновления: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean deleteUser(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            User userDelete = session.get(User.class, id);
            if (userDelete != null) {
                session.remove(userDelete);
                transaction.commit();
                System.out.println("Пользователь с ID " + id + " удален");
                return true;
            } else {
                System.out.println("Пользователь с ID " + id + " не найден");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Ошибка удаления: " + e.getMessage());
            return false;
        }
    }
}
