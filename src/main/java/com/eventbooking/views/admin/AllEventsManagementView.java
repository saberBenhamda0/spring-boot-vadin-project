package com.eventbooking.views.admin;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.enums.StatutEvent;
import com.eventbooking.security.SecurityService;
import com.eventbooking.service.EventService;
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
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View for managing all events (admin)
 */
@Route(value = "admin/events", layout = MainLayout.class)
@PageTitle("Gestion Événements | Event Booking")
@RolesAllowed("ADMIN")
public class AllEventsManagementView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SecurityService securityService;
    private final EventService eventService;
    private final ReservationService reservationService;
    private final Grid<Event> grid = new Grid<>(Event.class, false);

    private ComboBox<StatutEvent> statusFilter;
    private TextField searchField;

    public AllEventsManagementView(SecurityService securityService, EventService eventService,
            ReservationService reservationService) {
        this.securityService = securityService;
        this.eventService = eventService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createFilters();
        configureGrid();
        loadEvents();
    }

    private void createHeader() {
        H2 title = new H2("Gestion de Tous les Événements");
        title.getStyle().set("color", "#667eea");
        add(title);
    }

    private void createFilters() {
        statusFilter = new ComboBox<>("Filtrer par statut");
        statusFilter.setItems(StatutEvent.values());
        statusFilter.setItemLabelGenerator(StatutEvent::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> loadEvents());

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Titre ou ville");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterEvents(e.getValue()));

        HorizontalLayout filters = new HorizontalLayout(statusFilter, searchField);
        filters.setAlignItems(Alignment.END);
        add(filters);
    }

    private void configureGrid() {
        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(event -> event.getOrganisateur().getPrenom() + " " +
                event.getOrganisateur().getNom())
                .setHeader("Organisateur")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(event -> event.getCategorie().getLabel())
                .setHeader("Catégorie")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(event -> event.getDateDebut().format(DATE_FORMATTER))
                .setHeader("Date")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(Event::getVille)
                .setHeader("Ville")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(event -> {
            Span badge = new Span(event.getStatut().getLabel());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", getStatusColor(event.getStatut()))
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85em");
            return badge;
        })).setHeader("Statut").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(event -> {
            int reserved = reservationService.calculateTotalReservedPlaces(event.getId());
            int total = event.getCapaciteMax();

            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);

            Span label = new Span(reserved + " / " + total);
            label.getStyle().set("font-size", "0.9em");

            ProgressBar progressBar = new ProgressBar(0, total, reserved);
            progressBar.setWidth("100px");

            layout.add(label, progressBar);
            return layout;
        })).setHeader("Places").setAutoWidth(true);

        grid.addColumn(event -> String.format("%.2f DH", event.getPrixUnitaire()))
                .setHeader("Prix")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
                .setHeader("Actions")
                .setAutoWidth(true);

        add(grid);
    }

    private HorizontalLayout createActionButtons(Event event) {
        Button viewButton = new Button(VaadinIcon.EYE.create());
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        viewButton.getElement().setAttribute("title", "Voir les réservations");
        viewButton.addClickListener(
                e -> getUI().ifPresent(ui -> ui.navigate("organizer/event/" + event.getId() + "/reservations")));

        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editButton.getElement().setAttribute("title", "Modifier");
        editButton.setEnabled(event.getStatut().canModify());
        editButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("organizer/event/edit/" + event.getId())));

        Button publishButton = new Button(VaadinIcon.UPLOAD.create());
        publishButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        publishButton.getElement().setAttribute("title", "Publier");
        publishButton.setVisible(event.getStatut() == StatutEvent.BROUILLON);
        publishButton.addClickListener(e -> publishEvent(event));

        Button cancelButton = new Button(VaadinIcon.BAN.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        cancelButton.getElement().setAttribute("title", "Annuler");
        cancelButton.setEnabled(event.getStatut() == StatutEvent.PUBLIE);
        cancelButton.addClickListener(e -> confirmCancellation(event));

        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteButton.getElement().setAttribute("title", "Supprimer");
        deleteButton.addClickListener(e -> confirmDeletion(event));

        return new HorizontalLayout(viewButton, editButton, publishButton, cancelButton, deleteButton);
    }

    private void publishEvent(Event event) {
        try {
            eventService.publishEvent(event.getId());
            Notification.show("Événement publié avec succès", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadEvents();
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmCancellation(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Annuler l'événement");
        dialog.setText("Êtes-vous sûr de vouloir annuler cet événement ?");

        dialog.setCancelable(true);
        dialog.setCancelText("Non");

        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                eventService.cancelEvent(event.getId());
                Notification.show("Événement annulé", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                loadEvents();
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void confirmDeletion(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Supprimer l'événement");
        dialog.setText("Êtes-vous sûr de vouloir supprimer cet événement ? Cette action est irréversible.");

        dialog.setCancelable(true);
        dialog.setCancelText("Non");

        dialog.setConfirmText("Oui, supprimer");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                eventService.deleteEvent(event.getId());
                Notification.show("Événement supprimé", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                loadEvents();
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void loadEvents() {
        List<Event> events = eventService.getAllEvents();

        if (statusFilter.getValue() != null) {
            events = events.stream()
                    .filter(e -> e.getStatut() == statusFilter.getValue())
                    .toList();
        }

        grid.setItems(events);
    }

    private void filterEvents(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            loadEvents();
            return;
        }

        List<Event> events = eventService.getAllEvents();
        String lowerSearch = searchTerm.toLowerCase();

        events = events.stream()
                .filter(e -> e.getTitre().toLowerCase().contains(lowerSearch) ||
                        e.getVille().toLowerCase().contains(lowerSearch))
                .toList();

        grid.setItems(events);
    }

    private String getStatusColor(StatutEvent status) {
        return switch (status) {
            case PUBLIE -> "#4CAF50";
            case BROUILLON -> "#FF9800";
            case ANNULE -> "#F44336";
            case TERMINE -> "#9E9E9E";
        };
    }
}
