package com.eventbooking.security;

import com.eventbooking.domain.entity.User;
import com.eventbooking.repository.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    /**
     * Get the currently authenticated user
     */
    public Optional<User> getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .flatMap(userDetails -> userRepository.findByEmail(userDetails.getUsername()));
    }

    /**
     * Logout the current user
     */
    public void logout() {
        authenticationContext.logout();
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return authenticationContext.isAuthenticated();
    }
}
