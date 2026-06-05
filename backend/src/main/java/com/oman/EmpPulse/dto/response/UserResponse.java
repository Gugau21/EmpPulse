package com.oman.EmpPulse.dto.response;

public class UserResponse {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private boolean isOwner;
    private UserPreferencesResponse preferences;
    private EmployeeProfileResponse employeeProfile;
    private AdminProfileResponse adminProfile;

    public UserResponse(Long id, String name, String surname, String email, boolean isOwner,
                        UserPreferencesResponse preferences,
                        EmployeeProfileResponse employeeProfile,
                        AdminProfileResponse adminProfile) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.isOwner = isOwner;
        this.preferences = preferences;
        this.employeeProfile = employeeProfile;
        this.adminProfile = adminProfile;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public boolean isOwner() { return isOwner; }
    public UserPreferencesResponse getPreferences() { return preferences; }
    public EmployeeProfileResponse getEmployeeProfile() { return employeeProfile; }
    public AdminProfileResponse getAdminProfile() { return adminProfile; }
}
