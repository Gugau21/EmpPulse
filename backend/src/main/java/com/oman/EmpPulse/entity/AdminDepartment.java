package com.oman.EmpPulse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Admin_Department\"")
@IdClass(AdminDepartmentId.class)
public class AdminDepartment {

    @Id
    @Column(name = "admin_id")
    private Long adminId;

    @Id
    @Column(name = "department_id")
    private Long departmentId;

    public AdminDepartment() {}

    public AdminDepartment(Long adminId, Long departmentId) {
        this.adminId = adminId;
        this.departmentId = departmentId;
    }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}
