package com.cinema.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.cinema.reservation.conf.PostgresConf;
import com.cinema.reservation.endpoint.rest.dto.MovieResponse;
import com.cinema.reservation.endpoint.rest.dto.UpsertMovie;
import com.cinema.reservation.model.Genre;
import com.cinema.reservation.model.Movie;
import com.cinema.reservation.model.UserRole;
import com.cinema.reservation.repository.MovieRepository;
import com.cinema.reservation.repository.ProjectionRepository;
import com.cinema.reservation.repository.ReservationRepository;
import com.cinema.reservation.repository.RoomRepository;
import com.cinema.reservation.repository.SeatRepository;
import com.cinema.reservation.repository.UserRepository;
import com.cinema.reservation.security.JwtService;
import java.time.Duration;
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
class MovieIT {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        new PostgresConf().configureProperties(registry);
    }

    @Autowired TestRestTemplate rest;
    @Autowired JwtService jwtService;
    @Autowired MovieRepository movieRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired ProjectionRepository projectionRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired UserRepository userRepository;

    private UUID client;
    private UUID employee;
    private UUID manager;
    private Movie movie;

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
    }

    private HttpHeaders auth(UUID userId, UserRole role) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtService.generateToken(userId.toString(), role));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void put_movie_forbidden_for_client() {
        var body =
                new UpsertMovie(
                        movie.getId(),
                        "Dune Part Two",
                        Genre.SCI_FI,
                        "Updated description",
                        Duration.ofMinutes(166));

        var response =
                rest.exchange(
                        "/movies",
                        HttpMethod.PUT,
                        new HttpEntity<>(body, auth(client, UserRole.CLIENT)),
                        MovieResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void put_movie_forbidden_for_employee() {
        var body =
                new UpsertMovie(
                        movie.getId(),
                        "Dune Part Two",
                        Genre.SCI_FI,
                        "Updated description",
                        Duration.ofMinutes(166));

        var response =
                rest.exchange(
                        "/movies",
                        HttpMethod.PUT,
                        new HttpEntity<>(body, auth(employee, UserRole.EMPLOYEE)),
                        MovieResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void put_movie_ok_for_manager() {
        var body =
                new UpsertMovie(
                        movie.getId(),
                        "Dune Part Two",
                        Genre.SCI_FI,
                        "Updated description",
                        Duration.ofMinutes(166));

        var response =
                rest.exchange(
                        "/movies",
                        HttpMethod.PUT,
                        new HttpEntity<>(body, auth(manager, UserRole.MANAGER)),
                        MovieResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(movie.getId());
        assertThat(response.getBody().title()).isEqualTo("Dune Part Two");
        assertThat(response.getBody().genre()).isEqualTo(Genre.SCI_FI);
        assertThat(response.getBody().description()).isEqualTo("Updated description");
        assertThat(response.getBody().duration()).isEqualTo(Duration.ofMinutes(166));
    }

    @Test
    void put_movie_not_found_for_manager() {
        var body =
                new UpsertMovie(
                        UUID.randomUUID(),
                        "Unknown movie",
                        Genre.SCI_FI,
                        "Movie not found",
                        Duration.ofMinutes(120));

        var response =
                rest.exchange(
                        "/movies",
                        HttpMethod.PUT,
                        new HttpEntity<>(body, auth(manager, UserRole.MANAGER)),
                        MovieResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}