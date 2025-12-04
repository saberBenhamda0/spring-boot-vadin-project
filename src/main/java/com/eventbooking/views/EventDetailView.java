package com.eventbooking.views;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.service.EventService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "event/:eventId", layout = MainLayout.class)
@PageTitle("Détails de l'événement | Event Booking")
@AnonymousAllowed
public class EventDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventService eventService;
    private Event event;

    private VerticalLayout contentLayout;

    public EventDetailView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);

        contentLayout = new VerticalLayout();
        contentLayout.setWidthFull();
        add(contentLayout);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long eventId) {
        try {
            event = eventService.findById(eventId);
            displayEventDetails();
        } catch (Exception e) {
            contentLayout.add(new H2("Événement non trouvé"));
        }
    }

    private void displayEventDetails() {
        contentLayout.removeAll();

        // Header with category badge
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        Span categoryBadge = new Span(event.getCategorie().getIcon() + " " + event.getCategorie().getLabel());
        categoryBadge.getStyle()
                .set("background", event.getCategorie().getColor())
                .set("color", "white")
                .set("padding", "8px 15px")
                .set("border-radius", "4px")
                .set("font-weight", "bold");

        header.add(categoryBadge);

        // Title
        H1 title = new H1(event.getTitre());
        title.getStyle().set("color", "#333").set("margin", "20px 0");

        // Event info card
        VerticalLayout infoCard = new VerticalLayout();
        infoCard.setWidthFull();
        infoCard.setPadding(true);
        infoCard.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.1)");

        // Description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(event.getDescription());
            description.getStyle().set("color", "#666").set("line-height", "1.6");
            infoCard.add(new H3("Description"), description);
        }

        // Event details
        HorizontalLayout detailsLayout = new HorizontalLayout();
        detailsLayout.setWidthFull();
        detailsLayout.getStyle().set("flex-wrap", "wrap");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

        detailsLayout.add(
                createInfoItem("📅 Date de début", event.getDateDebut().format(formatter)),
                createInfoItem("🏁 Date de fin", event.getDateFin().format(formatter)),
                createInfoItem("📍 Lieu", event.getLieu()),
                createInfoItem("🌆 Ville", event.getVille()),
                createInfoItem("👥 Capacité", event.getCapaciteMax() + " places"),
                createInfoItem("💰 Prix", event.getPrixUnitaire() + " DH"));

        infoCard.add(new H3("Informations"), detailsLayout);

        // Available seats
        int availableSeats = eventService.calculateAvailableSeats(event.getId());
        Span availabilityBadge = new Span("Places disponibles: " + availableSeats);
        availabilityBadge.getStyle()
                .set("background", availableSeats > 0 ? "#4CAF50" : "#F44336")
                .set("color", "white")
                .set("padding", "10px 20px")
                .set("border-radius", "4px")
                .set("font-size", "1.1em")
                .set("font-weight", "bold")
                .set("display", "inline-block")
                .set("margin", "20px 0");

        // Reserve button
        Button reserveButton = new Button("Réserver maintenant");
        reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveButton.setEnabled(availableSeats > 0);
        reserveButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("reserve/" + event.getId())));

        Button backButton = new Button("← Retour aux événements",
                e -> getUI().ifPresent(ui -> ui.navigate(EventListView.class)));

        HorizontalLayout buttonLayout = new HorizontalLayout(backButton, reserveButton);
        buttonLayout.setSpacing(true);

        contentLayout.add(header, title, infoCard, availabilityBadge, buttonLayout);
    }

    private VerticalLayout createInfoItem(String label, String value) {
        VerticalLayout item = new VerticalLayout();
        item.setSpacing(false);
        item.setPadding(false);
        item.setWidth("200px");
        item.getStyle().set("margin", "10px");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-weight", "bold")
                .set("color", "#2196F3")
                .set("font-size", "0.9em");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "#333")
                .set("font-size", "1.1em");

        item.add(labelSpan, valueSpan);
        return item;
    }
}
