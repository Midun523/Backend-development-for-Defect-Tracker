package com.defecttracker.mapper;

import com.defecttracker.dto.response.*;
import com.defecttracker.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, EmployeeMapper.class})
public interface ModuleMapper {

    @Mapping(target = "moduleName", source = "name")
    @Mapping(target = "leaderId", expression = "java(module.getLeader() != null ? module.getLeader().getId() : null)")
    @Mapping(target = "projectId", expression = "java(module.getProject() != null ? module.getProject().getId() : null)")
    ModuleSummary toModuleSummary(com.defecttracker.entity.Module module);
    List<ModuleSummary> toModuleSummaryList(List<com.defecttracker.entity.Module> modules);

    @Mapping(target = "moduleId", expression = "java(subModule.getModule() != null ? subModule.getModule().getId() : null)")
    @Mapping(target = "submoduleName", source = "name")
    @Mapping(target = "subModuleName", source = "name")
    SubModuleSummary toSubModuleSummary(SubModule subModule);
    List<SubModuleSummary> toSubModuleSummaryList(List<SubModule> subModules);

    SubModuleDevAllocationResponse toSubModuleDevAllocationResponse(SubModuleDevAllocation allocation);
    List<SubModuleDevAllocationResponse> toSubModuleDevAllocationResponseList(List<SubModuleDevAllocation> allocations);

    ModuleLeaderAllocationResponse toModuleLeaderAllocationResponse(ModuleLeaderAllocation allocation);
}
