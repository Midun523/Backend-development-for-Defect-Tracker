package com.defecttracker.mapper;

import com.defecttracker.dto.response.*;
import com.defecttracker.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EmployeeMapper.class})
public interface TestCaseMapper {

    @Mapping(target = "no", source = "testcaseNo")
    @Mapping(target = "steps", source = "detailsSteps")
    @Mapping(target = "subModuleId", expression = "java(testCase.getSubModule() != null ? testCase.getSubModule().getId() : null)")
    @Mapping(target = "subModuleName", expression = "java(testCase.getSubModule() != null ? testCase.getSubModule().getName() : null)")
    @Mapping(target = "moduleId", expression = "java(testCase.getSubModule() != null && testCase.getSubModule().getModule() != null ? testCase.getSubModule().getModule().getId() : null)")
    @Mapping(target = "moduleName", expression = "java(testCase.getSubModule() != null && testCase.getSubModule().getModule() != null ? testCase.getSubModule().getModule().getName() : null)")
    @Mapping(target = "projectId", expression = "java(testCase.getSubModule() != null && testCase.getSubModule().getModule() != null && testCase.getSubModule().getModule().getProject() != null ? testCase.getSubModule().getModule().getProject().getId() : null)")
    @Mapping(target = "severityId", expression = "java(testCase.getSeverity() != null ? testCase.getSeverity().getId() : null)")
    @Mapping(target = "severityName", expression = "java(testCase.getSeverity() != null ? testCase.getSeverity().getName() : null)")
    @Mapping(target = "defectTypeId", expression = "java(testCase.getDefectType() != null ? testCase.getDefectType().getId() : null)")
    @Mapping(target = "defectTypeName", expression = "java(testCase.getDefectType() != null ? (testCase.getDefectType().getDefectTypeName() != null ? testCase.getDefectType().getDefectTypeName() : testCase.getDefectType().getName()) : null)")
    TestCaseSummary toTestCaseSummary(TestCase testCase);

    List<TestCaseSummary> toTestCaseSummaryList(List<TestCase> testCases);
}
