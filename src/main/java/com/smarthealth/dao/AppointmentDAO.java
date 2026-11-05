package com.smarthealth.dao;

import com.smarthealth.model.Appointment;
import com.smarthealth.util.HibernateUtil;
import java.util.Date;
import java.util.List;
import javax.persistence.TemporalType;

public final class AppointmentDAO {
    private AppointmentDAO() {}

    public static void save(Appointment a) { HibernateUtil.run(s -> s.save(a)); }
    public static void update(Appointment a) { HibernateUtil.run(s -> s.update(a)); }
    public static Appointment findById(Long id) { return HibernateUtil.tx(s -> s.get(Appointment.class, id)); }

    public static List<Appointment> findByPatient(Long patientId) {
        return HibernateUtil.tx(s -> s.createQuery(
                "from Appointment a where a.patient.id = :p order by a.appointmentDate desc, a.timeSlot desc", Appointment.class)
                .setParameter("p", patientId).list());
    }
    public static List<Appointment> findByDoctor(Long doctorId) {
        return HibernateUtil.tx(s -> s.createQuery(
                "from Appointment a where a.doctor.id = :d order by a.appointmentDate, a.timeSlot", Appointment.class)
                .setParameter("d", doctorId).list());
    }
    public static List<Appointment> findAll() {
        return HibernateUtil.tx(s -> s.createQuery(
                "from Appointment a order by a.appointmentDate desc, a.timeSlot desc", Appointment.class).list());
    }

    /** True when the doctor already has an active booking in that slot. */
    public static boolean slotTaken(Long doctorId, Date date, String slot) {
        return HibernateUtil.tx(s -> s.createQuery(
                "select count(a) from Appointment a where a.doctor.id = :d and a.appointmentDate = :dt "
                + "and a.timeSlot = :t and a.status <> 'CANCELLED'", Long.class)
                .setParameter("d", doctorId).setParameter("dt", date, TemporalType.DATE)
                .setParameter("t", slot).uniqueResult()) > 0;
    }

    /** Booked appointments dated between from and to (inclusive) whose reminder has not been sent. */
    public static List<Appointment> pendingReminders(Date from, Date to) {
        return HibernateUtil.tx(s -> s.createQuery(
                "from Appointment a where a.status = 'BOOKED' and a.reminderSent = false "
                + "and a.appointmentDate between :f and :t", Appointment.class)
                .setParameter("f", from, TemporalType.DATE).setParameter("t", to, TemporalType.DATE).list());
    }
}
