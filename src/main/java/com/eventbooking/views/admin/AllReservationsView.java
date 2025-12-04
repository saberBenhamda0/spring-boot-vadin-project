package com.eventbooking.views.admin;

import com.eventbooking.domain.entity.Reservation;
import com.eventbooking.domain.enums.StatutReservation;
import com.eventbooking.service.ReservationService;
import com.eventbooking.views.MainLayout;
import com.eventbooking.views.components.StatCard;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View for managing all reservations (admin)
 */
@Route(value = "admin/reservations", layout = MainLayout.class)
@PageTitle("Toutes les Réservations | Event Booking")
@RolesAllowed("ADMIN")
public class AllReservationsView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReservationService reservationService;
    private final Grid<Reservation> grid = new Grid<>(Reservation.class, false);

    private ComboBox<StatutReservation> statusFilter;
    private TextField searchField;

    public AllReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createStatistics();
        createFilters();
        configureGrid();
        loadReservations();
    }

    private void createHeader() {
        H2 title = new H2("Toutes les Réservations");
        title.getStyle().set("color", "#667eea");
        add(title);
    }

    private void createStatistics() {
        H3 statsTitle = new H3("Statistiques Globales");

        List<Reservation> allReservations = reservationService.getAllReservations();

        long totalReservations = allReservations.size();
        long confirmedReservations = allReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .count();
        long pendingReservations = allReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                .count();
        long cancelledReservations = allReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.ANNULEE)
                .count();

        int totalPlaces = allReservations.stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();

        double totalRevenue = allReservations.stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        StatCard totalCard = new StatCard(
                "Réservations Totales",
                String.valueOf(totalReservations),
                VaadinIcon.TICKET,
                "#2196F3");

        StatCard confirmedCard = new StatCard(
                "Confirmées",
                String.valueOf(confirmedReservations),
                VaadinIcon.CHECK_CIRCLE,
                "#4CAF50");

        StatCard pendingCard = new StatCard(
                "En Attente",
                String.valueOf(pendingReservations),
                VaadinIcon.CLOCK,
                "#FF9800");

        StatCard cancelledCard = new StatCard(
                "Annulées",
                String.valueOf(cancelledReservations),
                VaadinIcon.CLOSE_CIRCLE,
                "#F44336");

        StatCard placesCard = new StatCard(
                "Places Totales",
                String.valueOf(totalPlaces),
                VaadinIcon.GROUP,
                "#9C27B0");

        StatCard revenueCard = new StatCard(
                "Revenu Total",
                String.format("%.2f DH", totalRevenue),
                VaadinIcon.MONEY,
                "#00BCD4");

        statsLayout.add(totalCard, confirmedCard, pendingCard, cancelledCard, placesCard, revenueCard);

        VerticalLayout statsContainer = new VerticalLayout(statsTitle, statsLayout);
        statsContainer.setPadding(true);
        statsContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin-bottom", "20px");

        add(statsContainer);
    }

    private void createFilters() {
        statusFilter = new ComboBox<>("Filtrer par statut");
        statusFilter.setItems(StatutReservation.values());
        statusFilter.setItemLabelGenerator(StatutReservation::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> loadReservations());

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Code, utilisateur ou événement");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterReservations(e.getValue()));

        HorizontalLayout filters = new HorizontalLayout(statusFilter, searchField);
        filters.setAlignItems(Alignment.END);
        add(filters);
    }

    private void configureGrid() {
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(reservation -> reservation.getUtilisateur().getPrenom() + " " +
                reservation.getUtilisateur().getNom())
                .setHeader("Client")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(reservation -> reservation.getUtilisateur().getEmail())
                .setHeader("Email")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(reservation -> reservation.getEvenement().getTitre())
                .setHeader("Événement")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(reservation -> reservation.getEvenement().getOrganisateur().getPrenom() + " " +
                reservation.getEvenement().getOrganisateur().getNom())
                .setHeader("Organisateur")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(reservation -> reservation.getDateReservation().format(DATE_FORMATTER))
                .setHeader("Date Réservation")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(reservation -> String.format("%.2f DH", reservation.getMontantTotal()))
                .setHeader("Montant")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(reservation -> {
            Span badge = new Span(reservation.getStatut().getLabel());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", getStatusColor(reservation.getStatut()))
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85em");
            return badge;
        })).setHeader("Statut").setAutoWidth(true);

        add(grid);
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();

        if (statusFilter.getValue() != null) {
            reservations = reservations.stream()
                    .filter(r -> r.getStatut() == statusFilter.getValue())
                    .toList();
        }

        grid.setItems(reservations);
    }

    private void filterReservations(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            loadReservations();
            return;
        }

        List<Reservation> reservations = reservationService.getAllReservations();
        String lowerSearch = searchTerm.toLowerCase();

        reservations = reservations.stream()
                .filter(r -> r.getCodeReservation().toLowerCase().contains(lowerSearch) ||
                        r.getUtilisateur().getNom().toLowerCase().contains(lowerSearch) ||
                        r.getUtilisateur().getPrenom().toLowerCase().contains(lowerSearch) ||
                        r.getUtilisateur().getEmail().toLowerCase().contains(lowerSearch) ||
                        r.getEvenement().getTitre().toLowerCase().contains(lowerSearch))
                .toList();

        grid.setItems(reservations);
    }

    private String getStatusColor(StatutReservation status) {
        return switch (status) {
            case CONFIRMEE -> "#4CAF50";
            case EN_ATTENTE -> "#FF9800";
            case ANNULEE -> "#F44336";
        };
    }
}
