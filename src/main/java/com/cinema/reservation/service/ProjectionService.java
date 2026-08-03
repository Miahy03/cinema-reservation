package com.cinema.reservation.service;

import com.cinema.reservation.endpoint.rest.dto.ProjectionResponse;
import com.cinema.reservation.endpoint.rest.dto.UpsertProjection;
import com.cinema.reservation.model.Movie;
import com.cinema.reservation.model.Projection;
import com.cinema.reservation.model.Room;
import com.cinema.reservation.repository.MovieRepository;
import com.cinema.reservation.repository.ProjectionRepository;
import com.cinema.reservation.repository.RoomRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProjectionService {

    private final ProjectionRepository projectionRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<ProjectionResponse> listAll() {
        return projectionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProjectionResponse update(UpsertProjection request) {
        Projection projection = findById(request.id());
        Movie movie =
                movieRepository
                        .findById(request.movieId())
                        .orElseThrow(MovieNotFoundException::new);
        Room room =
                roomRepository
                        .findById(request.roomId())
                        .orElseThrow(RoomNotFoundException::new);
        projection.setDatetime(request.datetime());
        projection.setSeatPrice(request.seatPrice());
        projection.setMovie(movie);
        projection.setRoom(room);
        return toResponse(projectionRepository.save(projection));
    }

    private Projection findById(UUID id) {
        return projectionRepository.findById(id).orElseThrow(ProjectionNotFoundException::new);
    }

    private ProjectionResponse toResponse(Projection projection) {
        return new ProjectionResponse(
                projection.getId(),
                projection.getDatetime(),
                projection.getSeatPrice(),
                toMovie(projection.getMovie()),
                toRoom(projection.getRoom()));
    }

    private ProjectionResponse.MovieInfo toMovie(Movie movie) {
        return new ProjectionResponse.MovieInfo(
                movie.getId(),
                movie.getTitle(),
                movie.getGenre(),
                movie.getDescription(),
                movie.getDuration());
    }

    private ProjectionResponse.RoomInfo toRoom(Room room) {
        return new ProjectionResponse.RoomInfo(
                room.getId(),
                room.getNumber(),
                room.getCapacity());
    }
}