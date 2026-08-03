package com.cinema.reservation.security;

import com.cinema.reservation.model.UserRole;

public record UserPrincipal(String id, UserRole role) {}
