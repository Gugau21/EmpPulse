package com.oman.EmpPulse.notification.api;

import java.time.LocalDate;

public record LeaveNotificationDetails(
    String leaveType, LocalDate startDate, LocalDate endDate, boolean paid) {}
