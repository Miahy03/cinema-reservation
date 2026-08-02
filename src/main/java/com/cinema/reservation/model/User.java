package com.cinema.reservation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "app_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @Id @UuidGenerator private UUID id;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;

  @Column(unique = true)
  private String email;

  private String password;
  private String phone;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  @OneToMany(mappedBy = "user")
  private List<Reservation> reservations = new ArrayList<>();
}
