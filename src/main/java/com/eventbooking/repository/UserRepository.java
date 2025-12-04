package com.eventbooking.repository;

import com.eventbooking.domain.entity.User;
import com.eventbooking.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all active users by role
     */
    List<User> findByRoleAndActifTrue(Role role);

    /**
     * Check email existence
     */
    boolean existsByEmail(String email);

    /**
     * Find users by nom or prenom (case-insensitive search)
     */
    List<User> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);

    /**
     * Count users by role
     */
    long countByRole(Role role);
}
