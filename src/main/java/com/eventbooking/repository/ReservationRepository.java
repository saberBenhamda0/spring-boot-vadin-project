package com.eventbooking.repository;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.Reservation;
import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find reservations by utilisateur
     */
    List<Reservation> findByUtilisateur(User user);

    /**
     * Find reservations for an event with given statut
     */
    List<Reservation> findByEvenementAndStatut(Event event, StatutReservation statut);

    /**
     * Calculate total reserved places for an event
     */
    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r WHERE r.evenement = :event AND r.statut != 'ANNULEE'")
    Integer calculateTotalReservedPlaces(@Param("event") Event event);

    /**
     * Find reservation by code
     */
    Optional<Reservation> findByCodeReservation(String code);

    /**
     * Find reservations between two dates
     */
    List<Reservation> findByDateReservationBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find confirmed reservations for a user
     */
    List<Reservation> findByUtilisateurAndStatut(User user, StatutReservation statut);

    /**
     * Calculate total reservation amount per user
     */
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0.0) FROM Reservation r WHERE r.utilisateur = :user AND r.statut = 'CONFIRMEE'")
    Double calculateTotalAmountByUser(@Param("user") User user);

    /**
     * Find all reservations by event
     */
    List<Reservation> findByEvenement(Event event);
}
