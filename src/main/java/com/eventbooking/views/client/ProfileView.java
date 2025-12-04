package com.eventbooking.views.client;

import com.eventbooking.domain.entity.User;
import com.eventbooking.security.SecurityService;
import com.eventbooking.service.ReservationService;
import com.eventbooking.service.UserService;
import com.eventbooking.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * Profile view for users
 */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Mon Profil | Event Booking")
@RolesAllowed({ "CLIENT", "ORGANIZER" })
public class ProfileView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserService userService;
    private final ReservationService reservationService;

    private User currentUser;
    private Binder<User> binder = new Binder<>(User.class);

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;

    public ProfileView(SecurityService securityService, UserService userService,
            ReservationService reservationService) {
        this.securityService = securityService;
        this.userService = userService;
        this.reservationService = reservationService;

        this.currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");
        getStyle().set("margin", "0 auto");

        createHeader();
        createProfileForm();
        createPasswordSection();
        createStatisticsSection();
        createDangerZone();
    }

    private void createHeader() {
        H2 title = new H2("Mon Profil");
        title.getStyle().set("color", "#667eea");
        add(title);
    }

    private void createProfileForm() {
        H3 formTitle = new H3("Informations Personnelles");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        nomField = new TextField("Nom");
        prenomField = new TextField("Prénom");
        emailField = new EmailField("Email");
        telephoneField = new TextField("Téléphone");

        formLayout.add(nomField, prenomField, emailField, telephoneField);

        // Bind fields
        binder.forField(nomField)
                .asRequired("Le nom est obligatoire")
                .bind(User::getNom, User::setNom);

        binder.forField(prenomField)
                .asRequired("Le prénom est obligatoire")
                .bind(User::getPrenom, User::setPrenom);

        binder.forField(emailField)
                .asRequired("L'email est obligatoire")
                .bind(User::getEmail, User::setEmail);

        binder.forField(telephoneField)
                .bind(User::getTelephone, User::setTelephone);

        binder.readBean(currentUser);

        Button saveButton = new Button("Enregistrer les modifications");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveProfile());

        VerticalLayout formContainer = new VerticalLayout(formTitle, formLayout, saveButton);
        formContainer.setPadding(true);
        formContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        add(formContainer);
    }

    private void createPasswordSection() {
        H3 passwordTitle = new H3("Changer le mot de passe");

        FormLayout passwordForm = new FormLayout();
        passwordForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        PasswordField currentPassword = new PasswordField("Mot de passe actuel");
        PasswordField newPassword = new PasswordField("Nouveau mot de passe");
        PasswordField confirmPassword = new PasswordField("Confirmer le mot de passe");

        passwordForm.add(currentPassword, newPassword, confirmPassword);

        Button changePasswordButton = new Button("Changer le mot de passe");
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changePasswordButton.addClickListener(e -> {
            if (newPassword.getValue().equals(confirmPassword.getValue())) {
                if (newPassword.getValue().length() >= 8) {
                    try {
                        userService.changePassword(currentUser.getId(), currentPassword.getValue(),
                                newPassword.getValue());
                        Notification.show("Mot de passe modifié avec succès", 3000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        currentPassword.clear();
                        newPassword.clear();
                        confirmPassword.clear();
                    } catch (Exception ex) {
                        Notification.show("Erreur: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                } else {
                    Notification.show("Le mot de passe doit contenir au moins 8 caractères",
                            3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show("Les mots de passe ne correspondent pas", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        VerticalLayout passwordContainer = new VerticalLayout(passwordTitle, passwordForm, changePasswordButton);
        passwordContainer.setPadding(true);
        passwordContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin-top", "20px");

        add(passwordContainer);
    }

    private void createStatisticsSection() {
        H3 statsTitle = new H3("Mes Statistiques");

        var stats = reservationService.getReservationStatistics(currentUser.getId());

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        com.eventbooking.views.components.StatCard totalReservations = new com.eventbooking.views.components.StatCard(
                "Réservations Totales",
                String.valueOf(stats.totalReservations()),
                com.vaadin.flow.component.icon.VaadinIcon.TICKET,
                "#2196F3");

        com.eventbooking.views.components.StatCard upcomingEvents = new com.eventbooking.views.components.StatCard(
                "Événements à Venir",
                String.valueOf(stats.upcomingEvents()),
                com.vaadin.flow.component.icon.VaadinIcon.CALENDAR,
                "#4CAF50");

        com.eventbooking.views.components.StatCard totalSpent = new com.eventbooking.views.components.StatCard(
                "Total Dépensé",
                String.format("%.2f DH", stats.totalSpent()),
                com.vaadin.flow.component.icon.VaadinIcon.MONEY,
                "#FF9800");

        statsLayout.add(totalReservations, upcomingEvents, totalSpent);

        VerticalLayout statsContainer = new VerticalLayout(statsTitle, statsLayout);
        statsContainer.setPadding(true);
        statsContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin-top", "20px");

        add(statsContainer);
    }

    private void createDangerZone() {
        H3 dangerTitle = new H3("Zone de danger");
        dangerTitle.getStyle().set("color", "#F44336");

        Button deactivateButton = new Button("Désactiver mon compte");
        deactivateButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deactivateButton.addClickListener(e -> confirmDeactivation());

        VerticalLayout dangerContainer = new VerticalLayout(dangerTitle, deactivateButton);
        dangerContainer.setPadding(true);
        dangerContainer.getStyle()
                .set("background", "#ffebee")
                .set("border-radius", "8px")
                .set("border", "1px solid #F44336")
                .set("margin-top", "20px");

        add(dangerContainer);
    }

    private void saveProfile() {
        try {
            binder.writeBean(currentUser);
            userService.updateUser(currentUser.getId(), currentUser);
            Notification.show("Profil mis à jour avec succès", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (ValidationException e) {
            Notification.show("Veuillez corriger les erreurs", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeactivation() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Désactiver le compte");
        dialog.setText(
                "Êtes-vous sûr de vouloir désactiver votre compte ? Cette action peut être annulée par un administrateur.");

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");

        dialog.setConfirmText("Oui, désactiver");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                userService.deactivateUser(currentUser.getId());
                Notification.show("Compte désactivé", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                securityService.logout();
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }
}
