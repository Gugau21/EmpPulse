package com.oman.EmpPulse.shared.security;

import org.springframework.security.core.Authentication;

public final class AuthUtils {
  private AuthUtils() {}

  public static Long getUserId(Authentication authentication) {
    return (Long) authentication.getPrincipal();
  }

  public static boolean isOwner(Authentication authentication) {
    return hasAuthority(authentication, "OWNER");
  }

  public static boolean isAdmin(Authentication authentication) {
    return hasAuthority(authentication, "ADMIN");
  }

  private static boolean hasAuthority(Authentication authentication, String authority) {
    return authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals(authority));
  }
}
