package com.oman.EmpPulse.user.api;

import com.oman.EmpPulse.department.api.Department;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "admin")
public class Admin {

  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  @Generated(event = {EventType.INSERT, EventType.UPDATE})
  @Column(name = "active", nullable = false)
  private boolean active;

  @ManyToMany(mappedBy = "admins")
  private Set<Department> departments = new HashSet<>();

  public Admin() {}

  public Admin(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public boolean isActive() {
    return active;
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
