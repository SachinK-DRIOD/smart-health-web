package com.smarthealth.dao;

import com.smarthealth.model.User;
import com.smarthealth.util.HibernateUtil;
import java.util.List;

public final class UserDAO {
    private UserDAO() {}

    public static User findByEmail(String email) {
        return HibernateUtil.tx(s -> s.createQuery("from User u where u.email = :e", User.class)
                .setParameter("e", email.toLowerCase()).uniqueResult());
    }
    public static void save(User u) { HibernateUtil.run(s -> s.save(u)); }
    public static List<User> findAll() {
        return HibernateUtil.tx(s -> s.createQuery("from User u order by u.id", User.class).list());
    }
    public static long countByRole(String role) {
        return HibernateUtil.tx(s -> s.createQuery("select count(u) from User u where u.role = :r", Long.class)
                .setParameter("r", role).uniqueResult());
    }
}
