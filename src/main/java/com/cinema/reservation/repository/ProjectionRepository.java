package com.cinema.reservation.repository;

import com.cinema.reservation.model.Projection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectionRepository extends JpaRepository<Projection, UUID> {}
