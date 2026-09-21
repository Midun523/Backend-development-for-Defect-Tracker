package com.defecttracker.mapper;

import com.defecttracker.dto.response.*;
import com.defecttracker.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, EmployeeMapper.class})
public abstract class ReleaseMapper {

    @Autowired
    @Lazy
    protected DefectMapper defectMapper;

    @Named("defectToDefectResponse")
    protected DefectResponse defectToDefectResponse(Defect defect) {
        return defectMapper != null ? defectMapper.toResponse(defect) : null;
    }

    @Named("defectTypeToSummary")
    @Mapping(target = "defectTypeName", expression = "java(defectType != null ? (defectType.getDefectTypeName() != null ? defectType.getDefectTypeName() : defectType.getName()) : null)")
    public abstract DefectTypeSummary toTypeSummary(DefectType defectType);

    @Mapping(target = "releaseId", source = "id")
    @Mapping(target = "releaseName", source = "name")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "releaseTypeId", source = "releaseType.id")
    @Mapping(target = "releaseTypeName", source = "releaseType.name")
    public abstract ReleaseResponse toResponse(Release release);

    public abstract List<ReleaseResponse> toResponseList(List<Release> releases);

    public abstract ReleaseTypeSummary toReleaseTypeSummary(ReleaseType releaseType);

    @Mapping(target = "releaseId", source = "id")
    @Mapping(target = "releaseName", source = "name")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "releaseTypeId", source = "releaseType.id")
    @Mapping(target = "releaseTypeName", source = "releaseType.name")
    public abstract ReleaseSummary toSummary(Release release);

    @Mapping(target = "no", source = "testcaseNo")
    @Mapping(target = "steps", source = "detailsSteps")
    @Mapping(target = "subModuleId", source = "subModule.id")
    @Mapping(target = "subModuleName", source = "subModule.name")
    @Mapping(target = "moduleId", source = "subModule.module.id")
    @Mapping(target = "moduleName", source = "subModule.module.name")
    @Mapping(target = "projectId", source = "subModule.module.project.id")
    @Mapping(target = "severityId", source = "severity.id")
    @Mapping(target = "severityName", source = "severity.name")
    @Mapping(target = "defectTypeId", source = "defectType.id")
    @Mapping(target = "defectTypeName", expression = "java(testCase.getDefectType() != null ? (testCase.getDefectType().getDefectTypeName() != null ? testCase.getDefectType().getDefectTypeName() : testCase.getDefectType().getName()) : null)")
    @Mapping(target = "defectType", qualifiedByName = "defectTypeToSummary")
    public abstract TestCaseSummary toTestCaseSummary(TestCase testCase);

    @Mapping(target = "moduleId", source = "module.id")
    @Mapping(target = "submoduleName", source = "name")
    @Mapping(target = "subModuleName", source = "name")
    public abstract SubModuleSummary toSubModuleSummary(SubModule subModule);

    @Mapping(target = "defectNo", expression = "java(rtc.getDefectNo())")
    @Mapping(target = "assignedTo", expression = "java(rtc.getAssignedToName())")
    @Mapping(target = "priorityName", expression = "java(rtc.getPriorityName())")
    @Mapping(target = "testcaseNo", expression = "java(rtc.getTestcaseNo())")
    @Mapping(target = "description", expression = "java(rtc.getDescription())")
    @Mapping(target = "steps", expression = "java(rtc.getSteps())")
    @Mapping(target = "expectedResult", expression = "java(rtc.getExpectedResult())")
    @Mapping(target = "severityName", expression = "java(rtc.getSeverityName())")
    @Mapping(target = "defectTypeName", expression = "java(rtc.getDefectTypeName())")
    @Mapping(target = "subModuleName", expression = "java(rtc.getSubModuleName())")
    @Mapping(target = "moduleName", expression = "java(rtc.getModuleName())")
    @Mapping(target = "linkedDefect", qualifiedByName = "defectToDefectResponse")
    public abstract ReleaseTestCaseResponse toReleaseTestCaseResponse(ReleaseTestCase rtc);

    public abstract List<ReleaseTestCaseResponse> toReleaseTestCaseResponseList(List<ReleaseTestCase> list);

    public abstract TestCaseAllocationLogResponse toTestCaseAllocationLogResponse(TestCaseAllocationLog log);
    public abstract List<TestCaseAllocationLogResponse> toTestCaseAllocationLogResponseList(List<TestCaseAllocationLog> list);
}

