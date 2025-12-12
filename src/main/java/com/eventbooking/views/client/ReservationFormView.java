package com.eventbooking.views.client;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.Reservation;
import com.eventbooking.domain.entity.User;
import com.eventbooking.security.SecurityService;
import com.eventbooking.service.EventService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;

/**
 * Reservation form view
 */
@Route(value = "reserve", layout = MainLayout.class)
@PageTitle("Réserver | Event Booking")
@RolesAllowed("CLIENT")
public class ReservationFormView extends VerticalLayout implements HasUrlParameter<Long> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SecurityService securityService;
    private final EventService eventService;
    private final ReservationService reservationService;

    private Event event;
    private User currentUser;

    private IntegerField placesField;
    private TextArea commentField;
    private Span totalPriceLabel;
    private Span availabilityLabel;
    private Button submitButton;

    public ReservationFormView(SecurityService securityService, EventService eventService,
            ReservationService reservationService) {
        this.securityService = securityService;
        this.eventService = eventService;
        this.reservationService = reservationService;

        this.currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");
        getStyle().set("margin", "0 auto");
    }

    @Override
    public void setParameter(BeforeEvent event, Long eventId) {
        try {
            this.event = eventService.findById(eventId);
            buildView();
        } catch (Exception e) {
            Notification.show("Événement non trouvé", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate("events"));
        }
    }

    private void buildView() {
        removeAll();

        createEventInfo();
        createReservationForm();
    }

    private void createEventInfo() {
        H2 title = new H2("Réserver: " + event.getTitre());
        title.getStyle().set("color", "#667eea");

        VerticalLayout eventInfo = new VerticalLayout();
        eventInfo.setPadding(true);
        eventInfo.setSpacing(true);
        eventInfo.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        Paragraph date = new Paragraph("📅 " + event.getDateDebut().format(DATE_FORMATTER));
        Paragraph location = new Paragraph("📍 " + event.getLieu() + ", " + event.getVille());
        Paragraph price = new Paragraph("💰 " + event.getPrixUnitaire() + " DH par place");

        int available = eventService.calculateAvailableSeats(event.getId());
        availabilityLabel = new Span("Places disponibles: " + available + " / " + event.getCapaciteMax());
        availabilityLabel.getStyle()
                .set("background", available > 0 ? "#4CAF50" : "#F44336")
                .set("color", "white")
                .set("padding", "8px 15px")
                .set("border-radius", "4px")
                .set("font-weight", "bold");

        eventInfo.add(date, location, price, availabilityLabel);
        add(title, eventInfo);
    }

    private void createReservationForm() {
        H3 formTitle = new H3("Détails de la réservation");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1));

        placesField = new IntegerField("Nombre de places");
        placesField.setMin(1);
        placesField.setMax(10);
        placesField.setValue(1);
        placesField.setStepButtonsVisible(true);
        placesField.setHelperText("Maximum 10 places par réservation");
        placesField.addValueChangeListener(e -> updateTotalPrice());

        totalPriceLabel = new Span();
        totalPriceLabel.getStyle()
                .set("font-size", "1.5em")
                .set("font-weight", "bold")
                .set("color", "#2196F3")
                .set("margin", "10px 0");
        updateTotalPrice();

        commentField = new TextArea("Commentaire (optionnel)");
        commentField.setMaxLength(500);
        commentField.setHelperText("500 caractères maximum");

        formLayout.add(placesField, totalPriceLabel, commentField);

        submitButton = new Button("Confirmer la réservation");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        submitButton.addClickListener(e -> submitReservation());

        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("events")));

        HorizontalLayout buttons = new HorizontalLayout(submitButton, cancelButton);

        VerticalLayout formContainer = new VerticalLayout(formTitle, formLayout, buttons);
        formContainer.setPadding(true);
        formContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin-top", "20px");

        add(formContainer);
    }

    private void updateTotalPrice() {
        if (placesField.getValue() != null) {
            double total = event.getPrixUnitaire() * placesField.getValue();
            totalPriceLabel.setText(String.format("Total: %.2f DH", total));
        }
    }

    private void submitReservation() {
        try {
            // Validate
            if (placesField.getValue() == null || placesField.getValue() < 1) {
                Notification.show("Veuillez sélectionner au moins 1 place", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Check availability
            int available = eventService.calculateAvailableSeats(event.getId());
            if (placesField.getValue() > available) {
                Notification.show("Nombre de places insuffisant", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Create reservation
            Reservation reservation = Reservation.builder()
                    .utilisateur(currentUser)
                    .evenement(event)
                    .nombrePlaces(placesField.getValue())
                    .commentaire(commentField.getValue())
                    .build();

            Reservation saved = reservationService.createReservation(reservation);

            // Show success with code
            showSuccessDialog(saved.getCodeReservation());

        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showSuccessDialog(String code) {
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle("Réservation confirmée !");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);

        Paragraph message = new Paragraph("Votre réservation a été créée avec succès !");

        Span codeLabel = new Span("Code de réservation:");
        codeLabel.getStyle().set("font-weight", "bold");

        Span codeValue = new Span(code);
        codeValue.getStyle()
                .set("font-size", "1.5em")
                .set("color", "#667eea")
                .set("font-weight", "bold")
                .set("padding", "10px 20px")
                .set("background", "#f0f0f0")
                .set("border-radius", "8px");

        content.add(message, codeLabel, codeValue);
        dialog.add(content);

        Button closeButton = new Button("Voir mes réservations", e -> {
            dialog.close();
            getUI().ifPresent(ui -> ui.navigate(MyReservationsView.class));
        });
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(closeButton);
        dialog.open();
    }
}
