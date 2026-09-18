package com.defecttracker.repository;

import com.defecttracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmailOrUserId(String email, String userId);
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
}
