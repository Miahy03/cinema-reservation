package com.cinema.reservation.service;

import com.cinema.reservation.endpoint.rest.dto.ReservationResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ReservationService {
  private final ReservationRepository reservationRepository;
  private final ProjectionRepository projectionRepository;
  private final SeatRepository seatRepository;

  @Transactional(readOnly = true)
  public List<ReservationResponse> listAll() {
    return reservationRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ReservationResponse getById(UUID id, UserPrincipal principal) {
    Reservation reservation = findById(id);
    if (principal.role() == UserRole.CLIENT
        && !reservation.getUser().getId().equals(UUID.fromString(principal.id()))) {
      throw new ReservationForbiddenException();
    }
    return toResponse(reservation);
  }

  @Transactional
  public ReservationResponse update(UpsertReservation request) {
    Reservation reservation = findById(request.id());
    Projection projection =
        projectionRepository
            .findById(request.projectionId())
            .orElseThrow(ReservationNotFoundException::new);
    List<Seat> seats = seatRepository.findAllById(request.seatIds());
    if (seats.size() != request.seatIds().size()) {
      throw new ReservationNotFoundException();
    }
    reservation.setProjection(projection);
    reservation.setSeats(new ArrayList<>(seats));
    return toResponse(reservationRepository.save(reservation));
  }

  private Reservation findById(UUID id) {
    return reservationRepository.findById(id).orElseThrow(ReservationNotFoundException::new);
  }

  private ReservationResponse toResponse(Reservation reservation) {
    return new ReservationResponse(
        reservation.getId(),
        reservation.getCreatedAt().toString(),
        toProjection(reservation.getProjection()),
        toUser(reservation.getUser()),
        reservation.getSeats().stream().map(this::toSeat).toList());
  }

  private ReservationResponse.ProjectionInfo toProjection(Projection projection) {
    return new ReservationResponse.ProjectionInfo(
        projection.getId(),
        projection.getDatetime().toString(),
        projection.getSeatPrice(),
        toMovie(projection.getMovie()),
        toRoom(projection.getRoom()));
  }

  private ReservationResponse.MovieInfo toMovie(Movie movie) {
    return new ReservationResponse.MovieInfo(
        movie.getId(),
        movie.getTitle(),
        movie.getGenre(),
        movie.getDescription(),
        movie.getDuration().toString());
  }

  private ReservationResponse.RoomInfo toRoom(Room room) {
    return new ReservationResponse.RoomInfo(room.getId(), room.getNumber(), room.getCapacity());
  }

  private ReservationResponse.UserInfo toUser(User user) {
    return new ReservationResponse.UserInfo(
        user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
  }

  private ReservationResponse.SeatInfo toSeat(Seat seat) {
    return new ReservationResponse.SeatInfo(seat.getId(), seat.getNumber());
  }
}
