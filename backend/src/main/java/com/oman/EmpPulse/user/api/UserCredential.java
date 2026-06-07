package com.oman.EmpPulse.user.api;

import java.util.List;

public record UserCredential(Long id, String passwordHash, List<String> authorities) {}
