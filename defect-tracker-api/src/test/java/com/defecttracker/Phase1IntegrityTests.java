package com.defecttracker;

import com.defecttracker.dto.request.DefectCreateRequest;
import com.defecttracker.entity.*;
import com.defecttracker.entity.Module;
import com.defecttracker.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class Phase1IntegrityTests extends AbstractIntegrationTest {

    @Autowired
    private TestDataFixture testDataFixture;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private SubModuleRepository subModuleRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectAllocationRepository projectAllocationRepository;

    @Autowired
    private SeverityRepository severityRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    @Test
    @DisplayName("FR_DEF_01: Defect creation rejects when projectId is missing and cannot be derived")
    void FR_DEF_01_createDefect_rejectsWhenNoProjectAndCannotDerive() throws Exception {
        String token = obtainAdminToken();

        Map<String, Object> body = Map.of(
                "title", "Defect with no project",
                "description", "Missing project cannot be derived"
        );

        mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("FR_DEF_01: Defect creation derives projectId when candidate modules agree")
    void FR_DEF_01_createDefect_derivesProjectWhenModuleAgrees() throws Exception {
        String token = obtainAdminToken();
        TestDataFixture.FixtureIds ids = testDataFixture.seedFixtureData();

        Map<String, Object> body = Map.of(
                "title", "Defect with derived project",
                "description", "Project derived from module",
                "moduleId", ids.moduleId()
        );

        MvcResult result = mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(root.get("data").get("project").get("id").asLong()).isEqualTo(ids.projectId());
    }

    @Test
    @DisplayName("FR_DEF_01: Defect creation rejects mismatch between project and module")
    void FR_DEF_01_createDefect_rejectsMismatchBetweenProjectAndModule() throws Exception {
        String token = obtainAdminToken();
        TestDataFixture.FixtureIds ids = testDataFixture.seedFixtureData();

        Project otherProject = projectRepository.save(Project.builder()
                .projectId("PRJ_OTHER_01")
                .name("Other Project 01")
                .prefix("OTH")
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .build());

        Map<String, Object> body = Map.of(
                "title", "Defect with mismatching project/module",
                "description", "Project mismatch",
                "projectId", otherProject.getId(),
                "moduleId", ids.moduleId()
        );

        mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("FR_DEF_01: Defect creation rejects subModule not belonging to module")
    void FR_DEF_01_createDefect_rejectsSubModuleNotInModule() throws Exception {
        String token = obtainAdminToken();
        TestDataFixture.FixtureIds ids = testDataFixture.seedFixtureData();

        Module otherModule = moduleRepository.save(Module.builder()
                .name("Other Module 01")
                .project(projectRepository.findById(ids.projectId()).get())
                .build());

        SubModule foreignSubModule = subModuleRepository.save(SubModule.builder()
                .name("Foreign Submodule")
                .module(otherModule)
                .build());

        Map<String, Object> body = Map.of(
                "title", "Defect with foreign submodule",
                "description", "Submodule not in module",
                "projectId", ids.projectId(),
                "moduleId", ids.moduleId(),
                "subModuleId", foreignSubModule.getId()
        );

        mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("FR_DEF_01: Defect creation rejects release not in project")
    void FR_DEF_01_createDefect_rejectsReleaseNotInProject() throws Exception {
        String token = obtainAdminToken();
        TestDataFixture.FixtureIds ids = testDataFixture.seedFixtureData();

        Project otherProject = projectRepository.save(Project.builder()
                .projectId("PRJ_OTHER_02")
                .name("Other Project 02")
                .prefix("OT2")
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .build());

        Release foreignRelease = releaseRepository.save(Release.builder()
                .releaseNo("REL_FOREIGN")
                .name("Foreign Release")
                .version("v1.0")
                .project(otherProject)
                .status("PLANNED")
                .startDate(LocalDate.now())
                .build());

        Map<String, Object> body = Map.of(
                "title", "Defect with foreign release",
                "description", "Release not in project",
                "projectId", ids.projectId(),
                "releaseId", foreignRelease.getId()
        );

        mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("FR_DEF_01: Defect creation rejects testCase not in project")
    void FR_DEF_01_createDefect_rejectsTestCaseNotInProject() throws Exception {
        String token = obtainAdminToken();
        TestDataFixture.FixtureIds ids = testDataFixture.seedFixtureData();

        Project otherProject = projectRepository.save(Project.builder()
                .projectId("PRJ_OTHER_03")
                .name("Other Project 03")
                .prefix("OT3")
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .build());

        Module foreignModule = moduleRepository.save(Module.builder()
                .name("Foreign Module")
                .project(otherProject)
                .build());

        SubModule foreignSub = subModuleRepository.save(SubModule.builder()
                .name("Foreign Sub")
                .module(foreignModule)
                .build());

        TestCase foreignTc = testCaseRepository.save(TestCase.builder()
                .testcaseNo("TC_FOREIGN")
                .description("Foreign test case")
                .subModule(foreignSub)
                .executionStatus("NOT_RUN")
                .build());

        Map<String, Object> body = Map.of(
                "title", "Defect with foreign test case",
                "description", "Test case not in project",
                "projectId", ids.projectId(),
                "testCaseId", foreignTc.getId()
        );

        mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("FR_DEF_01: Defect creation rejects assignee not ACTIVE and allocated to project")
    void FR_DEF_01_createDefect_rejectsAssigneeNotAllocatedToProject() throws Exception {
        String token = obtainAdminToken();
        TestDataFixture.FixtureIds ids = testDataFixture.seedFixtureData();

        Employee unallocatedEmployee = employeeRepository.save(Employee.builder()
                .firstName("Unallocated")
                .lastName("Emp")
                .email("unallocated@defecttracker.com")
                .status("active")
                .availability(100)
                .build());

        Map<String, Object> body = Map.of(
                "title", "Defect with unallocated assignee",
                "description", "Assignee not allocated",
                "projectId", ids.projectId(),
                "assignedTo", unallocatedEmployee.getId()
        );

        mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("FR_DEF_01: Defect sequences are per-project")
    void FR_DEF_01_perProjectSequences_distinctPerProject() throws Exception {
        String token = obtainAdminToken();

        Project p1 = projectRepository.save(Project.builder()
                .projectId("PRJ_SEQ1")
                .name("Seq Project 1")
                .prefix("SQ1")
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .build());

        Project p2 = projectRepository.save(Project.builder()
                .projectId("PRJ_SEQ2")
                .name("Seq Project 2")
                .prefix("SQ2")
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .build());

        Map<String, Object> d1 = Map.of(
                "title", "Defect in P1",
                "description", "First defect in P1",
                "projectId", p1.getId()
        );

        Map<String, Object> d2 = Map.of(
                "title", "Defect in P2",
                "description", "First defect in P2",
                "projectId", p2.getId()
        );

        MvcResult r1 = mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(d1)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult r2 = mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(d2)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode n1 = objectMapper.readTree(r1.getResponse().getContentAsString());
        JsonNode n2 = objectMapper.readTree(r2.getResponse().getContentAsString());

        assertThat(n1.get("data").get("defectId").asText()).isEqualTo("DEF001");
        assertThat(n2.get("data").get("defectId").asText()).isEqualTo("DEF001");
    }

    @Test
    @DisplayName("FR_DEF_01: Pageable helper caps request size at 100")
    void FR_DEF_01_pageableCapsAt100() throws Exception {
        String token = obtainAdminToken();
        testDataFixture.seedFixtureData();

        MvcResult res = mockMvc.perform(get("/api/v1/defect?size=500")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString());
        int pageSize = root.get("data").get("pageSize").asInt();
        assertThat(pageSize).isLessThanOrEqualTo(100);
    }
}
