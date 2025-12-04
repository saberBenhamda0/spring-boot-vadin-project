package com.eventbooking.views.organizer;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.StatutEvent;
import com.eventbooking.security.SecurityService;
import com.eventbooking.service.EventService;
import com.eventbooking.service.EventService.OrganizerStatistics;
import com.eventbooking.service.ReservationService;
import com.eventbooking.views.MainLayout;
import com.eventbooking.views.components.EventCard;
import com.eventbooking.views.components.StatCard;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * Dashboard view for ORGANIZER users
 */
@Route(value = "organizer/dashboard", layout = MainLayout.class)
@PageTitle("Tableau de Bord Organisateur | Event Booking")
@RolesAllowed({ "ORGANIZER", "ADMIN" })
public class OrganizerDashboardView extends VerticalLayout {

    private final SecurityService securityService;
    private final EventService eventService;
    private final ReservationService reservationService;

    public OrganizerDashboardView(SecurityService securityService, EventService eventService,
            ReservationService reservationService) {
        this.securityService = securityService;
        this.eventService = eventService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        User currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        createWelcomeSection(currentUser);
        createStatisticsSection(currentUser);
        createRecentEventsSection(currentUser);
        createQuickActions();
    }

    private void createWelcomeSection(User user) {
        H2 welcome = new H2("Tableau de Bord Organisateur");
        welcome.getStyle().set("color", "#667eea");

        Paragraph subtitle = new Paragraph("Bienvenue, " + user.getPrenom() + " " + user.getNom());
        subtitle.getStyle().set("color", "#666");

        add(welcome, subtitle);
    }

    private void createStatisticsSection(User user) {
        H2 statsTitle = new H2("Vos Statistiques");
        statsTitle.getStyle().set("margin-top", "20px");

        OrganizerStatistics stats = eventService.getOrganizerStatistics(user.getId(), user);
        List<Event> events = eventService.getEventsByOrganizer(user);

        long draftEvents = events.stream().filter(e -> e.getStatut() == StatutEvent.BROUILLON).count();
        long publishedEvents = events.stream().filter(e -> e.getStatut() == StatutEvent.PUBLIE).count();
        long cancelledEvents = events.stream().filter(e -> e.getStatut() == StatutEvent.ANNULE).count();

        int totalReservations = events.stream()
                .mapToInt(e -> reservationService.getEventReservations(e.getId()).size())
                .sum();

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        StatCard totalEventsCard = new StatCard(
                "Événements Totaux",
                String.valueOf(stats.totalEvents()),
                VaadinIcon.CALENDAR,
                "#2196F3");

        StatCard publishedCard = new StatCard(
                "Événements Publiés",
                String.valueOf(publishedEvents),
                VaadinIcon.CHECK_CIRCLE,
                "#4CAF50");

        StatCard draftCard = new StatCard(
                "Brouillons",
                String.valueOf(draftEvents),
                VaadinIcon.EDIT,
                "#FF9800");

        StatCard reservationsCard = new StatCard(
                "Réservations Totales",
                String.valueOf(totalReservations),
                VaadinIcon.TICKET,
                "#9C27B0");

        StatCard revenueCard = new StatCard(
                "Revenu Total",
                String.format("%.2f DH", stats.totalRevenue()),
                VaadinIcon.MONEY,
                "#00BCD4");

        statsLayout.add(totalEventsCard, publishedCard, draftCard, reservationsCard, revenueCard);

        add(statsTitle, statsLayout);
    }

    private void createRecentEventsSection(User user) {
        H3 recentTitle = new H3("Événements Récents");
        recentTitle.getStyle().set("margin-top", "30px");

        List<Event> events = eventService.getEventsByOrganizer(user);

        HorizontalLayout eventsLayout = new HorizontalLayout();
        eventsLayout.setWidthFull();
        eventsLayout.setSpacing(true);
        eventsLayout.getStyle().set("flex-wrap", "wrap");

        events.stream()
                .limit(6)
                .forEach(event -> eventsLayout.add(new EventCard(event)));

        if (events.isEmpty()) {
            Paragraph noEvents = new Paragraph("Vous n'avez pas encore créé d'événements.");
            noEvents.getStyle().set("color", "#666");
            add(recentTitle, noEvents);
        } else {
            add(recentTitle, eventsLayout);
        }
    }

    private void createQuickActions() {
        H3 actionsTitle = new H3("Actions Rapides");
        actionsTitle.getStyle().set("margin-top", "30px");

        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);

        com.vaadin.flow.component.button.Button createEvent = new com.vaadin.flow.component.button.Button(
                "Créer un Événement", VaadinIcon.PLUS.create());
        createEvent.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        createEvent.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("organizer/event/new")));

        com.vaadin.flow.component.button.Button viewEvents = new com.vaadin.flow.component.button.Button(
                "Mes Événements", VaadinIcon.CALENDAR.create());
        viewEvents.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SUCCESS);
        viewEvents.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(MyEventsView.class)));

        actionsLayout.add(createEvent, viewEvents);

        add(actionsTitle, actionsLayout);
    }
}
