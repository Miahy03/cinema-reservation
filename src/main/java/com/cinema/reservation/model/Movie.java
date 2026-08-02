package com.cinema.reservation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.Duration;
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
public class Movie {
  @Id @UuidGenerator private UUID id;
  private String title;

  @Enumerated(EnumType.STRING)
  private Genre genre;

  private String description;
  private Duration duration;

  @OneToMany(mappedBy = "movie")
  private List<Projection> projections = new ArrayList<>();
}
