package com.oman.EmpPulse.dto.response;

public class EmployeeSummaryResponse {
    private Long id;
    private String name;
    private String surname;
    private Long departmentId;
    private String departmentName;

    public EmployeeSummaryResponse(Long id, String name, String surname,
                                   Long departmentId, String departmentName) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
}
