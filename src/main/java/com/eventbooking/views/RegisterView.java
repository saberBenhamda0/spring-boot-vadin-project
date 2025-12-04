package com.eventbooking.views;

import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Role;
import com.eventbooking.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Inscription | Event Booking")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserService userService;

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;

    public RegisterView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        createRegistrationForm();
    }

    private void createRegistrationForm() {
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setWidth("400px");
        formContainer.setPadding(true);
        formContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.1)");

        H2 title = new H2("Créer un compte");
        title.getStyle().set("margin", "0 0 20px 0");

        FormLayout formLayout = new FormLayout();

        nomField = new TextField("Nom");
        nomField.setRequired(true);
        nomField.setWidthFull();

        prenomField = new TextField("Prénom");
        prenomField.setRequired(true);
        prenomField.setWidthFull();

        emailField = new EmailField("Email");
        emailField.setRequired(true);
        emailField.setWidthFull();
        emailField.setErrorMessage("Format d'email invalide");

        telephoneField = new TextField("Téléphone");
        telephoneField.setWidthFull();

        passwordField = new PasswordField("Mot de passe");
        passwordField.setRequired(true);
        passwordField.setWidthFull();
        passwordField.setHelperText("Minimum 8 caractères");

        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setWidthFull();

        formLayout.add(nomField, prenomField, emailField, telephoneField,
                passwordField, confirmPasswordField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Button registerButton = new Button("S'inscrire", e -> handleRegistration());
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();

        RouterLink loginLink = new RouterLink("Déjà un compte ? Se connecter", LoginView.class);
        loginLink.getStyle().set("text-align", "center");

        formContainer.add(title, formLayout, registerButton, loginLink);
        add(formContainer);
    }

    private void handleRegistration() {
        // Validation
        if (nomField.isEmpty() || prenomField.isEmpty() || emailField.isEmpty() ||
                passwordField.isEmpty() || confirmPasswordField.isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires");
            return;
        }

        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        if (passwordField.getValue().length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        try {
            User user = User.builder()
                    .nom(nomField.getValue())
                    .prenom(prenomField.getValue())
                    .email(emailField.getValue())
                    .telephone(telephoneField.getValue())
                    .password(passwordField.getValue())
                    .role(Role.CLIENT)
                    .build();

            userService.registerUser(user);

            Notification.show("Inscription réussie ! Vous pouvez maintenant vous connecter.",
                    3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            getUI().ifPresent(ui -> ui.navigate(LoginView.class));

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
