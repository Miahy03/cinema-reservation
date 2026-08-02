package com.cinema.reservation.repository;

import com.cinema.reservation.model.Seat;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, UUID> {}
