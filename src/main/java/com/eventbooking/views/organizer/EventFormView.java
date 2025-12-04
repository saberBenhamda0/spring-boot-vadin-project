package com.eventbooking.views.organizer;

import com.eventbooking.domain.entity.Event;
import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Categorie;
import com.eventbooking.domain.enums.StatutEvent;
import com.eventbooking.security.SecurityService;
import com.eventbooking.service.EventService;
import com.eventbooking.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;

/**
 * Form view for creating and editing events
 */
@Route(value = "organizer/event", layout = MainLayout.class)
@PageTitle("Événement | Event Booking")
@RolesAllowed({ "ORGANIZER", "ADMIN" })
public class EventFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final SecurityService securityService;
    private final EventService eventService;

    private User currentUser;
    private Event event;
    private boolean isEditMode = false;

    private Binder<Event> binder = new Binder<>(Event.class);

    private TextField titreField;
    private TextArea descriptionField;
    private ComboBox<Categorie> categorieField;
    private DateTimePicker dateDebutField;
    private DateTimePicker dateFinField;
    private TextField lieuField;
    private TextField villeField;
    private IntegerField capaciteMaxField;
    private NumberField prixUnitaireField;
    private TextField imageUrlField;

    public EventFormView(SecurityService securityService, EventService eventService) {
        this.securityService = securityService;
        this.eventService = eventService;

        this.currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        if (parameter.startsWith("edit/")) {
            isEditMode = true;
            Long eventId = Long.parseLong(parameter.substring(5));
            try {
                event = eventService.findById(eventId);
                // Check ownership
                if (!event.getOrganisateur().getId().equals(currentUser.getId()) &&
                        !currentUser.getRole().name().equals("ADMIN")) {
                    Notification.show("Vous n'êtes pas autorisé à modifier cet événement",
                            3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    getUI().ifPresent(ui -> ui.navigate(MyEventsView.class));
                    return;
                }
            } catch (Exception e) {
                Notification.show("Événement non trouvé", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                getUI().ifPresent(ui -> ui.navigate(MyEventsView.class));
                return;
            }
        } else if (parameter.equals("new")) {
            isEditMode = false;
            event = Event.builder()
                    .organisateur(currentUser)
                    .statut(StatutEvent.BROUILLON)
                    .build();
        }

        buildView();
    }

    private void buildView() {
        removeAll();

        createHeader();
        createForm();
        createButtons();
    }

    private void createHeader() {
        H2 title = new H2(isEditMode ? "Modifier l'Événement" : "Créer un Événement");
        title.getStyle().set("color", "#667eea");
        add(title);
    }

    private void createForm() {
        H3 formTitle = new H3("Informations de l'Événement");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Title
        titreField = new TextField("Titre");
        titreField.setRequired(true);
        titreField.setHelperText("5-100 caractères");
        binder.forField(titreField)
                .asRequired("Le titre est obligatoire")
                .withValidator(new StringLengthValidator("Le titre doit contenir entre 5 et 100 caractères", 5, 100))
                .bind(Event::getTitre, Event::setTitre);

        // Category
        categorieField = new ComboBox<>("Catégorie");
        categorieField.setItems(Categorie.values());
        categorieField.setItemLabelGenerator(cat -> cat.getIcon() + " " + cat.getLabel());
        categorieField.setRequired(true);
        binder.forField(categorieField)
                .asRequired("La catégorie est obligatoire")
                .bind(Event::getCategorie, Event::setCategorie);

        // Description
        descriptionField = new TextArea("Description");
        descriptionField.setMaxLength(1000);
        descriptionField.setHelperText("Maximum 1000 caractères");
        binder.forField(descriptionField)
                .withValidator(new StringLengthValidator("Maximum 1000 caractères", 0, 1000))
                .bind(Event::getDescription, Event::setDescription);

        // Start date
        dateDebutField = new DateTimePicker("Date de Début");
        dateDebutField.setMin(LocalDateTime.now());
        binder.forField(dateDebutField)
                .asRequired("La date de début est obligatoire")
                .bind(Event::getDateDebut, Event::setDateDebut);

        // End date
        dateFinField = new DateTimePicker("Date de Fin");
        binder.forField(dateFinField)
                .asRequired("La date de fin est obligatoire")
                .bind(Event::getDateFin, Event::setDateFin);

        // Location
        lieuField = new TextField("Lieu");
        lieuField.setRequired(true);
        binder.forField(lieuField)
                .asRequired("Le lieu est obligatoire")
                .bind(Event::getLieu, Event::setLieu);

        // City
        villeField = new TextField("Ville");
        villeField.setRequired(true);
        binder.forField(villeField)
                .asRequired("La ville est obligatoire")
                .bind(Event::getVille, Event::setVille);

        // Max capacity
        capaciteMaxField = new IntegerField("Capacité Maximale");
        capaciteMaxField.setRequired(true);
        capaciteMaxField.setMin(1);
        capaciteMaxField.setStepButtonsVisible(true);
        binder.forField(capaciteMaxField)
                .asRequired("La capacité est obligatoire")
                .withValidator(new IntegerRangeValidator("La capacité doit être au moins 1", 1, Integer.MAX_VALUE))
                .bind(Event::getCapaciteMax, Event::setCapaciteMax);

        // Unit price
        prixUnitaireField = new NumberField("Prix Unitaire (DH)");
        prixUnitaireField.setRequired(true);
        prixUnitaireField.setMin(0);
        prixUnitaireField.setStepButtonsVisible(true);
        binder.forField(prixUnitaireField)
                .asRequired("Le prix est obligatoire")
                .withValidator(new DoubleRangeValidator("Le prix doit être positif ou nul", 0.0, Double.MAX_VALUE))
                .bind(Event::getPrixUnitaire, Event::setPrixUnitaire);

        // Image URL
        imageUrlField = new TextField("URL de l'Image (optionnel)");
        binder.forField(imageUrlField)
                .bind(Event::getImageUrl, Event::setImageUrl);

        formLayout.add(titreField, categorieField);
        formLayout.add(descriptionField, 2);
        formLayout.add(dateDebutField, dateFinField);
        formLayout.add(lieuField, villeField);
        formLayout.add(capaciteMaxField, prixUnitaireField);
        formLayout.add(imageUrlField, 2);

        VerticalLayout formContainer = new VerticalLayout(formTitle, formLayout);
        formContainer.setPadding(true);
        formContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        add(formContainer);

        // Load data if editing
        if (isEditMode) {
            binder.readBean(event);
        }
    }

    private void createButtons() {
        Button saveDraftButton = new Button("Sauvegarder en Brouillon");
        saveDraftButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        saveDraftButton.addClickListener(e -> saveEvent(StatutEvent.BROUILLON));

        Button publishButton = new Button("Publier");
        publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        publishButton.addClickListener(e -> saveEvent(StatutEvent.PUBLIE));

        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(MyEventsView.class)));

        HorizontalLayout buttons = new HorizontalLayout(saveDraftButton, publishButton, cancelButton);
        buttons.getStyle().set("margin-top", "20px");

        add(buttons);
    }

    private void saveEvent(StatutEvent targetStatus) {
        try {
            // Validate form
            binder.writeBean(event);

            // Validate dates
            if (event.getDateFin().isBefore(event.getDateDebut())) {
                Notification.show("La date de fin doit être après la date de début",
                        3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Set status
            event.setStatut(targetStatus);

            // Save
            Event saved;
            if (isEditMode) {
                saved = eventService.modifyEvent(event.getId(), event, currentUser);
                if (targetStatus == StatutEvent.PUBLIE && event.getStatut() == StatutEvent.BROUILLON) {
                    saved = eventService.publishEvent(saved.getId());
                }
            } else {
                saved = eventService.createEvent(event, currentUser);
                if (targetStatus == StatutEvent.PUBLIE) {
                    saved = eventService.publishEvent(saved.getId());
                }
            }

            String message = targetStatus == StatutEvent.PUBLIE ? "Événement publié avec succès"
                    : "Événement sauvegardé en brouillon";

            Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            getUI().ifPresent(ui -> ui.navigate(MyEventsView.class));

        } catch (ValidationException e) {
            Notification.show("Veuillez corriger les erreurs dans le formulaire",
                    3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
