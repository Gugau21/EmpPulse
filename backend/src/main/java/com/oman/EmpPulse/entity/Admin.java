package com.oman.EmpPulse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Admin\"")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public Admin() {}

    public Admin(Long userId) {
        this.userId = userId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
