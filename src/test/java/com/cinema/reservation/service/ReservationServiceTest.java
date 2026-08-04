package com.cinema.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.cinema.reservation.endpoint.rest.dto.UpsertReservation;
import com.cinema.reservation.model.Movie;
import com.cinema.reservation.model.Projection;
import com.cinema.reservation.model.Reservation;
import com.cinema.reservation.model.Room;
import com.cinema.reservation.model.Seat;
import com.cinema.reservation.model.User;
import com.cinema.reservation.model.UserRole;
import com.cinema.reservation.repository.ProjectionRepository;
import com.cinema.reservation.repository.ReservationRepository;
import com.cinema.reservation.repository.SeatRepository;
import com.cinema.reservation.security.UserPrincipal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

  @Mock ReservationRepository reservationRepository;
  @Mock ProjectionRepository projectionRepository;
  @Mock SeatRepository seatRepository;
  @InjectMocks ReservationService service;

  private static Reservation reservation(UUID id, UUID userId) {
    User user = User.builder().id(userId).firstName("Alice").lastName("Doe").email("a@a").build();
    Room room = Room.builder().id(UUID.randomUUID()).number("1").capacity(100).build();
    Movie movie =
        Movie.builder()
            .id(UUID.randomUUID())
            .title("Dune")
            .duration(Duration.ofMinutes(155))
            .build();
    Projection projection =
        Projection.builder()
            .id(UUID.randomUUID())
            .datetime(Instant.now())
            .movie(movie)
            .room(room)
            .build();
    Seat seat = Seat.builder().id(UUID.randomUUID()).number("A1").build();
    return Reservation.builder()
        .id(id)
        .createdAt(Instant.now())
        .projection(projection)
        .user(user)
        .seats(List.of(seat))
        .build();
  }

  @Test
  void listAll_maps_reservations() {
    when(reservationRepository.findAll())
        .thenReturn(List.of(reservation(UUID.randomUUID(), UUID.randomUUID())));

    var result = service.listAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).user().email()).isEqualTo("a@a");
  }

  @Test
  void getById_returns_own_reservation_for_client() {
    UUID clientId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation(id, clientId)));

    var result = service.getById(id, new UserPrincipal(clientId.toString(), UserRole.CLIENT));

    assertThat(result.id()).isEqualTo(id);
  }

  @Test
  void getById_forbidden_for_client_owning_another_reservation() {
    UUID ownerId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation(id, ownerId)));

    assertThatThrownBy(
            () ->
                service.getById(
                    id, new UserPrincipal(UUID.randomUUID().toString(), UserRole.CLIENT)))
        .isInstanceOf(ReservationForbiddenException.class);
  }

  @Test
  void getById_returns_reservation_for_employee() {
    UUID id = UUID.randomUUID();
    when(reservationRepository.findById(id))
        .thenReturn(Optional.of(reservation(id, UUID.randomUUID())));

    var result =
        service.getById(id, new UserPrincipal(UUID.randomUUID().toString(), UserRole.EMPLOYEE));

    assertThat(result.id()).isEqualTo(id);
  }

  @Test
  void getById_not_found_throws() {
    UUID id = UUID.randomUUID();
    when(reservationRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.getById(
                    id, new UserPrincipal(UUID.randomUUID().toString(), UserRole.MANAGER)))
        .isInstanceOf(ReservationNotFoundException.class);
  }

  @Test
  void update_replaces_projection_and_seats() {
    Reservation existing = reservation(UUID.randomUUID(), UUID.randomUUID());
    UUID projectionId = existing.getProjection().getId();
    UUID seatId = existing.getSeats().get(0).getId();
    when(reservationRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(projectionRepository.findById(projectionId))
        .thenReturn(Optional.of(existing.getProjection()));
    when(seatRepository.findAllById(List.of(seatId)))
        .thenReturn(List.of(existing.getSeats().get(0)));
    when(reservationRepository.save(existing)).thenReturn(existing);

    var result =
        service.update(new UpsertReservation(existing.getId(), projectionId, List.of(seatId)));

    assertThat(result.id()).isEqualTo(existing.getId());
    assertThat(result.seats()).hasSize(1);
  }

  @Test
  void update_reservation_not_found_throws() {
    UUID id = UUID.randomUUID();
    when(reservationRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.update(
                    new UpsertReservation(id, UUID.randomUUID(), List.of(UUID.randomUUID()))))
        .isInstanceOf(ReservationNotFoundException.class);
  }

  @Test
  void update_missing_seat_throws() {
    Reservation existing = reservation(UUID.randomUUID(), UUID.randomUUID());
    UUID seatId = UUID.randomUUID();
    when(reservationRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(projectionRepository.findById(existing.getProjection().getId()))
        .thenReturn(Optional.of(existing.getProjection()));
    when(seatRepository.findAllById(List.of(seatId))).thenReturn(List.of());

    assertThatThrownBy(
            () ->
                service.update(
                    new UpsertReservation(
                        existing.getId(), existing.getProjection().getId(), List.of(seatId))))
        .isInstanceOf(ReservationNotFoundException.class);
  }
}
