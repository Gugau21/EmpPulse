package com.oman.EmpPulse.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "\"Employee\"")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "vacation_balance", nullable = false)
    private int vacationBalance;

    @Column(name = "date_of_hiring", nullable = false)
    private LocalDate dateOfHiring;

    public Employee() {}

    public Employee(Long userId, Long departmentId, int vacationBalance, LocalDate dateOfHiring) {
        this.userId = userId;
        this.departmentId = departmentId;
        this.vacationBalance = vacationBalance;
        this.dateOfHiring = dateOfHiring;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public int getVacationBalance() { return vacationBalance; }
    public void setVacationBalance(int vacationBalance) { this.vacationBalance = vacationBalance; }
    public LocalDate getDateOfHiring() { return dateOfHiring; }
    public void setDateOfHiring(LocalDate dateOfHiring) { this.dateOfHiring = dateOfHiring; }
}
