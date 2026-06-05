package com.oman.EmpPulse.entity;

import java.io.Serializable;
import java.util.Objects;

public class AdminDepartmentId implements Serializable {

    private Long adminId;
    private Long departmentId;

    public AdminDepartmentId() {}

    public AdminDepartmentId(Long adminId, Long departmentId) {
        this.adminId = adminId;
        this.departmentId = departmentId;
    }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdminDepartmentId)) return false;
        AdminDepartmentId that = (AdminDepartmentId) o;
        return Objects.equals(adminId, that.adminId) && Objects.equals(departmentId, that.departmentId);
    }

    @Override
    public int hashCode() { return Objects.hash(adminId, departmentId); }
}
