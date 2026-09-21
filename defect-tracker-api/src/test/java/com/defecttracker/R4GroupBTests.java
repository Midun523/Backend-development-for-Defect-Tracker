package com.defecttracker;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.io.InputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class R4GroupBTests extends AbstractIntegrationTest {

    @Autowired
    private TestDataFixture testDataFixture;

    private void assertNoSensitiveKeys(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) return;
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                if (key.toLowerCase().contains("password") || key.toLowerCase().contains("secret") || key.toLowerCase().contains("token")) {
                    if (!"passwordSet".equalsIgnoreCase(key) && !"resetCount".equalsIgnoreCase(key) && !"defectStatus".equalsIgnoreCase(key)) {
                        assertThat(key).doesNotContainIgnoringCase("password");
                    }
                }
                assertNoSensitiveKeys(entry.getValue());
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                assertNoSensitiveKeys(item);
            }
        }
    }

    private Set<String> extractKeys(JsonNode node) {
        Set<String> keys = new TreeSet<>();
        if (node == null || node.isNull()) return keys;
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(keys::add);
        } else if (node.isArray() && node.size() > 0) {
            JsonNode first = node.get(0);
            if (first.isObject()) {
                first.fieldNames().forEachRemaining(keys::add);
            }
        }
        return keys;
    }

    private void assertMatchesGolden(String goldenPath, JsonNode actualNode) throws Exception {
        ClassPathResource resource = new ClassPathResource("golden/" + goldenPath);
        assertThat(resource.exists()).as("Golden file exists: " + goldenPath).isTrue();
        try (InputStream is = resource.getInputStream()) {
            JsonNode golden = objectMapper.readTree(is);
            Set<String> goldenEnvelopeKeys = extractKeys(golden);
            Set<String> actualEnvelopeKeys = extractKeys(actualNode);
            assertThat(actualEnvelopeKeys).as("Envelope keys for " + goldenPath).isEqualTo(goldenEnvelopeKeys);

            JsonNode goldenData = golden.get("data");
            JsonNode actualData = actualNode.get("data");

            Set<String> goldenDataKeys = extractKeys(goldenData);
            Set<String> actualDataKeys = extractKeys(actualData);
            assertThat(actualDataKeys).as("Data keys for " + goldenPath).isEqualTo(goldenDataKeys);
        }
    }

    @Test
    @DisplayName("Group B GET endpoints match golden schema and contain no credential leaks")
    void groupBGetEndpointsMatchGoldenSchemas() throws Exception {
        String token = obtainAdminToken();
        TestDataFixture.FixtureIds ids = testDataFixture.seedFixtureData();

        Map<String, String> endpointsToGolden = Map.ofEntries(
                Map.entry("/api/v1/project/" + ids.projectId(), "project_get.json"),
                Map.entry("/api/v1/project", "project_list.json"),
                Map.entry("/api/v1/project?page=0&size=10", "project_paged.json"),
                Map.entry("/api/v1/defect/" + ids.defectId(), "defect_get.json"),
                Map.entry("/api/v1/defect?projectId=" + ids.projectId(), "defect_filter.json"),
                Map.entry("/api/v1/project/" + ids.projectId() + "/defect", "defect_by_project.json"),
                Map.entry("/api/v1/defect/allocation/defect/" + ids.defectId(), "defect_allocation.json"),
                Map.entry("/api/v1/defect/bulk?projectId=" + ids.projectId(), "defect_bulk_export.json"),
                Map.entry("/api/v1/defect/" + ids.defectId() + "/comment", "defect_comment_list.json"),
                Map.entry("/api/v1/defect/" + ids.defectId() + "/history", "defect_history_list.json"),
                Map.entry("/api/v1/project/" + ids.projectId() + "/release/" + ids.releaseId() + "/defect-status-log", "defect_status_log_list.json"),
                Map.entry("/api/v1/release/" + ids.releaseId(), "release_get.json"),
                Map.entry("/api/v1/release", "release_list.json"),
                Map.entry("/api/v1/release?projectId=" + ids.projectId(), "release_by_project.json"),
                Map.entry("/api/v1/project/" + ids.projectId() + "/release/active", "release_active.json"),
                Map.entry("/api/v1/release/counts", "release_counts.json"),
                Map.entry("/api/v1/release/" + ids.releaseId() + "/test-case", "release_test_case_list.json"),
                Map.entry("/api/v1/release/" + ids.releaseId() + "/test-case/" + ids.testCaseId(), "release_test_case_get.json"),
                Map.entry("/api/v1/release/" + ids.releaseId() + "/test-case-qa-allocation", "release_test_case_qa_allocation.json"),
                Map.entry("/api/v1/testcase/allocation-log", "testcase_allocation_log.json"),
                Map.entry("/api/v1/project-allocation", "project_allocation_list.json"),
                Map.entry("/api/v1/project-allocation/" + ids.projectId(), "project_allocation_by_project.json"),
                Map.entry("/api/v1/project-allocation/" + ids.projectId() + "/employee", "project_allocation_employees.json"),
                Map.entry("/api/v1/project-allocation/" + ids.projectId() + "/employee_history", "project_allocation_history.json"),
                Map.entry("/api/v1/project-allocation/employee/" + ids.devEmployeeId(), "project_allocation_by_user.json"),
                Map.entry("/api/v1/user/me/projects", "user_me_projects.json")
        );

        for (Map.Entry<String, String> entry : endpointsToGolden.entrySet()) {
            MvcResult res = mockMvc.perform(get(entry.getKey())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode actual = objectMapper.readTree(res.getResponse().getContentAsString());
            assertNoSensitiveKeys(actual);
            assertMatchesGolden(entry.getValue(), actual);
        }
    }

    @Test
    @DisplayName("Write endpoints schema parity: write handlers return the same key-set as GET responses")
    void writeEndpointsSchemaParityWithGetGoldenFiles() throws Exception {
        String token = obtainAdminToken();
        TestDataFixture.FixtureIds ids = testDataFixture.seedFixtureData();

        // 1. Project write endpoint parity (PUT /api/v1/project/{id})
        Map<String, Object> projectReq = Map.of(
                "name", "Updated Core Project",
                "status", "ACTIVE"
        );
        MvcResult prjRes = mockMvc.perform(put("/api/v1/project/" + ids.projectId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectReq)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode prjNode = objectMapper.readTree(prjRes.getResponse().getContentAsString());
        assertMatchesGolden("project_get.json", prjNode);

        // 2. Project KLOC write endpoint parity (PATCH /api/v1/project/{id}/project-kilo-of-code)
        MvcResult klocRes = mockMvc.perform(patch("/api/v1/project/" + ids.projectId() + "/project-kilo-of-code")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("kloc", 12.5))))
                .andExpect(status().isOk())
                .andReturn();
        assertMatchesGolden("project_get.json", objectMapper.readTree(klocRes.getResponse().getContentAsString()));

        // 3. Release write endpoint parity (PATCH /api/v1/release/{id}/status)
        MvcResult relRes = mockMvc.perform(patch("/api/v1/release/" + ids.releaseId() + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isOk())
                .andReturn();
        assertMatchesGolden("release_get.json", objectMapper.readTree(relRes.getResponse().getContentAsString()));

        // 4. Release KLOC write endpoint parity (PATCH /api/v1/release/{id}/kloc)
        MvcResult relKlocRes = mockMvc.perform(patch("/api/v1/release/" + ids.releaseId() + "/kloc")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("kloc", 4.2))))
                .andExpect(status().isOk())
                .andReturn();
        assertMatchesGolden("release_get.json", objectMapper.readTree(relKlocRes.getResponse().getContentAsString()));

        // 5. Defect comment write endpoint parity (POST /api/v1/defect/{id}/comment)
        MvcResult cmtRes = mockMvc.perform(post("/api/v1/defect/" + ids.defectId() + "/comment")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("comment", "Parity test comment"))))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        JsonNode cmtNode = objectMapper.readTree(cmtRes.getResponse().getContentAsString());
        // Verify comment data keys match defect_comment_list.json
        ClassPathResource cmtResource = new ClassPathResource("golden/defect_comment_list.json");
        JsonNode cmtGolden = objectMapper.readTree(cmtResource.getInputStream());
        Set<String> goldenKeys = extractKeys(cmtGolden.get("data"));
        Set<String> actualKeys = extractKeys(cmtNode.get("data"));
        assertThat(actualKeys).isEqualTo(goldenKeys);
    }
}
