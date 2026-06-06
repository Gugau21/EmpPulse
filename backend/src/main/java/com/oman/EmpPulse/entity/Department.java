package com.oman.EmpPulse.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

  @ManyToMany
  @JoinTable(
      name = "\"Admin_Department\"",
      joinColumns = @JoinColumn(name = "department_id"),
      inverseJoinColumns = @JoinColumn(name = "admin_id"))
  private Set<Admin> admins = new HashSet<>();

  public Department() {}

  public Department(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getDefaultHours() {
    return defaultHours;
  }

  public void setDefaultHours(Long defaultHours) {
    this.defaultHours = defaultHours;
  }

  public Set<Admin> getAdmins() {
    return admins;
  }

  public void setAdmins(Set<Admin> admins) {
    this.admins = admins;
  }
}
