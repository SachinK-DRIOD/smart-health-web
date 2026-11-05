package com.smarthealth.service;

import com.smarthealth.model.Appointment;
import com.smarthealth.util.Config;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/** JavaMail (SMTP). With no mail.host configured, messages are logged instead of sent (handy in development). */
public final class EmailService {
    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());
    private static final ExecutorService POOL = Executors.newFixedThreadPool(2);
    private EmailService() {}

    public static void sendConfirmation(Appointment a) {
        String subject = "Appointment confirmed: " + describe(a);
        String body = "Hello " + a.getPatient().getUser().getName() + ",\n\nYour appointment is booked.\n\n" + details(a)
                + "\nWe will send a reminder 24 hours before your visit.\n\nSmart Health";
        POOL.submit(() -> send(a.getPatient().getUser().getEmail(), subject, body));
    }

    /** Synchronous so the scheduler only marks a reminder as sent when it actually went out. */
    public static boolean sendReminder(Appointment a) {
        return send(a.getPatient().getUser().getEmail(), "Reminder: " + describe(a),
                "Hello " + a.getPatient().getUser().getName() + ",\n\nThis is a reminder of your appointment.\n\n"
                + details(a) + "\nPlease arrive 10 minutes early.\n\nSmart Health");
    }

    private static String describe(Appointment a) {
        return new SimpleDateFormat("d MMM yyyy").format(a.getAppointmentDate()) + " at " + a.getTimeSlot();
    }
    private static String details(Appointment a) {
        return "Doctor: " + a.getDoctor().getUser().getName() + " (" + a.getDoctor().getSpecialization() + ")\n"
                + "When: " + describe(a) + "\n";
    }

    private static boolean send(String to, String subject, String body) {
        String host = Config.get("mail.host", "");
        if (host.isEmpty()) {
            LOG.info("[mail disabled] To: " + to + " | " + subject + "\n" + body);
            return true;
        }
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", Config.get("mail.port", "587"));
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            String user = Config.get("mail.user", "");
            Session session;
            if (user.isEmpty()) {
                session = Session.getInstance(props);
            } else {
                props.put("mail.smtp.auth", "true");
                String pass = Config.get("mail.pass", "");
                session = Session.getInstance(props, new Authenticator() {
                    @Override protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pass);
                    }
                });
            }
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Config.get("mail.from", "no-reply@smarthealth.local")));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not send email to " + to, e);
            return false;
        }
    }

    public static void shutdown() { POOL.shutdown(); }
}
