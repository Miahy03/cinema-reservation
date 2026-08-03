package com.cinema.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.cinema.reservation.conf.PostgresConf;
import com.cinema.reservation.endpoint.rest.dto.ProjectionResponse;
import com.cinema.reservation.endpoint.rest.dto.UpsertProjection;
import com.cinema.reservation.model.Genre;
import com.cinema.reservation.model.Movie;
import com.cinema.reservation.model.Projection;
import com.cinema.reservation.model.Room;
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
class ProjectionIT {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        new PostgresConf().configureProperties(registry);
    }

    @Autowired TestRestTemplate rest;
    @Autowired JwtService jwtService;
    @Autowired ProjectionRepository projectionRepository;
    @Autowired MovieRepository movieRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired UserRepository userRepository;

    private UUID client;
    private UUID employee;
    private UUID manager;
    private Projection projection;
    private Movie movie;
    private Room room;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        projectionRepository.deleteAll();
        movieRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        client = UUID.randomUUID();
        employee = UUID.randomUUID();
        manager = UUID.randomUUID();

        movie =
                movieRepository.save(
                        Movie.builder()
                                .title("Dune")
                                .genre(Genre.SCI_FI)
                                .description("A science-fiction epic")
                                .duration(Duration.ofMinutes(155))
                                .build());

        room = roomRepository.save(Room.builder().number("1").capacity(100).build());

        projection =
                projectionRepository.save(
                        Projection.builder()
                                .datetime(Instant.now())
                                .seatPrice(new BigDecimal("10.50"))
                                .movie(movie)
                                .room(room)
                                .build());
    }

    private HttpHeaders auth(UUID userId, UserRole role) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtService.generateToken(userId.toString(), role));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void put_projection_forbidden_for_client() {
        var body =
                new UpsertProjection(
                        projection.getId(),
                        Instant.now(),
                        new BigDecimal("12.50"),
                        movie.getId(),
                        room.getId());

        var response =
                rest.exchange(
                        "/projections",
                        HttpMethod.PUT,
                        new HttpEntity<>(body, auth(client, UserRole.CLIENT)),
                        ProjectionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void put_projection_forbidden_for_employee() {
        var body =
                new UpsertProjection(
                        projection.getId(),
                        Instant.now(),
                        new BigDecimal("12.50"),
                        movie.getId(),
                        room.getId());

        var response =
                rest.exchange(
                        "/projections",
                        HttpMethod.PUT,
                        new HttpEntity<>(body, auth(employee, UserRole.EMPLOYEE)),
                        ProjectionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void put_projection_ok_for_manager() {
        Instant newDatetime = Instant.now().plusSeconds(3600);

        var body =
                new UpsertProjection(
                        projection.getId(),
                        newDatetime,
                        new BigDecimal("12.50"),
                        movie.getId(),
                        room.getId());

        var response =
                rest.exchange(
                        "/projections",
                        HttpMethod.PUT,
                        new HttpEntity<>(body, auth(manager, UserRole.MANAGER)),
                        ProjectionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(projection.getId());
        assertThat(response.getBody().seatPrice()).isEqualByComparingTo("12.50");
        assertThat(response.getBody().movie().id()).isEqualTo(movie.getId());
        assertThat(response.getBody().room().id()).isEqualTo(room.getId());
    }

    @Test
    void put_projection_not_found_for_manager() {
        var body =
                new UpsertProjection(
                        UUID.randomUUID(),
                        Instant.now(),
                        new BigDecimal("12.50"),
                        movie.getId(),
                        room.getId());

        var response =
                rest.exchange(
                        "/projections",
                        HttpMethod.PUT,
                        new HttpEntity<>(body, auth(manager, UserRole.MANAGER)),
                        ProjectionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void get_projections_ok_for_client() {
        var response =
                rest.exchange(
                        "/projections",
                        HttpMethod.GET,
                        new HttpEntity<>(auth(client, UserRole.CLIENT)),
                        ProjectionResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void get_projections_ok_for_employee() {
        var response =
                rest.exchange(
                        "/projections",
                        HttpMethod.GET,
                        new HttpEntity<>(auth(employee, UserRole.EMPLOYEE)),
                        ProjectionResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void get_projections_ok_for_manager() {
        var response =
                rest.exchange(
                        "/projections",
                        HttpMethod.GET,
                        new HttpEntity<>(auth(manager, UserRole.MANAGER)),
                        ProjectionResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void get_projections_ok_without_authentication() {
        var response =
                rest.exchange(
                        "/projections",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        ProjectionResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }
}