package com.cinema.reservation.endpoint.rest.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record UpsertReservation(
    @NotNull UUID id, @NotNull UUID projectionId, @NotEmpty List<UUID> seatIds) {}
