package com.oman.EmpPulse.leave.internal;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "leave")
public class Leave {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "employee_id", nullable = false)
  private Long employeeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @ColumnTransformer(write = "?::leave_type")
  private LeaveType type;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(nullable = false)
  private boolean paid;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @ColumnTransformer(write = "?::leave_status")
  private LeaveStatus status;

  private String description;

  @Column(name = "admin_reviewer_id")
  private Long adminReviewerId;

  @Column(name = "admin_comment")
  private String adminComment;

  @Column(name = "modification_id")
  private Long modificationId;

  @Generated(event = EventType.INSERT)
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Generated(event = {EventType.INSERT, EventType.UPDATE})
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public Leave() {}

  public Leave(
      Long employeeId,
      LeaveType type,
      LocalDate startDate,
      LocalDate endDate,
      boolean paid,
      LeaveStatus status,
      String description,
      Long adminReviewerId,
      String adminComment) {
    this.employeeId = employeeId;
    this.type = type;
    this.startDate = startDate;
    this.endDate = endDate;
    this.paid = paid;
    this.status = status;
    this.description = description;
    this.adminReviewerId = adminReviewerId;
    this.adminComment = adminComment;
  }

  public Long getId() {
    return id;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public LeaveType getType() {
    return type;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public boolean isPaid() {
    return paid;
  }

  public LeaveStatus getStatus() {
    return status;
  }

  public String getDescription() {
    return description;
  }

  public Long getAdminReviewerId() {
    return adminReviewerId;
  }

  public String getAdminComment() {
    return adminComment;
  }

  public Long getModificationId() {
    return modificationId;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
