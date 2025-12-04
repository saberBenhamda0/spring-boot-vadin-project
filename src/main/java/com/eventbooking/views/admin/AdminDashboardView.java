package com.eventbooking.views.admin;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.Reservation;
import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Role;
import com.eventbooking.domain.enums.StatutEvent;
import com.eventbooking.service.EventService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.eventbooking.views.MainLayout;
import com.eventbooking.views.components.StatCard;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * Admin dashboard view
 */
@Route(value = "admin/dashboard", layout = MainLayout.class)
@PageTitle("Tableau de Bord Admin | Event Booking")
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {

    private final UserService userService;
    private final EventService eventService;
    private final ReservationService reservationService;

    public AdminDashboardView(UserService userService, EventService eventService,
            ReservationService reservationService) {
        this.userService = userService;
        this.eventService = eventService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createUserStatistics();
        createEventStatistics();
        createReservationStatistics();
        createQuickActions();
    }

    private void createHeader() {
        H2 title = new H2("Tableau de Bord Administrateur");
        title.getStyle().set("color", "#667eea");
        add(title);
    }

    private void createUserStatistics() {
        H3 sectionTitle = new H3("Statistiques Utilisateurs");
        sectionTitle.getStyle().set("margin-top", "20px");

        List<User> allUsers = userService.getAllUsers();

        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::getActif).count();
        long clients = allUsers.stream().filter(u -> u.getRole() == Role.CLIENT).count();
        long organizers = allUsers.stream().filter(u -> u.getRole() == Role.ORGANIZER).count();
        long admins = allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count();

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        StatCard totalUsersCard = new StatCard(
                "Utilisateurs Totaux",
                String.valueOf(totalUsers),
                VaadinIcon.USERS,
                "#2196F3");

        StatCard activeUsersCard = new StatCard(
                "Utilisateurs Actifs",
                String.valueOf(activeUsers),
                VaadinIcon.CHECK_CIRCLE,
                "#4CAF50");

        StatCard clientsCard = new StatCard(
                "Clients",
                String.valueOf(clients),
                VaadinIcon.USER,
                "#9C27B0");

        StatCard organizersCard = new StatCard(
                "Organisateurs",
                String.valueOf(organizers),
                VaadinIcon.BRIEFCASE,
                "#FF9800");

        StatCard adminsCard = new StatCard(
                "Administrateurs",
                String.valueOf(admins),
                VaadinIcon.SHIELD,
                "#F44336");

        statsLayout.add(totalUsersCard, activeUsersCard, clientsCard, organizersCard, adminsCard);

        add(sectionTitle, statsLayout);
    }

    private void createEventStatistics() {
        H3 sectionTitle = new H3("Statistiques Événements");
        sectionTitle.getStyle().set("margin-top", "30px");

        List<Event> allEvents = eventService.getAllEvents();

        long totalEvents = allEvents.size();
        long publishedEvents = allEvents.stream().filter(e -> e.getStatut() == StatutEvent.PUBLIE).count();
        long draftEvents = allEvents.stream().filter(e -> e.getStatut() == StatutEvent.BROUILLON).count();
        long cancelledEvents = allEvents.stream().filter(e -> e.getStatut() == StatutEvent.ANNULE).count();
        long finishedEvents = allEvents.stream().filter(e -> e.getStatut() == StatutEvent.TERMINE).count();

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        StatCard totalEventsCard = new StatCard(
                "Événements Totaux",
                String.valueOf(totalEvents),
                VaadinIcon.CALENDAR,
                "#2196F3");

        StatCard publishedCard = new StatCard(
                "Publiés",
                String.valueOf(publishedEvents),
                VaadinIcon.CHECK_CIRCLE,
                "#4CAF50");

        StatCard draftCard = new StatCard(
                "Brouillons",
                String.valueOf(draftEvents),
                VaadinIcon.EDIT,
                "#FF9800");

        StatCard cancelledCard = new StatCard(
                "Annulés",
                String.valueOf(cancelledEvents),
                VaadinIcon.BAN,
                "#F44336");

        StatCard finishedCard = new StatCard(
                "Terminés",
                String.valueOf(finishedEvents),
                VaadinIcon.CHECK,
                "#9E9E9E");

        statsLayout.add(totalEventsCard, publishedCard, draftCard, cancelledCard, finishedCard);

        add(sectionTitle, statsLayout);
    }

    private void createReservationStatistics() {
        H3 sectionTitle = new H3("Statistiques Réservations");
        sectionTitle.getStyle().set("margin-top", "30px");

        List<Reservation> allReservations = reservationService.getAllReservations();

        long totalReservations = allReservations.size();
        double totalRevenue = allReservations.stream()
                .filter(r -> r.getStatut() != com.eventbooking.domain.enums.StatutReservation.ANNULEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();

        int totalPlaces = allReservations.stream()
                .filter(r -> r.getStatut() != com.eventbooking.domain.enums.StatutReservation.ANNULEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        StatCard totalReservationsCard = new StatCard(
                "Réservations Totales",
                String.valueOf(totalReservations),
                VaadinIcon.TICKET,
                "#2196F3");

        StatCard totalPlacesCard = new StatCard(
                "Places Réservées",
                String.valueOf(totalPlaces),
                VaadinIcon.GROUP,
                "#4CAF50");

        StatCard totalRevenueCard = new StatCard(
                "Revenu Total Plateforme",
                String.format("%.2f DH", totalRevenue),
                VaadinIcon.MONEY,
                "#00BCD4");

        statsLayout.add(totalReservationsCard, totalPlacesCard, totalRevenueCard);

        add(sectionTitle, statsLayout);
    }

    private void createQuickActions() {
        H3 actionsTitle = new H3("Actions Rapides");
        actionsTitle.getStyle().set("margin-top", "30px");

        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);

        com.vaadin.flow.component.button.Button manageUsers = new com.vaadin.flow.component.button.Button(
                "Gérer les Utilisateurs", VaadinIcon.USERS.create());
        manageUsers.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        manageUsers.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(UserManagementView.class)));

        com.vaadin.flow.component.button.Button manageEvents = new com.vaadin.flow.component.button.Button(
                "Gérer les Événements", VaadinIcon.CALENDAR.create());
        manageEvents.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SUCCESS);
        manageEvents.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(AllEventsManagementView.class)));

        com.vaadin.flow.component.button.Button viewReservations = new com.vaadin.flow.component.button.Button(
                "Voir les Réservations", VaadinIcon.TICKET.create());
        viewReservations.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(AllReservationsView.class)));

        actionsLayout.add(manageUsers, manageEvents, viewReservations);

        add(actionsTitle, actionsLayout);
    }
}
