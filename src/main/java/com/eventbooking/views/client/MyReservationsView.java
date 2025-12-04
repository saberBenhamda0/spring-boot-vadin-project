package com.eventbooking.views.client;

import com.eventbooking.domain.entity.Reservation;
import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.StatutReservation;
import com.eventbooking.security.SecurityService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View for displaying user's reservations
 */
@Route(value = "my-reservations", layout = MainLayout.class)
@PageTitle("Mes Réservations | Event Booking")
@RolesAllowed("CLIENT")
public class MyReservationsView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SecurityService securityService;
    private final ReservationService reservationService;
    private final Grid<Reservation> grid = new Grid<>(Reservation.class, false);

    private ComboBox<StatutReservation> statusFilter;
    private TextField searchField;
    private User currentUser;

    public MyReservationsView(SecurityService securityService, ReservationService reservationService) {
        this.securityService = securityService;
        this.reservationService = reservationService;

        this.currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createFilters();
        configureGrid();
        loadReservations(null);
    }

    private void createHeader() {
        H2 title = new H2("Mes Réservations");
        title.getStyle().set("color", "#667eea");
        add(title);
    }

    private void createFilters() {
        statusFilter = new ComboBox<>("Filtrer par statut");
        statusFilter.setItems(StatutReservation.values());
        statusFilter.setItemLabelGenerator(StatutReservation::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> loadReservations(e.getValue()));

        searchField = new TextField("Rechercher par code");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterByCode(e.getValue()));

        HorizontalLayout filters = new HorizontalLayout(statusFilter, searchField);
        filters.setAlignItems(Alignment.END);
        add(filters);
    }

    private void configureGrid() {
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(reservation -> reservation.getEvenement().getTitre())
                .setHeader("Événement")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(reservation -> reservation.getEvenement().getDateDebut().format(DATE_FORMATTER))
                .setHeader("Date")
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

        grid.addClassName("reservation-grid");
        add(grid);
    }

    private HorizontalLayout createActionButtons(Reservation reservation) {
        Button cancelButton = new Button("Annuler", VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        cancelButton.setEnabled(reservation.getStatut().canCancel() &&
                isMoreThan48HoursBefore(reservation.getEvenement().getDateDebut()));
        cancelButton.addClickListener(e -> confirmCancellation(reservation));

        return new HorizontalLayout(cancelButton);
    }

    private boolean isMoreThan48HoursBefore(LocalDateTime eventDate) {
        return LocalDateTime.now().plusHours(48).isBefore(eventDate);
    }

    private void confirmCancellation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmer l'annulation");
        dialog.setText("Êtes-vous sûr de vouloir annuler cette réservation ?");

        dialog.setCancelable(true);
        dialog.setCancelText("Non");

        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                reservationService.cancelReservation(reservation.getId());
                Notification.show("Réservation annulée avec succès", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                loadReservations(statusFilter.getValue());
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void loadReservations(StatutReservation status) {
        List<Reservation> reservations = reservationService.getUserReservations(currentUser.getId(), status);
        grid.setItems(reservations);
    }

    private void filterByCode(String code) {
        if (code == null || code.isEmpty()) {
            loadReservations(statusFilter.getValue());
        } else {
            reservationService.verifyReservationByCode(code)
                    .ifPresentOrElse(
                            reservation -> grid.setItems(List.of(reservation)),
                            () -> grid.setItems(List.of()));
        }
    }

    private String getStatusColor(StatutReservation status) {
        return switch (status) {
            case CONFIRMEE -> "#4CAF50";
            case EN_ATTENTE -> "#FF9800";
            case ANNULEE -> "#F44336";
        };
    }
}
