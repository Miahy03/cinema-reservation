package com.cinema.reservation.security;

import com.cinema.reservation.model.UserRole;

public record JwtClaims(String userId, UserRole role) {}
