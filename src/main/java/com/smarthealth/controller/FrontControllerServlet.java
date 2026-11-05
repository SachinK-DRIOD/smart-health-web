package com.smarthealth.controller;

import com.smarthealth.dao.*;
import com.smarthealth.filter.AuthFilter;
import com.smarthealth.model.*;
import com.smarthealth.service.EmailService;
import com.smarthealth.service.PdfService;
import com.smarthealth.service.PredictionService;
import com.smarthealth.util.PasswordUtil;
import com.smarthealth.util.Symptoms;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * MVC controller: every page request lands here. GET renders a JSP view from /WEB-INF/views,
 * POST validates input, calls the DAOs/services, then redirects (Post-Redirect-Get).
 * Role checks and CSRF are enforced earlier by AuthFilter.
 */
@WebServlet(urlPatterns = {"/register", "/login", "/logout", "/dashboard", "/patient/form", "/symptoms", "/result",
        "/appointment/book", "/appointments", "/appointment/cancel", "/report/pdf",
        "/doctor/dashboard", "/doctor/notes", "/admin"})
public class FrontControllerServlet extends HttpServlet {
    private static final List<String> STEPS = List.of("Your details", "Symptoms", "Appointment", "Report");
    private static final Set<String> GENDERS = Set.of("Female", "Male", "Other", "Prefer not to say");

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { route(req, resp, false); }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { route(req, resp, true); }

    private void route(HttpServletRequest req, HttpServletResponse resp, boolean post) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/register": if (post) registerPost(req, resp); else view(req, resp, "register", "Create account"); break;
            case "/login": if (post) loginPost(req, resp); else view(req, resp, "login", "Sign in"); break;
            case "/logout": if (post) logout(req, resp); else redirect(req, resp, "/dashboard"); break;
            case "/dashboard": dashboard(req, resp); break;
            case "/patient/form": if (post) patientFormPost(req, resp); else patientFormGet(req, resp); break;
            case "/symptoms": if (post) symptomsPost(req, resp); else symptomsGet(req, resp); break;
            case "/result": result(req, resp); break;
            case "/appointment/book": if (post) bookPost(req, resp); else bookGet(req, resp); break;
            case "/appointments": appointments(req, resp); break;
            case "/appointment/cancel": if (post) cancel(req, resp); else redirect(req, resp, "/appointments"); break;
            case "/report/pdf": pdf(req, resp); break;
            case "/doctor/dashboard": doctorDashboard(req, resp); break;
            case "/doctor/notes": if (post) doctorNotes(req, resp); else redirect(req, resp, "/doctor/dashboard"); break;
            case "/admin": admin(req, resp); break;
            default: resp.sendError(404);
        }
    }

    // ------------------------------------------------------------ authentication

    private void registerPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = param(req, "name"), email = param(req, "email").toLowerCase();
        String pw = req.getParameter("password");
        String error = null;
        if (name.length() < 2 || name.length() > 100) error = "Enter your full name.";
        else if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || email.length() > 150) error = "Enter a valid email address.";
        else if (!PasswordUtil.isStrong(pw)) error = "Use a password of 8 or more characters with at least one letter and one number.";
        else if (!pw.equals(req.getParameter("confirm"))) error = "The two passwords do not match.";
        else if (UserDAO.findByEmail(email) != null) error = "An account with this email already exists. Sign in instead.";
        if (error != null) { req.setAttribute("error", error); view(req, resp, "register", "Create account"); return; }

        UserDAO.save(new User(name, email, PasswordUtil.hash(pw), "PATIENT"));
        flash(req, "Account created. Sign in to continue.");
        redirect(req, resp, "/login");
    }

    private void loginPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = UserDAO.findByEmail(param(req, "email"));
        if (u == null || !PasswordUtil.verify(req.getParameter("password"), u.getPasswordHash())) {
            req.setAttribute("error", "Incorrect email or password.");
            view(req, resp, "login", "Sign in");
            return;
        }
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate(); // new session id on login (prevents session fixation)
        HttpSession session = req.getSession(true);
        u.setPasswordHash(null); // the hash never needs to live in the session
        session.setAttribute("user", u);
        session.setAttribute("csrf", AuthFilter.newToken());
        session.setMaxInactiveInterval(30 * 60);
        redirect(req, resp, "/dashboard");
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    // ------------------------------------------------------------ patient flow

    private void dashboard(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = user(req);
        if ("DOCTOR".equals(user.getRole())) { redirect(req, resp, "/doctor/dashboard"); return; }
        if ("ADMIN".equals(user.getRole())) { redirect(req, resp, "/admin"); return; }

        Patient patient = patient(req);
        SymptomRecord record = currentRecord(req, patient);
        int step = 1;
        if (patient != null) {
            step = 2;
            if (record != null) {
                step = 3;
                final Long rid = record.getId();
                if (AppointmentDAO.findByPatient(patient.getId()).stream()
                        .anyMatch(a -> a.getSymptomRecord() != null && rid.equals(a.getSymptomRecord().getId()))) step = 4;
            }
        }
        req.setAttribute("steps", STEPS);
        req.setAttribute("step", step);
        req.setAttribute("record", record);
        view(req, resp, "dashboard", "Dashboard");
    }

    private void patientFormGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("p", patient(req));
        req.setAttribute("genders", GENDERS);
        view(req, resp, "patient-form", "Your details");
    }

    private void patientFormPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ageStr = param(req, "age"), gender = param(req, "gender"), phone = param(req, "phone"), history = param(req, "history");
        Integer age = null;
        String error = null;
        try { age = Integer.valueOf(ageStr); } catch (NumberFormatException ignored) { }
        if (age == null || age < 0 || age > 120) error = "Enter an age between 0 and 120.";
        else if (!GENDERS.contains(gender)) error = "Choose a gender option.";
        else if (!phone.matches("^[0-9+\\-() ]{7,20}$")) error = "Enter a phone number with 7 to 20 digits.";
        else if (history.length() > 2000) error = "Medical history is limited to 2000 characters.";

        Patient existing = patient(req);
        Patient p = existing != null ? existing : new Patient(user(req));
        if (error != null) { // re-show the form with what was typed, without touching the saved record
            Patient draft = new Patient(user(req));
            draft.setAge(age); draft.setGender(gender); draft.setPhone(phone); draft.setMedicalHistory(history);
            req.setAttribute("error", error);
            req.setAttribute("p", draft);
            req.setAttribute("genders", GENDERS);
            view(req, resp, "patient-form", "Your details");
            return;
        }
        p.setAge(age); p.setGender(gender); p.setPhone(phone); p.setMedicalHistory(history);
        PatientDAO.saveOrUpdate(p);
        req.getSession().setAttribute("patient", p); // visit data lives in the session
        flash(req, "Details saved.");
        redirect(req, resp, "/symptoms");
    }

    private void symptomsGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (patient(req) == null) { flash(req, "Fill in your details first."); redirect(req, resp, "/patient/form"); return; }
        req.setAttribute("symptomList", Symptoms.ALL);
        view(req, resp, "symptoms", "Check symptoms");
    }

    private void symptomsPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Patient patient = patient(req);
        if (patient == null) { redirect(req, resp, "/patient/form"); return; }
        String[] chosen = req.getParameterValues("symptom");
        List<String> symptoms = chosen == null ? List.of() : Arrays.stream(chosen)
                .filter(Symptoms.ALL::contains).distinct().collect(Collectors.toList()); // whitelist only
        if (symptoms.isEmpty()) {
            req.setAttribute("error", "Select at least one symptom.");
            req.setAttribute("symptomList", Symptoms.ALL);
            view(req, resp, "symptoms", "Check symptoms");
            return;
        }
        SymptomRecord rec = new SymptomRecord(patient, String.join(", ", symptoms));
        PredictionService.Prediction pred = PredictionService.predict(symptoms);
        if (pred != null) {
            rec.setPredictedDisease(pred.disease);
            rec.setConfidence(pred.confidence);
            rec.setSpecialist(pred.specialist);
        } else {
            rec.setSpecialist("General Physician");
        }
        SymptomDAO.save(rec);
        HttpSession s = req.getSession();
        s.setAttribute("visitSymptoms", symptoms);
        s.setAttribute("visitRecordId", rec.getId());
        redirect(req, resp, "/result");
    }

    private void result(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SymptomRecord rec = currentRecord(req, patient(req));
        if (rec == null) { redirect(req, resp, "/symptoms"); return; }
        req.setAttribute("record", rec);
        view(req, resp, "result", "Your result");
    }

    private void bookGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Patient patient = patient(req);
        if (patient == null) { flash(req, "Fill in your details first."); redirect(req, resp, "/patient/form"); return; }
        showBooking(req, resp, currentRecord(req, patient));
    }

    private void showBooking(HttpServletRequest req, HttpServletResponse resp, SymptomRecord rec) throws ServletException, IOException {
        String recommended = rec == null ? null : rec.getSpecialist();
        List<Doctor> doctors = new ArrayList<>(DoctorDAO.findAll());
        doctors.sort(Comparator.comparing((Doctor d) -> !d.getSpecialization().equals(recommended))); // stable: matches first
        req.setAttribute("doctors", doctors);
        req.setAttribute("recommended", recommended);
        req.setAttribute("slots", Symptoms.SLOTS);
        req.setAttribute("minDate", LocalDate.now().toString());
        view(req, resp, "book", "Book appointment");
    }

    private void bookPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Patient patient = patient(req);
        if (patient == null) { redirect(req, resp, "/patient/form"); return; }
        SymptomRecord rec = currentRecord(req, patient);
        String error = null;
        Doctor doctor = null;
        LocalDateTime start = null;
        String slot = param(req, "slot");
        try {
            doctor = DoctorDAO.findById(Long.valueOf(param(req, "doctorId")));
            start = LocalDateTime.of(LocalDate.parse(param(req, "date")), LocalTime.parse(slot));
        } catch (NumberFormatException | DateTimeParseException ignored) { }
        if (doctor == null || start == null || !Symptoms.SLOTS.contains(slot)) error = "Choose a doctor, a date and a time slot.";
        else if (!start.isAfter(LocalDateTime.now())) error = "Choose a time in the future.";
        else if (start.isAfter(LocalDateTime.now().plusDays(90))) error = "Appointments can be booked up to 90 days ahead.";
        else if (AppointmentDAO.slotTaken(doctor.getId(), java.sql.Date.valueOf(start.toLocalDate()), slot))
            error = "That slot is already taken. Pick another time.";
        if (error != null) { req.setAttribute("error", error); showBooking(req, resp, rec); return; }

        Appointment a = new Appointment();
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setSymptomRecord(rec);
        a.setAppointmentDate(java.sql.Date.valueOf(start.toLocalDate()));
        a.setTimeSlot(slot);
        AppointmentDAO.save(a);
        EmailService.sendConfirmation(a);
        flash(req, "Appointment booked. A confirmation email is on its way.");
        redirect(req, resp, "/appointments");
    }

    private void appointments(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Patient patient = patient(req);
        req.setAttribute("appointments", patient == null ? List.of() : AppointmentDAO.findByPatient(patient.getId()));
        view(req, resp, "appointments", "Appointments");
    }

    private void cancel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Patient patient = patient(req);
        Appointment a = findAppointment(req);
        if (a != null && patient != null && a.getPatient().getId().equals(patient.getId()) && "BOOKED".equals(a.getStatus())) {
            a.setStatus("CANCELLED");
            AppointmentDAO.update(a);
            flash(req, "Appointment cancelled.");
        }
        redirect(req, resp, "/appointments");
    }

    private void pdf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Appointment a = findAppointment(req);
        User u = user(req);
        boolean ok = a != null && ("PATIENT".equals(u.getRole()) ? a.getPatient().getUser().getId().equals(u.getId())
                : a.getDoctor().getUser().getId().equals(u.getId()));
        if (!ok) { resp.sendError(404); return; } // 404, not 403: do not reveal that the id exists
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=visit-report-" + a.getId() + ".pdf");
        try {
            PdfService.write(a, resp.getOutputStream());
        } catch (Exception e) {
            throw new IOException("Could not build the PDF report", e);
        }
    }

    // ------------------------------------------------------------ doctor and admin

    private void doctorDashboard(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Doctor d = DoctorDAO.findByUserId(user(req).getId());
        req.setAttribute("doctor", d);
        req.setAttribute("appointments", d == null ? List.of() : AppointmentDAO.findByDoctor(d.getId()));
        view(req, resp, "doctor-dashboard", "Appointments");
    }

    private void doctorNotes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Doctor d = DoctorDAO.findByUserId(user(req).getId());
        Appointment a = findAppointment(req);
        String notes = param(req, "notes");
        if (a != null && d != null && a.getDoctor().getId().equals(d.getId()) && !"CANCELLED".equals(a.getStatus()) && notes.length() <= 2000) {
            a.setDoctorNotes(notes);
            if ("1".equals(req.getParameter("complete"))) a.setStatus("COMPLETED");
            AppointmentDAO.update(a);
            flash(req, "Notes saved.");
        } else {
            flash(req, "Could not save the notes. They may be too long, or the appointment was cancelled.");
        }
        redirect(req, resp, "/doctor/dashboard");
    }

    private void admin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("users", UserDAO.findAll());
        req.setAttribute("appointments", AppointmentDAO.findAll());
        view(req, resp, "admin", "Admin");
    }

    // ------------------------------------------------------------ helpers

    private static void view(HttpServletRequest req, HttpServletResponse resp, String name, String title) throws ServletException, IOException {
        req.setAttribute("title", title);
        req.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(req, resp);
    }
    private static void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }
    private static void flash(HttpServletRequest req, String msg) { req.getSession().setAttribute("flash", msg); }
    private static String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }
    private static User user(HttpServletRequest req) { return (User) req.getSession().getAttribute("user"); }

    /** The patient record for the signed-in user: from the session, else loaded once from the database. */
    private static Patient patient(HttpServletRequest req) {
        HttpSession s = req.getSession();
        Patient p = (Patient) s.getAttribute("patient");
        if (p == null) {
            p = PatientDAO.findByUserId(user(req).getId());
            if (p != null) s.setAttribute("patient", p);
        }
        return p;
    }

    /** The symptom record from this visit, only if it belongs to the signed-in patient. */
    private static SymptomRecord currentRecord(HttpServletRequest req, Patient patient) {
        Long id = (Long) req.getSession().getAttribute("visitRecordId");
        if (id == null || patient == null) return null;
        SymptomRecord r = SymptomDAO.findById(id);
        return (r != null && r.getPatient().getId().equals(patient.getId())) ? r : null;
    }

    private static Appointment findAppointment(HttpServletRequest req) {
        try { return AppointmentDAO.findById(Long.valueOf(param(req, "id"))); }
        catch (NumberFormatException e) { return null; }
    }
}
