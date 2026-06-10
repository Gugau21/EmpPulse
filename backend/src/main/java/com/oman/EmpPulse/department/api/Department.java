package com.oman.EmpPulse.department.api;

import com.oman.EmpPulse.user.api.Admin;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "department")
public class Department {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String name;

  @Column(name = "week_schedule_id")
  private Long weekScheduleId;

  @ManyToMany
  @JoinTable(
      name = "admin_department",
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

  public Long getWeekScheduleId() {
    return weekScheduleId;
  }

  public void setWeekScheduleId(Long weekScheduleId) {
    this.weekScheduleId = weekScheduleId;
  }

  public Set<Admin> getAdmins() {
    return admins;
  }

  /**
   * Replaces the entire set of admins assigned to this department. This is the owning side of the
   * Admin_Department join table. Mutations here (add, remove, clear) persist to the database.
   * Changes to Admin.departments (the inverse side) are not persisted.
   *
   * @param admins the new set of admins to assign to this department
   */
  public void setAdmins(Set<Admin> admins) {
    this.admins = admins;
  }
}
