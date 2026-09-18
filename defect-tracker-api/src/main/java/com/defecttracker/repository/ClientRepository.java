package com.defecttracker.repository;

import com.defecttracker.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByClientName(String clientName);
    Optional<Client> findByEmail(String email);

    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.clientName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.country) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Client> searchClients(@Param("query") String query, Pageable pageable);
}
