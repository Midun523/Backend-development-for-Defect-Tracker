package com.defecttracker.service;

import com.defecttracker.dto.request.ModuleCreateRequest;
import com.defecttracker.entity.Module;
import com.defecttracker.entity.ModuleLeaderAllocation;

import java.util.List;

public interface ModuleService {
    Module createModule(Long projectId, ModuleCreateRequest request);
    Module updateModule(Long projectId, Long moduleId, ModuleCreateRequest request);
    Module getModuleById(Long moduleId);
    List<Module> getModulesByProjectId(Long projectId);
    void deleteModule(Long moduleId);

    ModuleLeaderAllocation allocateModuleLeader(Long moduleId, Long employeeId);
    void deallocateModuleLeader(Long allocationId);
    ModuleLeaderAllocation getAllocatedLeader(Long moduleId);
}
