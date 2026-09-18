package com.defecttracker.repository;

import com.defecttracker.entity.SubModuleDevAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubModuleDevAllocationRepository extends JpaRepository<SubModuleDevAllocation, Long> {

    @Query("SELECT s FROM SubModuleDevAllocation s WHERE s.subModule.id = :subModuleId")
    List<SubModuleDevAllocation> findBySubModuleId(@Param("subModuleId") Long subModuleId);

    @Query("SELECT s FROM SubModuleDevAllocation s WHERE s.subModule.id = :subModuleId AND s.employee.id = :employeeId")
    Optional<SubModuleDevAllocation> findBySubModuleIdAndEmployeeId(@Param("subModuleId") Long subModuleId, @Param("employeeId") Long employeeId);

    @Modifying
    @Query("DELETE FROM SubModuleDevAllocation s WHERE s.subModule.id = :subModuleId AND s.employee.id = :employeeId")
    void deleteBySubModuleIdAndEmployeeId(@Param("subModuleId") Long subModuleId, @Param("employeeId") Long employeeId);
}

