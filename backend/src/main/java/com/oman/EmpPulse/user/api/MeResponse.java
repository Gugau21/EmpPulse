package com.oman.EmpPulse.user.api;

public class MeResponse {
  private UserResponse user;

  public MeResponse(UserResponse user) {
    this.user = user;
  }

  public UserResponse getUser() {
    return user;
  }
}
