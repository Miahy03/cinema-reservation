package com.cinema.reservation.endpoint.rest.controller;

import com.cinema.reservation.endpoint.rest.dto.ProjectionResponse;
import com.cinema.reservation.endpoint.rest.dto.UpsertProjection;
import com.cinema.reservation.service.ProjectionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ProjectionController {
    private final ProjectionService projectionService;

    @GetMapping("/projections")
    public List<ProjectionResponse> getAll() {
        return projectionService.listAll();
    }

    @PutMapping("/projections")
    public ProjectionResponse update(@Valid @RequestBody UpsertProjection request) {
        return projectionService.update(request);
    }
}