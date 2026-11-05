package com.smarthealth.dao;

import com.smarthealth.model.Patient;
import com.smarthealth.util.HibernateUtil;

public final class PatientDAO {
    private PatientDAO() {}

    public static Patient findByUserId(Long userId) {
        return HibernateUtil.tx(s -> s.createQuery("from Patient p where p.user.id = :u", Patient.class)
                .setParameter("u", userId).uniqueResult());
    }
    public static void saveOrUpdate(Patient p) { HibernateUtil.run(s -> s.saveOrUpdate(p)); }
}
