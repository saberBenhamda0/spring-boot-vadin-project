package com.eventbooking.views.organizer;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.Reservation;
import com.eventbooking.domain.enums.StatutReservation;
import com.eventbooking.security.SecurityService;
import com.eventbooking.service.EventService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.views.MainLayout;
import com.eventbooking.views.components.StatCard;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View for displaying event reservations
 */
@Route(value = "organizer/event/reservations", layout = MainLayout.class)
@PageTitle("Réservations | Event Booking")
@RolesAllowed({ "ORGANIZER", "ADMIN" })
public class EventReservationsView extends VerticalLayout implements HasUrlParameter<Long> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SecurityService securityService;
    private final EventService eventService;
    private final ReservationService reservationService;

    private Event event;
    private final Grid<Reservation> grid = new Grid<>(Reservation.class, false);
    private ComboBox<StatutReservation> statusFilter;
    private TextField searchField;

    public EventReservationsView(SecurityService securityService, EventService eventService,
            ReservationService reservationService) {
        this.securityService = securityService;
        this.eventService = eventService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long eventId) {
        try {
            event = eventService.findById(eventId);
            buildView();
        } catch (Exception e) {
            Notification.show("Événement non trouvé", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate(MyEventsView.class));
        }
    }

    private void buildView() {
        removeAll();

        createHeader();
        createStatistics();
        createFilters();
        configureGrid();
        loadReservations(null);
    }

    private void createHeader() {
        H2 title = new H2("Réservations: " + event.getTitre());
        title.getStyle().set("color", "#667eea");

        Button backButton = new Button("Retour", VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(MyEventsView.class)));

        HorizontalLayout header = new HorizontalLayout(backButton, title);
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(true);

        add(header);
    }

    private void createStatistics() {
        H3 statsTitle = new H3("Statistiques");

        List<Reservation> allReservations = reservationService.getEventReservations(event.getId());

        long totalReservations = allReservations.size();
        int totalPlaces = allReservations.stream()
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
        double totalRevenue = allReservations.stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        StatCard reservationsCard = new StatCard(
                "Réservations Totales",
                String.valueOf(totalReservations),
                VaadinIcon.TICKET,
                "#2196F3");

        StatCard placesCard = new StatCard(
                "Places Réservées",
                totalPlaces + " / " + event.getCapaciteMax(),
                VaadinIcon.GROUP,
                "#4CAF50");

        StatCard revenueCard = new StatCard(
                "Revenu Généré",
                String.format("%.2f DH", totalRevenue),
                VaadinIcon.MONEY,
                "#FF9800");

        statsLayout.add(reservationsCard, placesCard, revenueCard);

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
        statusFilter.addValueChangeListener(e -> loadReservations(e.getValue()));

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Code ou nom d'utilisateur");
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

        grid.addColumn(
                reservation -> reservation.getUtilisateur().getPrenom() + " " + reservation.getUtilisateur().getNom())
                .setHeader("Client")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(reservation -> reservation.getUtilisateur().getEmail())
                .setHeader("Email")
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

        grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
                .setHeader("Actions")
                .setAutoWidth(true);

        add(grid);
    }

    private HorizontalLayout createActionButtons(Reservation reservation) {
        Button confirmButton = new Button("Confirmer", VaadinIcon.CHECK.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        confirmButton.setEnabled(reservation.getStatut() == StatutReservation.EN_ATTENTE);
        confirmButton.addClickListener(e -> confirmReservation(reservation));

        return new HorizontalLayout(confirmButton);
    }

    private void confirmReservation(Reservation reservation) {
        try {
            reservationService.confirmReservation(reservation.getId());
            Notification.show("Réservation confirmée", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadReservations(statusFilter.getValue());
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadReservations(StatutReservation status) {
        List<Reservation> reservations = reservationService.getEventReservations(event.getId());

        if (status != null) {
            reservations = reservations.stream()
                    .filter(r -> r.getStatut() == status)
                    .toList();
        }

        grid.setItems(reservations);
    }

    private void filterReservations(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            loadReservations(statusFilter.getValue());
            return;
        }

        List<Reservation> reservations = reservationService.getEventReservations(event.getId());
        String lowerSearch = searchTerm.toLowerCase();

        reservations = reservations.stream()
                .filter(r -> r.getCodeReservation().toLowerCase().contains(lowerSearch) ||
                        r.getUtilisateur().getNom().toLowerCase().contains(lowerSearch) ||
                        r.getUtilisateur().getPrenom().toLowerCase().contains(lowerSearch) ||
                        r.getUtilisateur().getEmail().toLowerCase().contains(lowerSearch))
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
