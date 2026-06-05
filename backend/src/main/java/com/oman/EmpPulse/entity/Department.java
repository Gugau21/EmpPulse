package com.oman.EmpPulse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Department\"")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "default_hours")
    private Long defaultHours;

    public Department() {}

    public Department(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getDefaultHours() { return defaultHours; }
    public void setDefaultHours(Long defaultHours) { this.defaultHours = defaultHours; }
}
