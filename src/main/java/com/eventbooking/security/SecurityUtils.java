package com.eventbooking.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    private final AuthenticationContext authenticationContext;

    public SecurityUtils(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public Optional<UserDetails> getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class);
    }

    public boolean isUserLoggedIn() {
        return authenticationContext.isAuthenticated();
    }

    public void logout() {
        authenticationContext.logout();
    }
}
