package com.eventbooking.views;

import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Role;
import com.eventbooking.security.SecurityService;
import com.eventbooking.views.admin.AdminDashboardView;
import com.eventbooking.views.admin.AllEventsManagementView;
import com.eventbooking.views.admin.AllReservationsView;
import com.eventbooking.views.admin.UserManagementView;
import com.eventbooking.views.client.DashboardView;
import com.eventbooking.views.client.MyReservationsView;
import com.eventbooking.views.client.ProfileView;
import com.eventbooking.views.organizer.MyEventsView;
import com.eventbooking.views.organizer.OrganizerDashboardView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Optional;

/**
 * Main layout with navigation menu
 */
public class MainLayout extends AppLayout {

    private final SecurityService securityService;
    private User currentUser;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;

        Optional<User> userOpt = securityService.getAuthenticatedUser();
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
            createHeader();
            createDrawer();
        }
    }

    private void createHeader() {
        H1 logo = new H1("Event Booking");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM);
        logo.getStyle().set("color", "#667eea");

        // User info
        Span userName = new Span(currentUser.getPrenom() + " " + currentUser.getNom());
        userName.getStyle()
                .set("margin-right", "10px")
                .set("color", "#666");

        Span userRole = new Span(currentUser.getRole().getLabel());
        userRole.getStyle()
                .set("background", "#667eea")
                .set("color", "white")
                .set("padding", "4px 12px")
                .set("border-radius", "12px")
                .set("font-size", "0.85em")
                .set("margin-right", "15px");

        Button logout = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logout.addClickListener(e -> {
            securityService.logout();
        });

        HorizontalLayout userLayout = new HorizontalLayout(userName, userRole, logout);
        userLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM);

        HorizontalLayout headerLayout = new HorizontalLayout(header, userLayout);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setPadding(true);

        addToNavbar(headerLayout);
    }

    private void createDrawer() {
        VerticalLayout navigation = new VerticalLayout();
        navigation.setPadding(true);
        navigation.setSpacing(true);

        // Public links
        navigation.add(createNavLink("Accueil", HomeView.class, VaadinIcon.HOME));
        navigation.add(createNavLink("Événements", EventListView.class, VaadinIcon.CALENDAR));

        // Role-based navigation
        if (currentUser.getRole() == Role.CLIENT) {
            navigation.add(createNavLink("Mon Tableau de Bord", DashboardView.class, VaadinIcon.DASHBOARD));
            navigation.add(createNavLink("Mes Réservations", MyReservationsView.class, VaadinIcon.TICKET));
            navigation.add(createNavLink("Mon Profil", ProfileView.class, VaadinIcon.USER));
        } else if (currentUser.getRole() == Role.ORGANIZER) {
            navigation.add(createNavLink("Tableau de Bord", OrganizerDashboardView.class, VaadinIcon.DASHBOARD));
            navigation.add(createNavLink("Mes Événements", MyEventsView.class, VaadinIcon.CALENDAR));
            navigation.add(createNavLink("Mon Profil", ProfileView.class, VaadinIcon.USER));
        } else if (currentUser.getRole() == Role.ADMIN) {
            navigation.add(createNavLink("Tableau de Bord Admin", AdminDashboardView.class, VaadinIcon.DASHBOARD));
            navigation.add(createNavLink("Gestion Utilisateurs", UserManagementView.class, VaadinIcon.USERS));
            navigation.add(createNavLink("Gestion Événements", AllEventsManagementView.class, VaadinIcon.CALENDAR));
            navigation.add(createNavLink("Toutes les Réservations", AllReservationsView.class, VaadinIcon.TICKET));
        }

        addToDrawer(navigation);
    }

    private RouterLink createNavLink(String text, Class<? extends com.vaadin.flow.component.Component> view,
            VaadinIcon icon) {
        RouterLink link = new RouterLink();
        link.add(icon.create(), new Span(" " + text));
        link.setRoute(view);
        link.getStyle()
                .set("padding", "10px 15px")
                .set("border-radius", "8px")
                .set("text-decoration", "none")
                .set("color", "#333")
                .set("display", "flex")
                .set("align-items", "center")
                .set("transition", "background 0.2s");

        return link;
    }
}
