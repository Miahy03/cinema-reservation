package com.cinema.reservation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
  @Id @UuidGenerator private UUID id;
  private String number;
  private int capacity;

  @OneToMany(mappedBy = "room")
  private List<Seat> seats = new ArrayList<>();

  @OneToMany(mappedBy = "room")
  private List<Projection> projections = new ArrayList<>();
}
