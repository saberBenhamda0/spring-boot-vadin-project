package com.eventbooking.config;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.Reservation;
import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Categorie;
import com.eventbooking.domain.enums.Role;
import com.eventbooking.domain.enums.StatutEvent;
import com.eventbooking.domain.enums.StatutReservation;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.ReservationRepository;
import com.eventbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

        private final UserRepository userRepository;
        private final EventRepository eventRepository;
        private final ReservationRepository reservationRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) {
                if (userRepository.count() == 0) {
                        log.info("Loading sample data...");
                        loadData();
                        log.info("Sample data loaded successfully!");
                }
        }

        private void loadData() {
                // Create users
                User admin = createUser("Admin", "System", "admin@event.ma", "admin123", Role.ADMIN);
                User org1 = createUser("Mohammed", "Alami", "org1@event.ma", "organizer123", Role.ORGANIZER,
                                "0612345678");
                User org2 = createUser("Fatima", "Bennani", "org2@event.ma", "organizer123", Role.ORGANIZER,
                                "0623456789");
                User client1 = createUser("Youssef", "Idrissi", "client1@event.ma", "client123", Role.CLIENT,
                                "0634567890");
                User client2 = createUser("Amina", "Tazi", "client2@event.ma", "client123", Role.CLIENT, "0645678901");
                User test = createUser("test", "test", "test@gmail.com", "testtest", Role.CLIENT, "0645678901");

                // Create events
                List<Event> events = new ArrayList<>();

                // CONCERT events
                events.add(createEvent("Festival Mawazine 2024", "Le plus grand festival de musique au Maroc",
                                Categorie.CONCERT, LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(37),
                                "Scène OLM Souissi", "Rabat", 50000, 150.0, org1, StatutEvent.PUBLIE));

                events.add(createEvent("Concert Saad Lamjarred", "Concert exceptionnel de la star marocaine",
                                Categorie.CONCERT, LocalDateTime.now().plusDays(15),
                                LocalDateTime.now().plusDays(15).plusHours(3),
                                "Complexe Mohammed V", "Casablanca", 15000, 300.0, org1, StatutEvent.PUBLIE));

                events.add(createEvent("Gnaoua World Music Festival", "Festival de musique gnaoua et musiques du monde",
                                Categorie.CONCERT, LocalDateTime.now().plusDays(60), LocalDateTime.now().plusDays(63),
                                "Place Moulay Hassan", "Essaouira", 20000, 200.0, org2, StatutEvent.BROUILLON));

                // THEATRE events
                events.add(createEvent("Pièce: Le Bourgeois Gentilhomme", "Comédie-ballet de Molière",
                                Categorie.THEATRE, LocalDateTime.now().plusDays(10),
                                LocalDateTime.now().plusDays(10).plusHours(2),
                                "Théâtre Mohammed V", "Rabat", 500, 120.0, org2, StatutEvent.PUBLIE));

                events.add(createEvent("Spectacle: Nass El Ghiwane", "Hommage au groupe mythique",
                                Categorie.THEATRE, LocalDateTime.now().plusDays(20),
                                LocalDateTime.now().plusDays(20).plusHours(3),
                                "Théâtre Mohammed VI", "Casablanca", 800, 180.0, org1, StatutEvent.PUBLIE));

                events.add(createEvent("One Man Show: Gad Elmaleh", "Spectacle d'humour",
                                Categorie.THEATRE, LocalDateTime.now().plusDays(45),
                                LocalDateTime.now().plusDays(45).plusHours(2),
                                "Palais des Congrès", "Marrakech", 2000, 250.0, org2, StatutEvent.PUBLIE));

                // CONFERENCE events
                events.add(createEvent("Tech Summit Morocco 2024", "Conférence sur les nouvelles technologies",
                                Categorie.CONFERENCE, LocalDateTime.now().plusDays(25),
                                LocalDateTime.now().plusDays(27),
                                "Sofitel Jardin des Roses", "Rabat", 1000, 500.0, org1, StatutEvent.PUBLIE));

                events.add(createEvent("Forum Économique Africain", "Rencontre des leaders économiques africains",
                                Categorie.CONFERENCE, LocalDateTime.now().plusDays(40),
                                LocalDateTime.now().plusDays(42),
                                "Hyatt Regency", "Casablanca", 1500, 450.0, org2, StatutEvent.PUBLIE));

                events.add(createEvent("Conférence sur l'IA", "Intelligence Artificielle et avenir",
                                Categorie.CONFERENCE, LocalDateTime.now().plusDays(50),
                                LocalDateTime.now().plusDays(50).plusHours(6),
                                "Palais des Congrès", "Tanger", 800, 350.0, org1, StatutEvent.BROUILLON));

                // SPORT events
                events.add(createEvent("Match Raja vs Wydad", "Derby casablancais",
                                Categorie.SPORT, LocalDateTime.now().plusDays(7),
                                LocalDateTime.now().plusDays(7).plusHours(2),
                                "Stade Mohammed V", "Casablanca", 45000, 100.0, org1, StatutEvent.PUBLIE));

                events.add(createEvent("Marathon International de Marrakech", "Course de 42km à travers la ville rouge",
                                Categorie.SPORT, LocalDateTime.now().plusDays(35),
                                LocalDateTime.now().plusDays(35).plusHours(5),
                                "Place Jemaa el-Fna", "Marrakech", 5000, 50.0, org2, StatutEvent.PUBLIE));

                events.add(createEvent("Tournoi de Tennis ATP", "Tournoi international de tennis",
                                Categorie.SPORT, LocalDateTime.now().plusDays(55), LocalDateTime.now().plusDays(62),
                                "Complexe Al Amal", "Rabat", 3000, 200.0, org1, StatutEvent.PUBLIE));

                // AUTRE events
                events.add(createEvent("Salon du Livre de Casablanca", "Rencontre avec les auteurs et éditeurs",
                                Categorie.AUTRE, LocalDateTime.now().plusDays(18), LocalDateTime.now().plusDays(25),
                                "Office des Changes", "Casablanca", 10000, 30.0, org2, StatutEvent.PUBLIE));

                events.add(createEvent("Exposition d'Art Contemporain", "Œuvres d'artistes marocains et internationaux",
                                Categorie.AUTRE, LocalDateTime.now().plusDays(12), LocalDateTime.now().plusDays(40),
                                "Musée Mohammed VI", "Rabat", 2000, 80.0, org1, StatutEvent.PUBLIE));

                events.add(createEvent("Festival du Film de Marrakech", "Cinéma international",
                                Categorie.AUTRE, LocalDateTime.now().plusDays(70), LocalDateTime.now().plusDays(77),
                                "Palais des Congrès", "Marrakech", 5000, 150.0, org2, StatutEvent.BROUILLON));

                // Save events
                events.forEach(eventRepository::save);

                // Create reservations
                createReservation(client1, events.get(0), 2, StatutReservation.CONFIRMEE);
                createReservation(client2, events.get(0), 4, StatutReservation.CONFIRMEE);
                createReservation(client1, events.get(1), 1, StatutReservation.EN_ATTENTE);
                createReservation(client2, events.get(3), 3, StatutReservation.CONFIRMEE);
                createReservation(client1, events.get(4), 2, StatutReservation.CONFIRMEE);
                createReservation(client2, events.get(5), 5, StatutReservation.CONFIRMEE);
                createReservation(client1, events.get(6), 1, StatutReservation.CONFIRMEE);
                createReservation(client2, events.get(7), 2, StatutReservation.EN_ATTENTE);
                createReservation(client1, events.get(9), 3, StatutReservation.CONFIRMEE);
                createReservation(client2, events.get(10), 1, StatutReservation.CONFIRMEE);
                createReservation(client1, events.get(11), 4, StatutReservation.CONFIRMEE);
                createReservation(client2, events.get(12), 2, StatutReservation.CONFIRMEE);
                createReservation(client1, events.get(13), 1, StatutReservation.ANNULEE);
                createReservation(client2, events.get(1), 2, StatutReservation.CONFIRMEE);
                createReservation(client1, events.get(3), 3, StatutReservation.CONFIRMEE);
                createReservation(client2, events.get(4), 1, StatutReservation.EN_ATTENTE);
                createReservation(client1, events.get(6), 2, StatutReservation.CONFIRMEE);
                createReservation(client2, events.get(9), 4, StatutReservation.CONFIRMEE);
                createReservation(client1, events.get(11), 1, StatutReservation.CONFIRMEE);
                createReservation(client2, events.get(12), 3, StatutReservation.CONFIRMEE);
        }

        private User createUser(String nom, String prenom, String email, String password, Role role) {
                return createUser(nom, prenom, email, password, role, null);
        }

        private User createUser(String nom, String prenom, String email, String password, Role role, String telephone) {
                User user = User.builder()
                                .nom(nom)
                                .prenom(prenom)
                                .email(email)
                                .password(passwordEncoder.encode(password))
                                .role(role)
                                .telephone(telephone)
                                .actif(true)
                                .dateInscription(LocalDateTime.now())
                                .build();
                return userRepository.save(user);
        }

        private Event createEvent(String titre, String description, Categorie categorie,
                        LocalDateTime dateDebut, LocalDateTime dateFin, String lieu, String ville,
                        Integer capaciteMax, Double prixUnitaire, User organisateur, StatutEvent statut) {
                Event event = Event.builder()
                                .titre(titre)
                                .description(description)
                                .categorie(categorie)
                                .dateDebut(dateDebut)
                                .dateFin(dateFin)
                                .lieu(lieu)
                                .ville(ville)
                                .capaciteMax(capaciteMax)
                                .prixUnitaire(prixUnitaire)
                                .organisateur(organisateur)
                                .statut(statut)
                                .dateCreation(LocalDateTime.now())
                                .build();
                return event;
        }

        private void createReservation(User utilisateur, Event evenement, Integer nombrePlaces,
                        StatutReservation statut) {
                Reservation reservation = Reservation.builder()
                                .utilisateur(utilisateur)
                                .evenement(evenement)
                                .nombrePlaces(nombrePlaces)
                                .statut(statut)
                                .dateReservation(LocalDateTime.now())
                                .build();
                reservationRepository.save(reservation);
        }
}
