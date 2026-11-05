package com.smarthealth.model;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", unique = true) private User user;
    @Column(nullable = false, length = 60) private String specialization;

    public Doctor() {}
    public Doctor(User user, String specialization) { this.user = user; this.specialization = specialization; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
