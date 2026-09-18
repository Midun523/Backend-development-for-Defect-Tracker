package com.defecttracker.service.impl;

import com.defecttracker.dto.request.SubModuleCreateRequest;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.Module;
import com.defecttracker.entity.SubModule;
import com.defecttracker.entity.SubModuleDevAllocation;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.ModuleRepository;
import com.defecttracker.repository.SubModuleDevAllocationRepository;
import com.defecttracker.repository.SubModuleRepository;
import com.defecttracker.service.SubModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubModuleServiceImpl implements SubModuleService {

    private final SubModuleRepository subModuleRepository;
    private final ModuleRepository moduleRepository;
    private final EmployeeRepository employeeRepository;
    private final SubModuleDevAllocationRepository subModuleDevAllocationRepository;

    @Override
    @Transactional
    public SubModule createSubModule(Long moduleId, SubModuleCreateRequest request) {
        Long targetModuleId = moduleId != null ? moduleId : request.getModuleId();
        if (targetModuleId == null) {
            throw new com.defecttracker.exception.BadRequestException("Module ID is required to create a submodule");
        }
        Module module = moduleRepository.findById(targetModuleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", targetModuleId));

        SubModule subModule = SubModule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .module(module)
                .build();

        return subModuleRepository.save(subModule);
    }

    @Override
    @Transactional
    public SubModule updateSubModule(Long moduleId, Long subModuleId, SubModuleCreateRequest request) {
        if (subModuleId == null) {
            throw new com.defecttracker.exception.BadRequestException("SubModule ID is required to update a submodule");
        }
        SubModule subModule = getSubModuleById(subModuleId);
        subModule.setName(request.getName());
        subModule.setDescription(request.getDescription());
        return subModuleRepository.save(subModule);
    }

    @Override
    public SubModule getSubModuleById(Long subModuleId) {
        return subModuleRepository.findById(subModuleId)
                .orElseThrow(() -> new ResourceNotFoundException("SubModule", "id", subModuleId));
    }

    @Override
    public List<SubModule> getSubModulesByModuleId(Long moduleId) {
        return subModuleRepository.findByModuleId(moduleId);
    }

    @Override
    public List<SubModule> getSubModulesBulk(List<Long> moduleIds) {
        if (moduleIds == null || moduleIds.isEmpty()) {
            return subModuleRepository.findAll();
        }
        return subModuleRepository.findByModuleIdIn(moduleIds);
    }

    @Override
    @Transactional
    public void deleteSubModule(Long subModuleId) {
        SubModule subModule = getSubModuleById(subModuleId);
        subModuleRepository.delete(subModule);
    }

    @Override
    @Transactional
    public SubModuleDevAllocation assignDevToSubModule(Long subModuleId, Long employeeId) {
        SubModule subModule = getSubModuleById(subModuleId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        SubModuleDevAllocation allocation = subModuleDevAllocationRepository
                .findBySubModuleIdAndEmployeeId(subModuleId, employeeId)
                .orElse(SubModuleDevAllocation.builder().subModule(subModule).employee(employee).build());

        return subModuleDevAllocationRepository.save(allocation);
    }

    @Override
    @Transactional
    public void removeDevFromSubModule(Long subModuleId, Long employeeId) {
        subModuleDevAllocationRepository.deleteBySubModuleIdAndEmployeeId(subModuleId, employeeId);
    }

    @Override
    public List<SubModuleDevAllocation> getSubModuleDevs(Long subModuleId) {
        return subModuleDevAllocationRepository.findBySubModuleId(subModuleId);
    }
}
