package com.oman.EmpPulse.user.api;

public class UserPreferencesResponse {
  private String theme;
  private String language;

  public UserPreferencesResponse(String theme, String language) {
    this.theme = theme;
    this.language = language;
  }

  public String getTheme() {
    return theme;
  }

  public String getLanguage() {
    return language;
  }
}
