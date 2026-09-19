package com.defecttracker.modules.project.repository;

import com.defecttracker.modules.project.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByName(String name);
    boolean existsByName(String name);
}
