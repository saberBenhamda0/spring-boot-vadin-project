package com.eventbooking.views;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.enums.Categorie;
import com.eventbooking.domain.enums.StatutEvent;
import com.eventbooking.service.EventService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route("events")
@PageTitle("Événements | Event Booking")
@AnonymousAllowed
public class EventListView extends VerticalLayout {

    private final EventService eventService;
    private final Grid<Event> grid = new Grid<>(Event.class, false);

    private TextField searchField;
    private ComboBox<Categorie> categorieFilter;
    private ComboBox<String> villeFilter;

    private List<Event> allEvents;

    public EventListView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Tous les événements");

        createFilters();
        createGrid();

        add(title, createFilterLayout(), grid);

        loadEvents();
    }

    private void createFilters() {
        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Titre de l'événement...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        categorieFilter = new ComboBox<>("Catégorie");
        categorieFilter.setItems(Categorie.values());
        categorieFilter.setItemLabelGenerator(Categorie::getLabel);
        categorieFilter.setClearButtonVisible(true);
        categorieFilter.addValueChangeListener(e -> applyFilters());

        villeFilter = new ComboBox<>("Ville");
        villeFilter.setItems("Casablanca", "Rabat", "Marrakech", "Tanger", "Fès");
        villeFilter.setClearButtonVisible(true);
        villeFilter.addValueChangeListener(e -> applyFilters());
    }

    private HorizontalLayout createFilterLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.add(searchField, categorieFilter, villeFilter);
        return layout;
    }

    private void createGrid() {
        grid.addColumn(event -> event.getCategorie().getIcon() + " " + event.getCategorie().getLabel())
                .setHeader("Catégorie")
                .setAutoWidth(true);

        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setAutoWidth(true);

        grid.addColumn(Event::getVille)
                .setHeader("Ville")
                .setAutoWidth(true);

        grid.addColumn(event -> event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date")
                .setAutoWidth(true);

        grid.addColumn(event -> event.getPrixUnitaire() + " DH")
                .setHeader("Prix")
                .setAutoWidth(true);

        grid.addComponentColumn(event -> {
            Span badge = new Span(event.getStatut().getLabel());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", event.getStatut().getColor())
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "4px");
            return badge;
        }).setHeader("Statut").setAutoWidth(true);

        grid.addComponentColumn(event -> {
            Button viewButton = new Button("Voir détails");
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            viewButton
                    .addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(EventDetailView.class, event.getId())));
            return viewButton;
        }).setHeader("Actions");

        grid.setHeight("600px");
    }

    private void loadEvents() {
        allEvents = eventService.getPublishedEvents();
        applyFilters();
    }

    private void applyFilters() {
        List<Event> filtered = allEvents.stream()
                .filter(event -> {
                    if (searchField.getValue() != null && !searchField.getValue().isEmpty()) {
                        return event.getTitre().toLowerCase()
                                .contains(searchField.getValue().toLowerCase());
                    }
                    return true;
                })
                .filter(event -> {
                    if (categorieFilter.getValue() != null) {
                        return event.getCategorie().equals(categorieFilter.getValue());
                    }
                    return true;
                })
                .filter(event -> {
                    if (villeFilter.getValue() != null) {
                        return event.getVille().equals(villeFilter.getValue());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        grid.setItems(filtered);
    }
}
