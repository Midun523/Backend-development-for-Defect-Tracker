package com.defecttracker;

import com.defecttracker.entity.Project;
import com.defecttracker.entity.Role;
import com.defecttracker.entity.User;
import com.defecttracker.repository.DefectRepository;
import com.defecttracker.repository.ProjectRepository;
import com.defecttracker.repository.RoleRepository;
import com.defecttracker.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CharacterizationTests extends AbstractIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("FR_AUTH_04: Calling an endpoint with an expired or garbage token must return HTTP 401 Unauthorized")
    void FR_AUTH_04_expiredOrGarbageTokenIs401() throws Exception {
        mockMvc.perform(get("/api/v1/employee")
                        .header("Authorization", "Bearer garbage.jwt.token.that.is.invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("FR_ORG_06: Authenticated user without required permission must receive HTTP 403 Forbidden")
    void FR_ORG_06_authenticatedUserWithoutPermissionIs403() throws Exception {
        // Create role with no permissions
        Role noPermRole = roleRepository.findByRoleName("ROLE_NO_PERM_CHAR")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("ROLE_NO_PERM_CHAR")
                        .description("Role with zero permissions for characterization test")
                        .build()));

        // Create user in this role
        String email = "noperm_char@defecttracker.com";
        String password = "CharPassword123!";
        userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .userId("NOPM_CHAR")
                        .firstName("NoPerm")
                        .lastName("Char")
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .userStatus("ACTIVE")
                        .userType("CompanyStaff")
                        .roles(Set.of(noPermRole))
                        .build())
        );

        // Login as this user
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("data").get("token").asText();

        // Call endpoint requiring EMPLOYEE_READ without having the permission
        mockMvc.perform(get("/api/v1/employee")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @Disabled("R6")
    @DisplayName("B08: Defect creation with non-existent projectId must be rejected, not silently attached to an existing project")
    void B08_defectWithUnknownProjectIsRejectedEvenWhenProjectsExist() throws Exception {
        String adminToken = obtainAdminToken();

        // 1. FIRST create one project
        Project project = projectRepository.save(Project.builder()
                .projectId("PRJ_B08")
                .name("Existing Project B08")
                .prefix("B08")
                .description("Project for B08 characterization test")
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .kloc(5.0)
                .build());

        // 2. THEN POST a defect with a non-existent projectId (999999L) and no module/release/subModule
        Map<String, Object> defectPayload = Map.of(
                "projectId", 999999L,
                "title", "Defect with invalid project ID",
                "description", "Should be rejected because project 999999 does not exist",
                "severityId", 1L,
                "priorityId", 1L,
                "defectTypeId", 1L,
                "statusId", 1L
        );

        // Expected: Should be rejected with 404 Not Found (or 400 Bad Request), NOT 200/201 attaching to PRJ_B08
        MvcResult result = mockMvc.perform(post("/api/v1/defect")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defectPayload)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @Disabled("R14")
    @DisplayName("FR_DEF_14: Importing defects without a file must return HTTP 400 Bad Request")
    void FR_DEF_14_importWithoutFileIs400() throws Exception {
        String adminToken = obtainAdminToken();

        mockMvc.perform(post("/api/v1/defect/import")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Disabled("R15")
    @DisplayName("FR_DASH: Time-to-find dashboard metric must differ between releases with different defect lifecycles")
    void FR_DASH_timeToFindDiffersBetweenReleases() throws Exception {
        String adminToken = obtainAdminToken();

        // Query time-to-find for two different releases
        MvcResult res1 = mockMvc.perform(get("/api/v1/project/1/release/1/dashboard/time-to-find")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult res2 = mockMvc.perform(get("/api/v1/project/1/release/2/dashboard/time-to-find")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json1 = objectMapper.readTree(res1.getResponse().getContentAsString());
        JsonNode json2 = objectMapper.readTree(res2.getResponse().getContentAsString());

        double days1 = json1.path("data").path("averageDaysToFind").asDouble();
        double days2 = json2.path("data").path("averageDaysToFind").asDouble();

        // Expected: Real analytics should not return the identical static stub (2.4) across different releases
        assertThat(days1).isNotEqualTo(days2);
    }
}
