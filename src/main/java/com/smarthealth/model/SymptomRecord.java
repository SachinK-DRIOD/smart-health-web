package com.smarthealth.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "symptom_records")
public class SymptomRecord implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "patient_id") private Patient patient;
    @Column(length = 500, nullable = false) private String symptoms; // comma separated
    @Column(name = "predicted_disease", length = 100) private String predictedDisease; // null when the ML service was unavailable
    private Double confidence;
    @Column(length = 60) private String specialist;
    @Temporal(TemporalType.TIMESTAMP) @Column(name = "created_at") private Date createdAt = new Date();

    public SymptomRecord() {}
    public SymptomRecord(Patient patient, String symptoms) { this.patient = patient; this.symptoms = symptoms; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getPredictedDisease() { return predictedDisease; }
    public void setPredictedDisease(String predictedDisease) { this.predictedDisease = predictedDisease; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getSpecialist() { return specialist; }
    public void setSpecialist(String specialist) { this.specialist = specialist; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
