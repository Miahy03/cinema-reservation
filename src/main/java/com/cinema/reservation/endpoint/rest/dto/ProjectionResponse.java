package com.cinema.reservation.endpoint.rest.dto;

import com.cinema.reservation.model.Genre;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record ProjectionResponse(
    UUID id, Instant datetime, BigDecimal seatPrice, MovieInfo movie, RoomInfo room) {

  public record MovieInfo(
      UUID id, String title, Genre genre, String description, Duration duration) {}

  public record RoomInfo(UUID id, String number, int capacity) {}
}
