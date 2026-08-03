package com.cinema.reservation.endpoint.rest.dto;

import com.cinema.reservation.model.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.UUID;

public record UpsertMovie(
        @NotNull UUID id,
        @NotBlank String title,
        @NotNull Genre genre,
        @NotBlank String description,
        @NotNull Duration duration) {}