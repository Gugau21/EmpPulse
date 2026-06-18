package com.oman.EmpPulse.user.internal;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Table(name = "app_user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String surname;

  @Column(nullable = false)
  private String email;

  @Column(name = "pass_hash", nullable = false)
  private String passHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @ColumnTransformer(write = "?::user_theme")
  private UserTheme theme;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @ColumnTransformer(write = "?::user_language")
  private UserLanguage language;

  @Column(name = "is_owner", nullable = false)
  private boolean isOwner;

  @Column(name = "active", nullable = false, insertable = false, updatable = false)
  private boolean active;

  public User() {}

  public User(
      String name,
      String surname,
      String email,
      String passHash,
      UserTheme theme,
      UserLanguage language) {
    this.name = name;
    this.surname = surname;
    this.email = email;
    this.passHash = passHash;
    this.theme = theme;
    this.language = language;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassHash() {
    return passHash;
  }

  public void setPassHash(String passHash) {
    this.passHash = passHash;
  }

  public UserTheme getTheme() {
    return theme;
  }

  public void setTheme(UserTheme theme) {
    this.theme = theme;
  }

  public UserLanguage getLanguage() {
    return language;
  }

  public void setLanguage(UserLanguage language) {
    this.language = language;
  }

  public boolean isOwner() {
    return isOwner;
  }

  public void setOwner(boolean isOwner) {
    this.isOwner = isOwner;
  }

  public boolean isActive() {
    return active;
  }
}
