package com.defecttracker.modules.access.repository;

import com.defecttracker.modules.access.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeId(String employeeId);
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);

    List<Employee> findByActiveTrue();

    // Bench employees (employees with capacity > 0)
    @Query("SELECT e FROM Employee e WHERE e.active = true AND e.currentCapacity > 0")
    List<Employee> findBenchEmployees();

    @Query("SELECT e FROM Employee e WHERE e.active = true AND e.currentCapacity >= :requiredCapacity")
    List<Employee> findEmployeesWithCapacity(@Param("requiredCapacity") BigDecimal requiredCapacity);

    // Search and filter employees
    @Query("SELECT e FROM Employee e WHERE e.active = true AND (" +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Employee> searchEmployees(@Param("query") String query, Pageable pageable);
}
