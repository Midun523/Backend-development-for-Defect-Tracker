package com.defecttracker.modules.project.repository;

import com.defecttracker.modules.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);
    boolean existsByName(String name);
    List<Project> findByActiveTrue();

    @Query("SELECT p FROM Project p WHERE p.active = true AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.status) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Project> searchProjects(@Param("query") String query, Pageable pageable);
}
