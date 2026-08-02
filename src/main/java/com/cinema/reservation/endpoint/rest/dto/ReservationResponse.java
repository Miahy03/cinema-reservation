package com.cinema.reservation.endpoint.rest.dto;

import com.cinema.reservation.model.Genre;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
    UUID id, Instant createdAt, ProjectionInfo projection, UserInfo user, List<SeatInfo> seats) {

  public record ProjectionInfo(
      UUID id, Instant datetime, BigDecimal seatPrice, MovieInfo movie, RoomInfo room) {}

  public record MovieInfo(
      UUID id, String title, Genre genre, String description, Duration duration) {}

  public record RoomInfo(UUID id, String number, int capacity) {}

  public record UserInfo(UUID id, String firstName, String lastName, String email) {}

  public record SeatInfo(UUID id, String number) {}
}
