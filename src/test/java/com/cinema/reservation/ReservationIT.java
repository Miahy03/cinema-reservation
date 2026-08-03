package com.cinema.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.cinema.reservation.conf.PostgresConf;
import com.cinema.reservation.endpoint.rest.dto.ReservationResponse;
import com.cinema.reservation.endpoint.rest.dto.UpsertReservation;
import com.cinema.reservation.model.Genre;
import com.cinema.reservation.model.Movie;
import com.cinema.reservation.model.Projection;
import com.cinema.reservation.model.Reservation;
import com.cinema.reservation.model.Room;
import com.cinema.reservation.model.Seat;
import com.cinema.reservation.model.User;
import com.cinema.reservation.model.UserRole;
import com.cinema.reservation.repository.MovieRepository;
import com.cinema.reservation.repository.ProjectionRepository;
import com.cinema.reservation.repository.ReservationRepository;
import com.cinema.reservation.repository.RoomRepository;
import com.cinema.reservation.repository.SeatRepository;
import com.cinema.reservation.repository.UserRepository;
import com.cinema.reservation.security.JwtService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReservationIT {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    new PostgresConf().configureProperties(registry);
  }

  @Autowired TestRestTemplate rest;
  @Autowired JwtService jwtService;
  @Autowired ReservationRepository reservationRepository;
  @Autowired ProjectionRepository projectionRepository;
  @Autowired SeatRepository seatRepository;
  @Autowired MovieRepository movieRepository;
  @Autowired RoomRepository roomRepository;
  @Autowired UserRepository userRepository;

  private UUID clientA;
  private UUID clientB;
  private UUID employee;
  private UUID manager;
  private Reservation reservation;
  private Projection projection;
  private Seat seat1;
  private Seat seat2;

  @BeforeEach
  void setUp() {
    reservationRepository.deleteAll();
    seatRepository.deleteAll();
    projectionRepository.deleteAll();
    movieRepository.deleteAll();
    roomRepository.deleteAll();
    userRepository.deleteAll();

    clientA = UUID.randomUUID();
    clientB = UUID.randomUUID();
    employee = UUID.randomUUID();
    manager = UUID.randomUUID();

    Movie movie =
        movieRepository.save(
            Movie.builder()
                .title("Dune")
                .genre(Genre.SCI_FI)
                .description("A science-fiction epic")
                .duration(Duration.ofMinutes(155))
                .build());
    Room room = roomRepository.save(Room.builder().number("1").capacity(100).build());
    seat1 = seatRepository.save(Seat.builder().number("A1").room(room).build());
    seat2 = seatRepository.save(Seat.builder().number("A2").room(room).build());
    projection =
        projectionRepository.save(
            Projection.builder()
                .datetime(Instant.now())
                .seatPrice(new BigDecimal("10.50"))
                .movie(movie)
                .room(room)
                .build());
    User userA =
        userRepository.save(
            User.builder()
                .id(clientA)
                .firstName("Alice")
                .lastName("Doe")
                .email("alice@example.com")
                .password("hashed")
                .role(UserRole.CLIENT)
                .build());
    clientA = userA.getId();
    reservation =
        reservationRepository.save(
            Reservation.builder()
                .createdAt(Instant.now())
                .projection(projection)
                .user(userA)
                .seats(List.of(seat1, seat2))
                .build());
  }

  private HttpHeaders auth(UUID userId, UserRole role) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwtService.generateToken(userId.toString(), role));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @Test
  void get_reservations_forbidden_for_client() {
    var response =
        rest.exchange(
            "/reservations",
            HttpMethod.GET,
            new HttpEntity<>(auth(clientA, UserRole.CLIENT)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void get_reservations_ok_for_employee() {
    var response =
        rest.exchange(
            "/reservations",
            HttpMethod.GET,
            new HttpEntity<>(auth(employee, UserRole.EMPLOYEE)),
            ReservationResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  void get_reservations_ok_for_manager() {
    var response =
        rest.exchange(
            "/reservations",
            HttpMethod.GET,
            new HttpEntity<>(auth(manager, UserRole.MANAGER)),
            ReservationResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  void get_reservation_by_id_ok_for_owner() {
    var response =
        rest.exchange(
            "/reservations/" + reservation.getId(),
            HttpMethod.GET,
            new HttpEntity<>(auth(clientA, UserRole.CLIENT)),
            ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(reservation.getId());
  }

  @Test
  void get_reservation_by_id_forbidden_for_other_client() {
    var response =
        rest.exchange(
            "/reservations/" + reservation.getId(),
            HttpMethod.GET,
            new HttpEntity<>(auth(clientB, UserRole.CLIENT)),
            ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void get_reservation_by_id_ok_for_employee() {
    var response =
        rest.exchange(
            "/reservations/" + reservation.getId(),
            HttpMethod.GET,
            new HttpEntity<>(auth(employee, UserRole.EMPLOYEE)),
            ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void get_reservation_by_id_ok_for_manager() {
    var response =
        rest.exchange(
            "/reservations/" + reservation.getId(),
            HttpMethod.GET,
            new HttpEntity<>(auth(manager, UserRole.MANAGER)),
            ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void get_reservation_by_id_not_found() {
    var response =
        rest.exchange(
            "/reservations/" + UUID.randomUUID(),
            HttpMethod.GET,
            new HttpEntity<>(auth(employee, UserRole.EMPLOYEE)),
            ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void put_reservation_forbidden_for_client() {
    var body =
        new UpsertReservation(reservation.getId(), projection.getId(), List.of(seat1.getId()));
    var response =
        rest.exchange(
            "/reservations",
            HttpMethod.PUT,
            new HttpEntity<>(body, auth(clientA, UserRole.CLIENT)),
            ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void put_reservation_ok_for_employee() {
    var body =
        new UpsertReservation(reservation.getId(), projection.getId(), List.of(seat2.getId()));
    var response =
        rest.exchange(
            "/reservations",
            HttpMethod.PUT,
            new HttpEntity<>(body, auth(employee, UserRole.EMPLOYEE)),
            ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().seats()).hasSize(1);
    assertThat(response.getBody().seats().get(0).id()).isEqualTo(seat2.getId());
  }

  @Test
  void put_reservation_ok_for_manager() {
    var body =
        new UpsertReservation(reservation.getId(), projection.getId(), List.of(seat1.getId()));
    var response =
        rest.exchange(
            "/reservations",
            HttpMethod.PUT,
            new HttpEntity<>(body, auth(manager, UserRole.MANAGER)),
            ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }
}
