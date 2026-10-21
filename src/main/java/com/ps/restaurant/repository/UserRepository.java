package com.ps.restaurant.repository;

import java.util.Optional;

import java.lang.Long;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ps.restaurant.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUser(String user);

    @Query("select u from User u where u.user = :identifier or u.email = :identifier")
    Optional<User> findByUserOrEmail(@Param("identifier") String identifier);

    boolean existsByEmail(String email);
}
