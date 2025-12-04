package com.eventbooking.service;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Categorie;
import com.eventbooking.domain.enums.Role;
import com.eventbooking.domain.enums.StatutEvent;
import com.eventbooking.exception.BadRequestException;
import com.eventbooking.exception.BusinessException;
import com.eventbooking.exception.ForbiddenException;
import com.eventbooking.exception.ResourceNotFoundException;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 1. Create event (only ADMIN or ORGANIZER)
     */
    public Event createEvent(Event event, User user) {
        // Validate user role
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.ORGANIZER) {
            throw new ForbiddenException("Seuls les administrateurs et organisateurs peuvent créer des événements");
        }

        // Validate dates
        if (event.getDateFin().isBefore(event.getDateDebut())) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }

        event.setOrganisateur(user);
        return eventRepository.save(event);
    }

    /**
     * 2. Modify event (only creator or ADMIN)
     */
    public Event modifyEvent(Long eventId, Event updatedEvent, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        // Check ownership or admin
        if (!event.getOrganisateur().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Vous n'êtes pas autorisé à modifier cet événement");
        }

        // Check if event can be modified
        if (!event.getStatut().canModify()) {
            throw new BusinessException("Les événements terminés ne peuvent pas être modifiés");
        }

        // Validate dates
        if (updatedEvent.getDateFin().isBefore(updatedEvent.getDateDebut())) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }

        // Update fields
        event.setTitre(updatedEvent.getTitre());
        event.setDescription(updatedEvent.getDescription());
        event.setCategorie(updatedEvent.getCategorie());
        event.setDateDebut(updatedEvent.getDateDebut());
        event.setDateFin(updatedEvent.getDateFin());
        event.setLieu(updatedEvent.getLieu());
        event.setVille(updatedEvent.getVille());
        event.setCapaciteMax(updatedEvent.getCapaciteMax());
        event.setPrixUnitaire(updatedEvent.getPrixUnitaire());
        event.setImageUrl(updatedEvent.getImageUrl());

        return eventRepository.save(event);
    }

    /**
     * 3. Publish event (change from BROUILLON to PUBLIE)
     */
    public Event publishEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        // Validate all required fields
        if (event.getTitre() == null || event.getCategorie() == null ||
                event.getDateDebut() == null || event.getDateFin() == null ||
                event.getLieu() == null || event.getVille() == null ||
                event.getCapaciteMax() == null || event.getPrixUnitaire() == null) {
            throw new BadRequestException("Tous les champs obligatoires doivent être remplis pour publier l'événement");
        }

        event.setStatut(StatutEvent.PUBLIE);
        return eventRepository.save(event);
    }

    /**
     * 4. Cancel event (with existing reservation handling)
     */
    public Event cancelEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        event.setStatut(StatutEvent.ANNULE);

        // Note: In a real application, you would notify users with existing
        // reservations
        // This could be done via email or notification system

        return eventRepository.save(event);
    }

    /**
     * 5. Delete event (only if no reservations)
     */
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        // Check if event has reservations
        long reservationCount = reservationRepository.findByEvenement(event).size();
        if (reservationCount > 0) {
            throw new BusinessException("Impossible de supprimer un événement avec des réservations existantes");
        }

        eventRepository.delete(event);
    }

    /**
     * 6. Search events with multiple filters (using Streams)
     */
    public List<Event> searchEvents(Categorie categorie, String ville, LocalDateTime dateDebut,
            LocalDateTime dateFin, Double prixMin, Double prixMax) {
        List<Event> events = eventRepository.findAll();

        return events.stream()
                .filter(e -> categorie == null || e.getCategorie().equals(categorie))
                .filter(e -> ville == null || e.getVille().equalsIgnoreCase(ville))
                .filter(e -> dateDebut == null || e.getDateDebut().isAfter(dateDebut))
                .filter(e -> dateFin == null || e.getDateFin().isBefore(dateFin))
                .filter(e -> prixMin == null || e.getPrixUnitaire() >= prixMin)
                .filter(e -> prixMax == null || e.getPrixUnitaire() <= prixMax)
                .collect(Collectors.toList());
    }

    /**
     * 7. Calculate available seats for an event
     */
    public int calculateAvailableSeats(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        Integer totalReserved = reservationRepository.calculateTotalReservedPlaces(event);
        return event.getCapaciteMax() - totalReserved;
    }

    /**
     * 8. Retrieve popular events (most reserved) using Streams
     */
    public List<Event> getPopularEvents(int limit) {
        List<Event> events = eventRepository.findAll();

        return events.stream()
                .filter(e -> e.getStatut() == StatutEvent.PUBLIE)
                .sorted(Comparator.comparingInt(e -> reservationRepository.calculateTotalReservedPlaces((Event) e))
                        .reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 9. Generate statistics per organizer using Streams
     */
    public OrganizerStatistics getOrganizerStatistics(Long userId, User organizer) {
        List<Event> events = eventRepository.findByOrganisateur(organizer);

        long totalEvents = events.size();
        long publishedEvents = events.stream()
                .filter(e -> e.getStatut() == StatutEvent.PUBLIE)
                .count();

        double totalRevenue = events.stream()
                .flatMap(e -> reservationRepository.findByEvenement(e).stream())
                .mapToDouble(r -> r.getMontantTotal())
                .sum();

        double avgAttendance = events.stream()
                .mapToInt(e -> reservationRepository.calculateTotalReservedPlaces(e))
                .average()
                .orElse(0.0);

        return new OrganizerStatistics(totalEvents, publishedEvents, totalRevenue, avgAttendance);
    }

    /**
     * 10. Automatic check for finished events
     */
    public void checkAndUpdateFinishedEvents() {
        List<Event> events = eventRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        events.stream()
                .filter(e -> e.getStatut() == StatutEvent.PUBLIE)
                .filter(e -> e.getDateFin().isBefore(now))
                .forEach(e -> {
                    e.setStatut(StatutEvent.TERMINE);
                    eventRepository.save(e);
                });
    }

    /**
     * Find event by ID
     */
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));
    }

    /**
     * Get all published events
     */
    public List<Event> getPublishedEvents() {
        return eventRepository.findAvailableEvents();
    }

    /**
     * Get events by organizer
     */
    public List<Event> getEventsByOrganizer(User organizer) {
        return eventRepository.findByOrganisateur(organizer);
    }

    /**
     * Get all events
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // DTO for organizer statistics
    public record OrganizerStatistics(long totalEvents, long publishedEvents,
            double totalRevenue, double avgAttendance) {
    }
}
