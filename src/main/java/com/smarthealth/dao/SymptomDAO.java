package com.smarthealth.dao;

import com.smarthealth.model.SymptomRecord;
import com.smarthealth.util.HibernateUtil;

public final class SymptomDAO {
    private SymptomDAO() {}
    public static void save(SymptomRecord r) { HibernateUtil.run(s -> s.save(r)); }
    public static SymptomRecord findById(Long id) { return HibernateUtil.tx(s -> s.get(SymptomRecord.class, id)); }
}
