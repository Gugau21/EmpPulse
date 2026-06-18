package com.oman.EmpPulse.user.dto;

public class BonusVacationDaysResponse {
  private final int year;
  private final int days;

  public BonusVacationDaysResponse(int year, int days) {
    this.year = year;
    this.days = days;
  }

  public int getYear() {
    return year;
  }

  public int getDays() {
    return days;
  }
}
