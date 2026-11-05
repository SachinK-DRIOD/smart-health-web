package com.smarthealth.model;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "patients")
public class Patient implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", unique = true) private User user;
    private Integer age;
    @Column(length = 20) private String gender;
    @Column(length = 20) private String phone;
    @Column(name = "medical_history", length = 2000) private String medicalHistory;

    public Patient() {}
    public Patient(User user) { this.user = user; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
}
