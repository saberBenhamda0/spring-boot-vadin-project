package com.eventbooking.repository;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Categorie;
import com.eventbooking.domain.enums.StatutEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Find events by categorie
     */
    List<Event> findByCategorie(Categorie categorie);

    /**
     * Find published events between two dates
     */
    List<Event> findByStatutAndDateDebutBetween(StatutEvent statut, LocalDateTime start, LocalDateTime end);

    /**
     * Find events by organizer with given statut
     */
    List<Event> findByOrganisateurAndStatut(User organizer, StatutEvent statut);

    /**
     * Find available events (published and not terminated)
     */
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND e.statut != 'TERMINE'")
    List<Event> findAvailableEvents();

    /**
     * Count events by categorie
     */
    long countByCategorie(Categorie categorie);

    /**
     * Find events by lieu or ville
     */
    List<Event> findByLieuContainingIgnoreCaseOrVilleContainingIgnoreCase(String lieu, String ville);

    /**
     * Search events by title (containing keyword)
     */
    List<Event> findByTitreContainingIgnoreCase(String keyword);

    /**
     * Find events by price range
     */
    List<Event> findByPrixUnitaireBetween(Double min, Double max);

    /**
     * Find all events by organizer
     */
    List<Event> findByOrganisateur(User organizer);
}
