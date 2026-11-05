package com.smarthealth.util;

import java.util.function.Consumer;
import java.util.function.Function;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {
    private static SessionFactory factory;
    private HibernateUtil() {}

    public static synchronized SessionFactory factory() {
        if (factory == null) {
            Configuration cfg = new Configuration().configure();
            String url = Config.get("db.url", null), user = Config.get("db.user", null), pass = Config.get("db.pass", null);
            if (url != null) cfg.setProperty("hibernate.connection.url", url);
            if (user != null) cfg.setProperty("hibernate.connection.username", user);
            if (pass != null) cfg.setProperty("hibernate.connection.password", pass);
            factory = cfg.buildSessionFactory();
        }
        return factory;
    }

    /** Runs work in one transaction and returns its result. */
    public static <T> T tx(Function<Session, T> work) {
        Session s = factory().openSession();
        Transaction t = s.beginTransaction();
        try {
            T result = work.apply(s);
            t.commit();
            return result;
        } catch (RuntimeException e) {
            if (t.isActive()) t.rollback();
            throw e;
        } finally {
            s.close();
        }
    }

    public static void run(Consumer<Session> work) {
        tx(s -> { work.accept(s); return null; });
    }

    public static synchronized void shutdown() {
        if (factory != null) { factory.close(); factory = null; }
    }
}
