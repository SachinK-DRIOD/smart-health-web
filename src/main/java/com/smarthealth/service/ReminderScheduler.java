package com.smarthealth.service;

import com.smarthealth.dao.AppointmentDAO;
import com.smarthealth.model.Appointment;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Every 15 minutes, emails patients whose appointment starts within the next 24 hours. */
public final class ReminderScheduler {
    private static final Logger LOG = Logger.getLogger(ReminderScheduler.class.getName());
    private static ScheduledExecutorService executor;
    private ReminderScheduler() {}

    public static synchronized void start() {
        if (executor != null) return;
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(ReminderScheduler::runOnce, 1, 15, TimeUnit.MINUTES);
    }

    static void runOnce() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();
            for (Appointment a : AppointmentDAO.pendingReminders(Date.valueOf(today), Date.valueOf(today.plusDays(1)))) {
                LocalDateTime start = a.getStart();
                if (start.isAfter(now) && Duration.between(now, start).toHours() < 24 && EmailService.sendReminder(a)) {
                    a.setReminderSent(true);
                    AppointmentDAO.update(a);
                }
            }
        } catch (Exception e) { // never let one failure cancel the schedule
            LOG.log(Level.WARNING, "Reminder run failed", e);
        }
    }

    public static synchronized void stop() {
        if (executor != null) { executor.shutdownNow(); executor = null; }
    }
}
