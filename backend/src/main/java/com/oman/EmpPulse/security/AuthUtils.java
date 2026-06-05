package com.oman.EmpPulse.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

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

    public static String getRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow();
    }

    private static boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
