package com.eventbooking.service;

import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Role;
import com.eventbooking.exception.BadRequestException;
import com.eventbooking.exception.ConflictException;
import com.eventbooking.exception.ResourceNotFoundException;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.ReservationRepository;
import com.eventbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 1. Register new user (with validation and password hashing using BCrypt)
     */
    public User registerUser(User user) {
        // Check email uniqueness
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Un utilisateur avec cet email existe déjà");
        }

        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole(Role.CLIENT);
        }

        return userRepository.save(user);
    }

    /**
     * 2. Authenticate (email/password verification)
     */
    public Optional<User> authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(User::getActif)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }

    /**
     * 3. Update user profile
     */
    public User updateProfile(Long userId, User updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setNom(updatedUser.getNom());
        user.setPrenom(updatedUser.getPrenom());
        user.setTelephone(updatedUser.getTelephone());

        return userRepository.save(user);
    }

    /**
     * 4. Change password
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }

        // Validate new password
        if (newPassword.length() < 8) {
            throw new BadRequestException("Le nouveau mot de passe doit contenir au moins 8 caractères");
        }

        // Hash and save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 5. Deactivate account
     */
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setActif(false);
        userRepository.save(user);
    }

    /**
     * 6. Activate account
     */
    public void activateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setActif(true);
        userRepository.save(user);
    }

    /**
     * 7. Retrieve user statistics (events created, reservations made, total spent)
     * Using Streams API
     */
    public UserStatistics getUserStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        long eventsCreated = eventRepository.findByOrganisateur(user).stream().count();
        long reservationsMade = reservationRepository.findByUtilisateur(user).stream().count();
        Double totalSpent = reservationRepository.calculateTotalAmountByUser(user);

        return new UserStatistics(eventsCreated, reservationsMade, totalSpent);
    }

    /**
     * List users with filters
     */
    public List<User> listUsers(Role role, Boolean actif) {
        if (role != null && actif != null && actif) {
            return userRepository.findByRoleAndActifTrue(role);
        }
        return userRepository.findAll();
    }

    /**
     * Find user by ID
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Search users by name
     */
    public List<User> searchByName(String searchTerm) {
        return userRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(searchTerm, searchTerm);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Update user (for admin)
     */
    public User updateUser(Long userId, User updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setNom(updatedUser.getNom());
        user.setPrenom(updatedUser.getPrenom());
        user.setEmail(updatedUser.getEmail());
        user.setTelephone(updatedUser.getTelephone());

        return userRepository.save(user);
    }

    /**
     * Deactivate user (for admin)
     */
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setActif(false);
        userRepository.save(user);
    }

    /**
     * Activate user (for admin)
     */
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setActif(true);
        userRepository.save(user);
    }

    /**
     * Change user role (for admin)
     */
    public void changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setRole(newRole);
        userRepository.save(user);
    }

    // DTO for user statistics
    public record UserStatistics(long eventsCreated, long reservationsMade, Double totalSpent) {
    }
}
