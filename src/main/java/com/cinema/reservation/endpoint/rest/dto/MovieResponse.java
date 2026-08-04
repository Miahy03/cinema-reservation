package com.cinema.reservation.endpoint.rest.dto;

import com.cinema.reservation.model.Genre;
import java.util.UUID;

public record MovieResponse(
    UUID id, String title, Genre genre, String description, String duration) {}
