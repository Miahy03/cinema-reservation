package com.cinema.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.cinema.reservation.endpoint.rest.dto.UpsertMovie;
import com.cinema.reservation.model.Genre;
import com.cinema.reservation.model.Movie;
import com.cinema.reservation.repository.MovieRepository;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

  @Mock MovieRepository movieRepository;

  @InjectMocks MovieService service;

  @Test
  void update_updates_movie() {
    UUID id = UUID.randomUUID();

    Movie existing =
        Movie.builder()
            .id(id)
            .title("Dune")
            .genre(Genre.SCI_FI)
            .description("Old description")
            .duration(Duration.ofMinutes(155))
            .build();

    when(movieRepository.findById(id)).thenReturn(Optional.of(existing));
    when(movieRepository.save(existing)).thenReturn(existing);

    var result =
        service.update(
            new UpsertMovie(id, "Dune Part Two", Genre.SCI_FI, "Updated description", "PT2H46M"));

    assertThat(result.id()).isEqualTo(id);
    assertThat(result.title()).isEqualTo("Dune Part Two");
    assertThat(result.genre()).isEqualTo(Genre.SCI_FI);
    assertThat(result.description()).isEqualTo("Updated description");
    assertThat(result.duration()).isEqualTo("PT2H46M");
  }

  @Test
  void update_movie_not_found_throws() {
    UUID id = UUID.randomUUID();

    when(movieRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.update(
                    new UpsertMovie(id, "Unknown movie", Genre.SCI_FI, "Description", "PT2H")))
        .isInstanceOf(MovieNotFoundException.class);
  }
}
