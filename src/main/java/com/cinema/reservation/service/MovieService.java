package com.cinema.reservation.service;

import com.cinema.reservation.endpoint.rest.dto.MovieResponse;
import com.cinema.reservation.endpoint.rest.dto.UpsertMovie;
import com.cinema.reservation.model.Movie;
import com.cinema.reservation.repository.MovieRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class MovieService {
  private final MovieRepository movieRepository;

  @Transactional
  public MovieResponse update(UpsertMovie request) {
    Movie movie = findById(request.id());
    movie.setTitle(request.title());
    movie.setGenre(request.genre());
    movie.setDescription(request.description());
    movie.setDuration(request.duration());
    return toResponse(movieRepository.save(movie));
  }

  private Movie findById(UUID id) {
    return movieRepository.findById(id).orElseThrow(MovieNotFoundException::new);
  }

  private MovieResponse toResponse(Movie movie) {
    return new MovieResponse(
        movie.getId(),
        movie.getTitle(),
        movie.getGenre(),
        movie.getDescription(),
        movie.getDuration());
  }
}
