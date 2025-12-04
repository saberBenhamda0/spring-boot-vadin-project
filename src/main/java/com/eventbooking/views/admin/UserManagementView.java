package com.eventbooking.views.admin;

import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Role;
import com.eventbooking.service.UserService;
import com.eventbooking.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * User management view for admins
 */
@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Gestion Utilisateurs | Event Booking")
@RolesAllowed("ADMIN")
public class UserManagementView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    private ComboBox<Role> roleFilter;
    private ComboBox<Boolean> statusFilter;
    private TextField searchField;

    public UserManagementView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createFilters();
        configureGrid();
        loadUsers();
    }

    private void createHeader() {
        H2 title = new H2("Gestion des Utilisateurs");
        title.getStyle().set("color", "#667eea");
        add(title);
    }

    private void createFilters() {
        roleFilter = new ComboBox<>("Filtrer par rôle");
        roleFilter.setItems(Role.values());
        roleFilter.setItemLabelGenerator(Role::getLabel);
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(e -> loadUsers());

        statusFilter = new ComboBox<>("Filtrer par statut");
        statusFilter.setItems(true, false);
        statusFilter.setItemLabelGenerator(active -> active ? "Actif" : "Inactif");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> loadUsers());

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Nom ou email");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterUsers(e.getValue()));

        HorizontalLayout filters = new HorizontalLayout(roleFilter, statusFilter, searchField);
        filters.setAlignItems(Alignment.END);
        add(filters);
    }

    private void configureGrid() {
        grid.addColumn(user -> user.getPrenom() + " " + user.getNom())
                .setHeader("Nom")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(User::getEmail)
                .setHeader("Email")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(user -> {
            Span badge = new Span(user.getRole().getLabel());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", getRoleColor(user.getRole()))
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85em");
            return badge;
        })).setHeader("Rôle").setAutoWidth(true);

        grid.addColumn(user -> user.getDateInscription().format(DATE_FORMATTER))
                .setHeader("Date Inscription")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(user -> {
            Span badge = new Span(user.getActif() ? "Actif" : "Inactif");
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", user.getActif() ? "#4CAF50" : "#F44336")
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85em");
            return badge;
        })).setHeader("Statut").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
                .setHeader("Actions")
                .setAutoWidth(true);

        add(grid);
    }

    private HorizontalLayout createActionButtons(User user) {
        Button toggleStatusButton = new Button(
                user.getActif() ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create());
        toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        toggleStatusButton.getElement().setAttribute("title",
                user.getActif() ? "Désactiver" : "Activer");
        toggleStatusButton.addClickListener(e -> toggleUserStatus(user));

        Button changeRoleButton = new Button(VaadinIcon.USER_CARD.create());
        changeRoleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        changeRoleButton.getElement().setAttribute("title", "Changer le rôle");
        changeRoleButton.addClickListener(e -> showChangeRoleDialog(user));

        return new HorizontalLayout(toggleStatusButton, changeRoleButton);
    }

    private void toggleUserStatus(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(user.getActif() ? "Désactiver l'utilisateur" : "Activer l'utilisateur");
        dialog.setText("Êtes-vous sûr de vouloir " +
                (user.getActif() ? "désactiver" : "activer") + " cet utilisateur ?");

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");

        dialog.setConfirmText("Confirmer");
        dialog.setConfirmButtonTheme("primary");

        dialog.addConfirmListener(e -> {
            try {
                if (user.getActif()) {
                    userService.deactivateUser(user.getId());
                } else {
                    userService.activateUser(user.getId());
                }
                Notification.show("Statut modifié avec succès", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                loadUsers();
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void showChangeRoleDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Changer le rôle de " + user.getPrenom() + " " + user.getNom());

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        ComboBox<Role> roleComboBox = new ComboBox<>("Nouveau rôle");
        roleComboBox.setItems(Role.values());
        roleComboBox.setItemLabelGenerator(Role::getLabel);
        roleComboBox.setValue(user.getRole());
        roleComboBox.setWidthFull();

        content.add(roleComboBox);
        dialog.add(content);

        Button saveButton = new Button("Enregistrer", e -> {
            try {
                userService.changeUserRole(user.getId(), roleComboBox.getValue());
                Notification.show("Rôle modifié avec succès", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                loadUsers();
            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void loadUsers() {
        List<User> users = userService.getAllUsers();

        // Apply filters
        if (roleFilter.getValue() != null) {
            users = users.stream()
                    .filter(u -> u.getRole() == roleFilter.getValue())
                    .toList();
        }

        if (statusFilter.getValue() != null) {
            users = users.stream()
                    .filter(u -> u.getActif() == statusFilter.getValue())
                    .toList();
        }

        grid.setItems(users);
    }

    private void filterUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            loadUsers();
            return;
        }

        List<User> users = userService.getAllUsers();
        String lowerSearch = searchTerm.toLowerCase();

        users = users.stream()
                .filter(u -> u.getNom().toLowerCase().contains(lowerSearch) ||
                        u.getPrenom().toLowerCase().contains(lowerSearch) ||
                        u.getEmail().toLowerCase().contains(lowerSearch))
                .toList();

        grid.setItems(users);
    }

    private String getRoleColor(Role role) {
        return switch (role) {
            case ADMIN -> "#F44336";
            case ORGANIZER -> "#FF9800";
            case CLIENT -> "#2196F3";
        };
    }
}
