package com.oman.EmpPulse.dto.response;

public class UserSummaryResponse {
    private Long id;
    private String name;
    private String surname;
    private String email;

    public UserSummaryResponse(Long id, String name, String surname, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
}
