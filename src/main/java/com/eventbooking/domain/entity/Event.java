package com.eventbooking.domain.entity;

import com.eventbooking.domain.enums.Categorie;
import com.eventbooking.domain.enums.StatutEvent;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "organisateur", "reservations" })
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caractères")
    @Column(nullable = false)
    private String titre;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categorie categorie;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateFin;

    @NotBlank(message = "Le lieu est obligatoire")
    @Column(nullable = false)
    private String lieu;

    @NotBlank(message = "La ville est obligatoire")
    @Column(nullable = false)
    private String ville;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Min(value = 1, message = "La capacité doit être au moins 1")
    @Column(nullable = false)
    private Integer capaciteMax;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Min(value = 0, message = "Le prix doit être positif ou nul")
    @Column(nullable = false)
    private Double prixUnitaire;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organisateur_id", nullable = false)
    @ToString.Exclude
    private User organisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEvent statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        if (statut == null) {
            statut = StatutEvent.BROUILLON;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}
