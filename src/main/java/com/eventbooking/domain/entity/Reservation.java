package com.eventbooking.domain.entity;

import com.eventbooking.domain.enums.StatutReservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Random;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @ToString.Exclude
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    @ToString.Exclude
    private Event evenement;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Le nombre de places doit être au moins 1")
    @Max(value = 10, message = "Le nombre de places ne peut pas dépasser 10")
    @Column(nullable = false)
    private Integer nombrePlaces;

    @Column(nullable = false)
    private Double montantTotal;

    @Column(nullable = false)
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation statut;

    @Column(nullable = false, unique = true)
    private String codeReservation;

    @Column(length = 500)
    private String commentaire;

    @PrePersist
    protected void onCreate() {
        dateReservation = LocalDateTime.now();

        if (statut == null) {
            statut = StatutReservation.EN_ATTENTE;
        }

        // Generate unique reservation code
        if (codeReservation == null) {
            codeReservation = generateReservationCode();
        }

        // Calculate total amount
        if (evenement != null && nombrePlaces != null) {
            montantTotal = evenement.getPrixUnitaire() * nombrePlaces;
        }
    }

    private String generateReservationCode() {
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000); // 5-digit number
        return "EVT-" + randomNumber;
    }
}
