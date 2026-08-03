package com.cinema.reservation.repository;

import com.cinema.reservation.model.Room;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, UUID> {}
