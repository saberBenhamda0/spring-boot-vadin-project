package com.eventbooking.views;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.enums.Categorie;
import com.eventbooking.domain.enums.StatutEvent;
import com.eventbooking.service.EventService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Accueil | Event Booking")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    private final EventService eventService;

    public HomeView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        createHeroSection();
        createFeaturedEvents();
    }

    private void createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.setWidthFull();
        hero.setPadding(true);
        hero.setSpacing(true);
        hero.setAlignItems(Alignment.CENTER);
        hero.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "60px 20px");

        H1 title = new H1("Découvrez les meilleurs événements");
        title.getStyle().set("margin", "0");

        Paragraph subtitle = new Paragraph("Concerts, théâtres, conférences et plus encore");
        subtitle.getStyle().set("font-size", "1.2em").set("margin", "10px 0 30px 0");

        Button browseButton = new Button("Voir tous les événements",
                e -> getUI().ifPresent(ui -> ui.navigate(EventListView.class)));
        browseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        browseButton.getStyle()
                .set("background", "white")
                .set("color", "#667eea");

        hero.add(title, subtitle, browseButton);
        add(hero);
    }

    private void createFeaturedEvents() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);

        H2 sectionTitle = new H2("Événements à la une");
        sectionTitle.getStyle().set("color", "#333");

        HorizontalLayout eventsLayout = new HorizontalLayout();
        eventsLayout.setWidthFull();
        eventsLayout.getStyle().set("flex-wrap", "wrap");

        // Get popular events
        List<Event> featuredEvents = eventService.getPopularEvents(6);

        for (Event event : featuredEvents) {
            if (event.getStatut() == StatutEvent.PUBLIE) {
                eventsLayout.add(createEventCard(event));
            }
        }

        section.add(sectionTitle, eventsLayout);
        add(section);
    }

    private VerticalLayout createEventCard(Event event) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin", "10px")
                .set("cursor", "pointer");

        Span category = new Span(event.getCategorie().getIcon() + " " + event.getCategorie().getLabel());
        category.getStyle()
                .set("background", event.getCategorie().getColor())
                .set("color", "white")
                .set("padding", "5px 10px")
                .set("border-radius", "4px")
                .set("font-size", "0.9em");

        H3 title = new H3(event.getTitre());
        title.getStyle().set("margin", "10px 0");

        Paragraph location = new Paragraph("📍 " + event.getVille());
        location.getStyle().set("margin", "5px 0").set("color", "#666");

        Paragraph price = new Paragraph(event.getPrixUnitaire() + " DH");
        price.getStyle()
                .set("font-size", "1.3em")
                .set("font-weight", "bold")
                .set("color", "#2196F3")
                .set("margin", "10px 0");

        card.add(category, title, location, price);

        card.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("event/" + event.getId())));

        return card;
    }

    // private void createCategoryFilters() {
    // VerticalLayout section = new VerticalLayout();
    // section.setWidthFull();
    // section.setPadding(true);

    // H2 sectionTitle = new H2("Parcourir par catégorie");
    // sectionTitle.getStyle().set("color", "#333");

    // HorizontalLayout categoriesLayout = new HorizontalLayout();
    // categoriesLayout.setWidthFull();
    // categoriesLayout.getStyle().set("flex-wrap", "wrap");

    // for (Categorie categorie : Categorie.values()) {
    // Button categoryButton = new Button(categorie.getIcon() + " " +
    // categorie.getLabel());
    // categoryButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
    // categoryButton.getStyle()
    // .set("background", categorie.getColor())
    // .set("color", "white")
    // .set("margin", "10px");

    // categoryButton.addClickListener(e -> getUI().ifPresent(ui ->
    // ui.navigate(EventListView.class)));

    // categoriesLayout.add(categoryButton);
    // }

    // section.add(sectionTitle, categoriesLayout);
    // add(section);
    // }
}
