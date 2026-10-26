package com.ps.restaurant.repository;

import java.util.Optional;

import java.lang.Long;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ps.restaurant.model.User;

/** Provides user persistence and identifier-based lookups through Spring Data JPA. */
public interface UserRepository extends JpaRepository<User, Long> {
    /** Finds a user by email, returning an empty optional when no match exists. */
    Optional<User> findByEmail(String email);

    /** Finds a user by username, returning an empty optional when no match exists. */
    Optional<User> findByUser(String user);

    /** Finds a user whose username or email matches the supplied login identifier. */
    @Query("select u from User u where u.user = :identifier or u.email = :identifier")
    Optional<User> findByUserOrEmail(@Param("identifier") String identifier);

    /** Reports whether an account already uses the supplied email address. */
    boolean existsByEmail(String email);
}
