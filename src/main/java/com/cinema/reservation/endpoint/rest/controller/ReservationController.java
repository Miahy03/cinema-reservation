package com.cinema.reservation.endpoint.rest.controller;

import com.cinema.reservation.endpoint.rest.dto.ReservationResponse;
import com.cinema.reservation.endpoint.rest.dto.UpsertReservation;
import com.cinema.reservation.security.UserPrincipal;
import com.cinema.reservation.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ReservationController {
  private final ReservationService reservationService;

  @GetMapping("/reservations")
  public List<ReservationResponse> list() {
    return reservationService.listAll();
  }

  @GetMapping("/reservations/{id}")
  public ReservationResponse getById(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
    return reservationService.getById(id, principal);
  }

  @PutMapping("/reservations")
  public ReservationResponse update(@Valid @RequestBody UpsertReservation request) {
    return reservationService.update(request);
  }
}
