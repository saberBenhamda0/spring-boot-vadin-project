package com.eventbooking.views.components;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.views.EventDetailView;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;

/**
 * Reusable event card component
 */
public class EventCard extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EventCard(Event event) {
        setWidth("300px");
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin", "10px")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s, box-shadow 0.2s");

        // Hover effect
        addClassName("event-card-hover");

        Span category = new Span(event.getCategorie().getIcon() + " " + event.getCategorie().getLabel());
        category.getStyle()
                .set("background", event.getCategorie().getColor())
                .set("color", "white")
                .set("padding", "5px 10px")
                .set("border-radius", "4px")
                .set("font-size", "0.9em")
                .set("display", "inline-block");

        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "10px 0")
                .set("color", "#333");

        Paragraph date = new Paragraph("📅 " + event.getDateDebut().format(DATE_FORMATTER));
        date.getStyle()
                .set("margin", "5px 0")
                .set("color", "#666")
                .set("font-size", "0.9em");

        Paragraph location = new Paragraph("📍 " + event.getVille());
        location.getStyle()
                .set("margin", "5px 0")
                .set("color", "#666")
                .set("font-size", "0.9em");

        Paragraph price = new Paragraph(event.getPrixUnitaire() + " DH");
        price.getStyle()
                .set("font-size", "1.3em")
                .set("font-weight", "bold")
                .set("color", "#2196F3")
                .set("margin", "10px 0");

        Span status = new Span(event.getStatut().getLabel());
        status.getStyle()
                .set("background", getStatusColor(event.getStatut().name()))
                .set("color", "white")
                .set("padding", "3px 8px")
                .set("border-radius", "4px")
                .set("font-size", "0.8em")
                .set("display", "inline-block");

        add(category, title, date, location, price, status);

        addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(EventDetailView.class, event.getId())));
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "PUBLIE" -> "#4CAF50";
            case "BROUILLON" -> "#FF9800";
            case "ANNULE" -> "#F44336";
            case "TERMINE" -> "#9E9E9E";
            default -> "#2196F3";
        };
    }
}
