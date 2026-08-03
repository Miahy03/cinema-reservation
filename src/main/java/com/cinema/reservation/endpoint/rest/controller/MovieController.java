package com.cinema.reservation.endpoint.rest.controller;

import com.cinema.reservation.endpoint.rest.dto.UpsertMovie;
import com.cinema.reservation.endpoint.rest.dto.MovieResponse;
import com.cinema.reservation.service.MovieService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @PutMapping("/movies")
    public MovieResponse update(@Valid @RequestBody UpsertMovie request) {
        return movieService.update(request);
    }
}
