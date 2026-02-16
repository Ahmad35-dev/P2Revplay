package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Locates a user by their email for the login process
    Optional<User> findByEmail(String email);

    // Helps us check if an email is already registered before creating a new account
    boolean existsByEmail(String email);
}
