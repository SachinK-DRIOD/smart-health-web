package com.smarthealth.dao;

import com.smarthealth.model.Doctor;
import com.smarthealth.util.HibernateUtil;
import java.util.List;

public final class DoctorDAO {
    private DoctorDAO() {}

    public static List<Doctor> findAll() {
        return HibernateUtil.tx(s -> s.createQuery("from Doctor d order by d.specialization, d.id", Doctor.class).list());
    }
    public static Doctor findById(Long id) { return HibernateUtil.tx(s -> s.get(Doctor.class, id)); }
    public static Doctor findByUserId(Long userId) {
        return HibernateUtil.tx(s -> s.createQuery("from Doctor d where d.user.id = :u", Doctor.class)
                .setParameter("u", userId).uniqueResult());
    }
    public static void save(Doctor d) { HibernateUtil.run(s -> s.save(d)); }
    public static long count() {
        return HibernateUtil.tx(s -> s.createQuery("select count(d) from Doctor d", Long.class).uniqueResult());
    }
}
