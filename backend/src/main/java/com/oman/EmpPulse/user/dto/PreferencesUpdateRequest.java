package com.oman.EmpPulse.user.dto;

public class PreferencesUpdateRequest {
  private String theme;
  private String language;

  public String getTheme() {
    return theme;
  }

  public void setTheme(String theme) {
    this.theme = theme;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
