package com.cinema.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.cinema.reservation.endpoint.rest.dto.UpsertProjection;
import com.cinema.reservation.model.Genre;
import com.cinema.reservation.model.Movie;
import com.cinema.reservation.model.Projection;
import com.cinema.reservation.model.Room;
import com.cinema.reservation.repository.MovieRepository;
import com.cinema.reservation.repository.ProjectionRepository;
import com.cinema.reservation.repository.RoomRepository;
import java.math.BigDecimal;
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
class ProjectionServiceTest {

  @Mock ProjectionRepository projectionRepository;
  @Mock MovieRepository movieRepository;
  @Mock RoomRepository roomRepository;

  @InjectMocks ProjectionService service;

  private static Projection projection(UUID id) {
    Movie movie =
        Movie.builder()
            .id(UUID.randomUUID())
            .title("Dune")
            .genre(Genre.SCI_FI)
            .description("A science-fiction epic")
            .duration(Duration.ofMinutes(155))
            .build();

    Room room = Room.builder().id(UUID.randomUUID()).number("1").capacity(100).build();

    return Projection.builder()
        .id(id)
        .datetime(Instant.now())
        .seatPrice(new BigDecimal("10.50"))
        .movie(movie)
        .room(room)
        .build();
  }

  @Test
  void listAll_maps_projections() {
    Projection projection = projection(UUID.randomUUID());

    when(projectionRepository.findAll()).thenReturn(List.of(projection));

    var result = service.listAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(projection.getId());
    assertThat(result.get(0).movie().title()).isEqualTo("Dune");
    assertThat(result.get(0).room().number()).isEqualTo("1");
  }

  @Test
  void update_updates_projection() {
    Projection existing = projection(UUID.randomUUID());

    Movie newMovie =
        Movie.builder()
            .id(UUID.randomUUID())
            .title("Interstellar")
            .genre(Genre.SCI_FI)
            .description("Space movie")
            .duration(Duration.ofMinutes(169))
            .build();

    Room newRoom = Room.builder().id(UUID.randomUUID()).number("2").capacity(150).build();

    Instant newDatetime = Instant.now().plusSeconds(3600);

    when(projectionRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

    when(movieRepository.findById(newMovie.getId())).thenReturn(Optional.of(newMovie));

    when(roomRepository.findById(newRoom.getId())).thenReturn(Optional.of(newRoom));

    when(projectionRepository.save(existing)).thenReturn(existing);

    var result =
        service.update(
            new UpsertProjection(
                existing.getId(),
                newDatetime,
                new BigDecimal("15.00"),
                newMovie.getId(),
                newRoom.getId()));

    assertThat(result.id()).isEqualTo(existing.getId());
    assertThat(result.datetime()).isEqualTo(newDatetime);
    assertThat(result.seatPrice()).isEqualByComparingTo("15.00");
    assertThat(result.movie().id()).isEqualTo(newMovie.getId());
    assertThat(result.room().id()).isEqualTo(newRoom.getId());
  }

  @Test
  void update_projection_not_found_throws() {
    UUID id = UUID.randomUUID();

    when(projectionRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.update(
                    new UpsertProjection(
                        id,
                        Instant.now(),
                        new BigDecimal("10.00"),
                        UUID.randomUUID(),
                        UUID.randomUUID())))
        .isInstanceOf(ProjectionNotFoundException.class);
  }

  @Test
  void update_movie_not_found_throws() {
    Projection existing = projection(UUID.randomUUID());
    UUID movieId = UUID.randomUUID();

    when(projectionRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

    when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.update(
                    new UpsertProjection(
                        existing.getId(),
                        Instant.now(),
                        new BigDecimal("10.00"),
                        movieId,
                        UUID.randomUUID())))
        .isInstanceOf(MovieNotFoundException.class);
  }

  @Test
  void update_room_not_found_throws() {
    Projection existing = projection(UUID.randomUUID());
    UUID roomId = UUID.randomUUID();

    when(projectionRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

    when(movieRepository.findById(existing.getMovie().getId()))
        .thenReturn(Optional.of(existing.getMovie()));

    when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.update(
                    new UpsertProjection(
                        existing.getId(),
                        Instant.now(),
                        new BigDecimal("10.00"),
                        existing.getMovie().getId(),
                        roomId)))
        .isInstanceOf(RoomNotFoundException.class);
  }
}
