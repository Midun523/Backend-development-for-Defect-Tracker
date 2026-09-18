package com.defecttracker.repository;

import com.defecttracker.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserId(Long userId);
    List<Employee> findByDesignationId(Long designationId);
    List<Employee> findByStatus(String status);

    @Query("SELECT e FROM Employee e WHERE e.availability > 0 AND (LOWER(e.status) = 'active' OR (e.status IS NULL AND (e.user IS NULL OR LOWER(e.user.userStatus) = 'active')))")
    List<Employee> findBenchEmployees();

    @Query("SELECT e FROM Employee e WHERE e.designation.id = :designationId AND e.status = 'active'")
    List<Employee> findAvailableManagersByDesignation(@Param("designationId") Long designationId);

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Employee> searchEmployees(@Param("query") String query, Pageable pageable);
}
