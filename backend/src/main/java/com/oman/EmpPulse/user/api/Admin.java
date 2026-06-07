package com.oman.EmpPulse.user.api;

import com.oman.EmpPulse.department.api.Department;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "\"Admin\"")
public class Admin {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToMany(mappedBy = "admins")
  private Set<Department> departments = new HashSet<>();

  public Admin() {}

  public Admin(Long userId) {
    this.userId = userId;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  /**
   * Returns the departments this admin oversees. This is the inverse side of the Admin_Department
   * join table.
   *
   * <p><strong>Warning:</strong> Be careful when calling this and Department.setAdmins(...) in the
   * same transaction. This can lead to inconsistent data.
   *
   * <p>Access admin.getDepartments() only for display/queries in read-only transactions; mutations
   * must happen on the Department side.
   *
   * @return the set of departments this admin is assigned to.
   */
  public Set<Department> getDepartments() {
    return departments;
  }
}
