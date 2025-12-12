package com.eventbooking.service;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.Reservation;
import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.StatutEvent;
import com.eventbooking.domain.enums.StatutReservation;
import com.eventbooking.exception.BadRequestException;
import com.eventbooking.exception.BusinessException;
import com.eventbooking.exception.ResourceNotFoundException;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    /**
     * 1. Create reservation with checks
     */
    public Reservation createReservation(Reservation reservation) {
        Event event = reservation.getEvenement();

        // Validate event is PUBLIE and not TERMINE
        if (event.getStatut() != StatutEvent.PUBLIE) {
            throw new BusinessException("Seuls les événements publiés peuvent être réservés");
        }

        if (event.getStatut() == StatutEvent.TERMINE) {
            throw new BusinessException("Impossible de réserver un événement terminé");
        }

        // Validate nombrePlaces <= 10
        if (reservation.getNombrePlaces() > 10) {
            throw new BusinessException("Le nombre maximum de places par réservation est de 10");
        }

        // Check available seats
        Integer totalReserved = reservationRepository.calculateTotalReservedPlaces(event);
        int availableSeats = event.getCapaciteMax() - totalReserved;

        if (reservation.getNombrePlaces() > availableSeats) {
            throw new BusinessException("Nombre de places insuffisant. Places disponibles: " + availableSeats);
        }

        // Generate unique code with retry logic
        String code;
        int maxRetries = 10;
        int retries = 0;
        do {
            code = generateUniqueCode();
            retries++;
            if (retries > maxRetries) {
                throw new BusinessException("Impossible de générer un code de réservation unique");
            }
        } while (reservationRepository.findByCodeReservation(code).isPresent());

        reservation.setCodeReservation(code);

        // Calculate montantTotal (will also be done in @PrePersist, but explicit here)
        reservation.setMontantTotal(event.getPrixUnitaire() * reservation.getNombrePlaces());

        return reservationRepository.save(reservation);
    }

    /**
     * 2. Confirm reservation
     */
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        reservation.setStatut(StatutReservation.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    /**
     * 3. Cancel reservation (validate 48h before event)
     */
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        // Check if reservation can be cancelled
        if (!reservation.getStatut().canCancel()) {
            throw new BusinessException("Cette réservation ne peut pas être annulée");
        }

        // Check 48h policy using Lambda for date calculations
        LocalDateTime eventStart = reservation.getEvenement().getDateDebut();
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilEvent = Duration.between(now, eventStart).toHours();

        if (hoursUntilEvent < 48) {
            throw new BusinessException("Les réservations ne peuvent être annulées que 48h avant l'événement");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        return reservationRepository.save(reservation);
    }

    /**
     * 4. Retrieve user reservations with filtering options
     */
    public List<Reservation> getUserReservations(Long userId, StatutReservation statut) {
        User user = User.builder().id(userId).build();

        if (statut != null) {
            return reservationRepository.findByUtilisateurAndStatutWithEvenement(user, statut);
        }
        return reservationRepository.findByUtilisateurWithEvenement(user);
    }

    /**
     * 5. Verify reservation by code
     */
    public Optional<Reservation> verifyReservationByCode(String code) {
        return reservationRepository.findByCodeReservation(code);
    }

    /**
     * 6. Generate reservation summary
     */
    public ReservationSummary generateReservationSummary(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        return new ReservationSummary(
                reservation.getCodeReservation(),
                reservation.getEvenement().getTitre(),
                reservation.getEvenement().getDateDebut(),
                reservation.getEvenement().getLieu(),
                reservation.getNombrePlaces(),
                reservation.getMontantTotal(),
                reservation.getStatut().getLabel());
    }

    /**
     * 7. Get reservation statistics using Streams
     */
    public ReservationStatistics getReservationStatistics(Long userId) {
        User user = User.builder().id(userId).build();
        List<Reservation> reservations = reservationRepository.findByUtilisateur(user);

        long totalReservations = reservations.size();

        Double totalSpent = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();

        long upcomingEvents = reservations.stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .filter(r -> r.getEvenement().getDateDebut().isAfter(LocalDateTime.now()))
                .count();

        return new ReservationStatistics(totalReservations, totalSpent, upcomingEvents);
    }

    /**
     * 8. Get event reservations (for organizers/admin)
     */
    public List<Reservation> getEventReservations(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        return reservationRepository.findByEvenementWithUtilisateur(event);
    }

    /**
     * 9. Calculate total reserved places for an event
     */
    public Integer calculateTotalReservedPlaces(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        return reservationRepository.calculateTotalReservedPlaces(event);
    }

    /**
     * Find reservation by ID
     */
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));
    }

    /**
     * Get all reservations
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllWithUtilisateurAndEvenement();
    }

    /**
     * Generate unique reservation code (EVT-XXXXX format)
     */
    private String generateUniqueCode() {
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000); // 5-digit number
        return "EVT-" + randomNumber;
    }

    // DTOs
    public record ReservationSummary(String code, String eventTitle, LocalDateTime eventDate,
            String eventLocation, Integer places, Double totalAmount,
            String status) {
    }

    public record ReservationStatistics(long totalReservations, Double totalSpent,
            long upcomingEvents) {
    }
}
