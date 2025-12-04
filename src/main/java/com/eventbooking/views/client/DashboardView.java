package com.eventbooking.views.client;

import com.eventbooking.domain.entity.User;
import com.eventbooking.security.SecurityService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.ReservationService.ReservationStatistics;
import com.eventbooking.views.MainLayout;
import com.eventbooking.views.components.StatCard;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * Dashboard view for CLIENT users
 */
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Tableau de Bord | Event Booking")
@RolesAllowed("CLIENT")
public class DashboardView extends VerticalLayout {

    private final SecurityService securityService;
    private final ReservationService reservationService;

    public DashboardView(SecurityService securityService, ReservationService reservationService) {
        this.securityService = securityService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        User currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        createWelcomeSection(currentUser);
        createStatisticsSection(currentUser);
        createQuickActions();
    }

    private void createWelcomeSection(User user) {
        H2 welcome = new H2("Bienvenue, " + user.getPrenom() + " !");
        welcome.getStyle().set("color", "#667eea");

        Paragraph subtitle = new Paragraph("Voici un aperçu de vos activités");
        subtitle.getStyle().set("color", "#666");

        add(welcome, subtitle);
    }

    private void createStatisticsSection(User user) {
        H2 statsTitle = new H2("Vos Statistiques");
        statsTitle.getStyle().set("margin-top", "20px");

        ReservationStatistics stats = reservationService.getReservationStatistics(user.getId());

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        StatCard totalReservations = new StatCard(
                "Réservations Totales",
                String.valueOf(stats.totalReservations()),
                VaadinIcon.TICKET,
                "#2196F3");

        StatCard upcomingEvents = new StatCard(
                "Événements à Venir",
                String.valueOf(stats.upcomingEvents()),
                VaadinIcon.CALENDAR,
                "#4CAF50");

        StatCard totalSpent = new StatCard(
                "Montant Total Dépensé",
                String.format("%.2f DH", stats.totalSpent()),
                VaadinIcon.MONEY,
                "#FF9800");

        statsLayout.add(totalReservations, upcomingEvents, totalSpent);

        add(statsTitle, statsLayout);
    }

    private void createQuickActions() {
        H2 actionsTitle = new H2("Accès Rapide");
        actionsTitle.getStyle().set("margin-top", "30px");

        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);

        com.vaadin.flow.component.button.Button viewReservations = new com.vaadin.flow.component.button.Button(
                "Mes Réservations", VaadinIcon.TICKET.create());
        viewReservations.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        viewReservations.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(MyReservationsView.class)));

        com.vaadin.flow.component.button.Button browseEvents = new com.vaadin.flow.component.button.Button(
                "Parcourir les Événements", VaadinIcon.CALENDAR.create());
        browseEvents.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SUCCESS);
        browseEvents.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("events")));

        com.vaadin.flow.component.button.Button viewProfile = new com.vaadin.flow.component.button.Button("Mon Profil",
                VaadinIcon.USER.create());
        viewProfile.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ProfileView.class)));

        actionsLayout.add(viewReservations, browseEvents, viewProfile);

        add(actionsTitle, actionsLayout);
    }
}
