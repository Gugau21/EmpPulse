package com.oman.EmpPulse.dto.response;

public class MeResponse {
    private UserResponse user;

    public MeResponse(UserResponse user) {
        this.user = user;
    }

    public UserResponse getUser() { return user; }
}
