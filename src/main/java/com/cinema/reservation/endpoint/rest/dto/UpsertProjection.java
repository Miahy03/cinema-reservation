package com.cinema.reservation.endpoint.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record UpsertProjection(
    @NotNull UUID id,
    @NotNull String datetime,
    @NotNull BigDecimal seatPrice,
    @NotNull UUID movieId,
    @NotNull UUID roomId) {}
