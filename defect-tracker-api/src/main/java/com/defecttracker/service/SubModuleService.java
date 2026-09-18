package com.defecttracker.service;

import com.defecttracker.dto.request.SubModuleCreateRequest;
import com.defecttracker.entity.SubModule;
import com.defecttracker.entity.SubModuleDevAllocation;

import java.util.List;

public interface SubModuleService {
    SubModule createSubModule(Long moduleId, SubModuleCreateRequest request);
    SubModule updateSubModule(Long moduleId, Long subModuleId, SubModuleCreateRequest request);
    SubModule getSubModuleById(Long subModuleId);
    List<SubModule> getSubModulesByModuleId(Long moduleId);
    List<SubModule> getSubModulesBulk(List<Long> moduleIds);
    void deleteSubModule(Long subModuleId);

    SubModuleDevAllocation assignDevToSubModule(Long subModuleId, Long employeeId);
    void removeDevFromSubModule(Long subModuleId, Long employeeId);
    List<SubModuleDevAllocation> getSubModuleDevs(Long subModuleId);
}
