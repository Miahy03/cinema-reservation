package com.cinema.reservation.endpoint.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpsertProjection(
        @NotNull UUID id,
        @NotNull Instant datetime,
        @NotNull BigDecimal seatPrice,
        @NotNull UUID movieId,
        @NotNull UUID roomId) {}