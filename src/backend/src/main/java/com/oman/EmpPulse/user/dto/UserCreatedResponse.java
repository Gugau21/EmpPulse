package com.oman.EmpPulse.user.dto;

public class UserCreatedResponse {
  private final Long id;

  public UserCreatedResponse(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }
}
