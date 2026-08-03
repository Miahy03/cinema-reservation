package com.cinema.reservation.repository;

import com.cinema.reservation.model.Movie;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, UUID> {}
