package com.smarthealth.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "appointments")
public class Appointment implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "patient_id") private Patient patient;
    @ManyToOne(optional = false) @JoinColumn(name = "doctor_id") private Doctor doctor;
    @ManyToOne @JoinColumn(name = "symptom_record_id") private SymptomRecord symptomRecord;
    @Temporal(TemporalType.DATE) @Column(name = "appointment_date", nullable = false) private Date appointmentDate;
    @Column(name = "time_slot", nullable = false, length = 5) private String timeSlot; // "09:00"
    @Column(nullable = false, length = 12) private String status = "BOOKED"; // BOOKED, COMPLETED, CANCELLED
    @Column(name = "reminder_sent", nullable = false) private boolean reminderSent = false;
    @Column(name = "doctor_notes", length = 2000) private String doctorNotes;

    public Appointment() {}

    /** Start of the appointment as a LocalDateTime (not persisted: the class uses field access). */
    public LocalDateTime getStart() {
        return LocalDateTime.of(new java.sql.Date(appointmentDate.getTime()).toLocalDate(), LocalTime.parse(timeSlot));
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public SymptomRecord getSymptomRecord() { return symptomRecord; }
    public void setSymptomRecord(SymptomRecord symptomRecord) { this.symptomRecord = symptomRecord; }
    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }
    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
}
