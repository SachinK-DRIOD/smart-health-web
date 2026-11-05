package com.smarthealth.util;

import com.smarthealth.dao.DoctorDAO;
import com.smarthealth.dao.UserDAO;
import com.smarthealth.model.Doctor;
import com.smarthealth.model.User;
import com.smarthealth.service.EmailService;
import com.smarthealth.service.ReminderScheduler;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** On startup: builds the schema (Hibernate), seeds demo staff accounts, starts the reminder job. */
@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent e) {
        HibernateUtil.factory();
        if (UserDAO.countByRole("ADMIN") == 0) {
            UserDAO.save(new User("Site Admin", "admin@clinic.test", PasswordUtil.hash("Admin@123"), "ADMIN"));
        }
        if (DoctorDAO.count() == 0) {
            String[][] staff = {
                {"Dr. Asha Verma", "asha@clinic.test", "General Physician"},
                {"Dr. Rohan Mehta", "rohan@clinic.test", "Pulmonologist"},
                {"Dr. Neha Kapoor", "neha@clinic.test", "Dermatologist"},
                {"Dr. Imran Sheikh", "imran@clinic.test", "Gastroenterologist"},
                {"Dr. Priya Nair", "priya@clinic.test", "Neurologist"},
                {"Dr. Arjun Singh", "arjun@clinic.test", "Cardiologist"}};
            for (String[] s : staff) {
                User u = new User(s[0], s[1], PasswordUtil.hash("Doctor@123"), "DOCTOR");
                UserDAO.save(u);
                DoctorDAO.save(new Doctor(u, s[2]));
            }
        }
        ReminderScheduler.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        ReminderScheduler.stop();
        EmailService.shutdown();
        HibernateUtil.shutdown();
    }
}
