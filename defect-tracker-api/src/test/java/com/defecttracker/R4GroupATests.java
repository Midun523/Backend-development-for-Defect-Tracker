package com.defecttracker;

import com.defecttracker.entity.EmailConfig;
import com.defecttracker.repository.EmailConfigRepository;
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

public class R4GroupATests extends AbstractIntegrationTest {

    @Autowired
    private EmailConfigRepository emailConfigRepository;

    @Test
    @DisplayName("B05: Employee JSON responses have no password or token fields")
    void B05_employeeJsonHasNoPasswordOrToken() throws Exception {
        String token = obtainAdminToken();

        // Check GET /api/v1/employee/1
        MvcResult res1 = mockMvc.perform(get("/api/v1/employee/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode empNode = objectMapper.readTree(res1.getResponse().getContentAsString());
        assertNoSensitiveKeysInTree(empNode);

        // Verify key frontend properties exist
        JsonNode data = empNode.get("data");
        assertThat(data.has("id")).isTrue();
        assertThat(data.has("firstName")).isTrue();
        assertThat(data.has("lastName")).isTrue();
        assertThat(data.has("email")).isTrue();
        assertThat(data.has("phone")).isTrue();
        assertThat(data.has("user")).isTrue();
        assertThat(data.get("user").has("userId")).isTrue();
        assertThat(data.has("designation")).isTrue();
        assertThat(data.has("joinedDate")).isTrue();
        assertThat(data.has("status")).isTrue();

        // Check GET /api/v1/employee
        MvcResult res2 = mockMvc.perform(get("/api/v1/employee")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertNoSensitiveKeysInTree(objectMapper.readTree(res2.getResponse().getContentAsString()));

        // Check GET /api/v1/bench
        MvcResult res3 = mockMvc.perform(get("/api/v1/bench")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertNoSensitiveKeysInTree(objectMapper.readTree(res3.getResponse().getContentAsString()));
    }

    @Test
    @DisplayName("FR-NOT-02: EmailConfig JSON responses have no password field, but include passwordSet")
    void FR_NOT_02_smtpConfigJsonHasNoPassword() throws Exception {
        String token = obtainAdminToken();

        MvcResult resList = mockMvc.perform(get("/api/v1/email/config")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode listNode = objectMapper.readTree(resList.getResponse().getContentAsString());
        assertNoSensitiveKeysInTree(listNode, Set.of("passwordSet"));

        MvcResult resGet = mockMvc.perform(get("/api/v1/email/config/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode getNode = objectMapper.readTree(resGet.getResponse().getContentAsString());
        assertNoSensitiveKeysInTree(getNode, Set.of("passwordSet"));
        assertThat(getNode.get("data").has("passwordSet")).isTrue();
        assertThat(getNode.get("data").get("passwordSet").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("FR-NOT-02: Updating SMTP config with blank/null password keeps existing stored password, while create requires it")
    void FR_NOT_02_updateWithBlankPasswordKeepsStoredPassword() throws Exception {
        String token = obtainAdminToken();

        EmailConfig original = emailConfigRepository.findById(1L).orElseThrow();
        String originalPassword = original.getPassword();
        assertThat(originalPassword).isNotBlank();

        // 1. Update with blank password
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("id", 1L);
        updatePayload.put("name", "Updated SMTP Name");
        updatePayload.put("smtpHost", "smtp.gmail.com");
        updatePayload.put("smtpPort", 587);
        updatePayload.put("username", "test@test.com");
        updatePayload.put("fromEmail", "test@test.com");
        updatePayload.put("fromName", "Test");
        updatePayload.put("password", "   "); // blank password

        mockMvc.perform(put("/api/v1/email/config/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk());

        EmailConfig afterUpdate = emailConfigRepository.findById(1L).orElseThrow();
        assertThat(afterUpdate.getPassword()).isEqualTo(originalPassword);
        assertThat(afterUpdate.getName()).isEqualTo("Updated SMTP Name");

        // 2. Create with blank password should fail with 400 Bad Request
        Map<String, Object> createPayload = new HashMap<>(updatePayload);
        createPayload.remove("id");
        createPayload.put("name", "New Invalid Config");
        createPayload.put("password", "");

        mockMvc.perform(post("/api/v1/email/config")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isBadRequest());
    }

    @Autowired
    private com.defecttracker.repository.ProjectRepository projectRepository;

    @Test
    @DisplayName("FR-PRJ-11: KLOC metric responses do not leak githubToken")
    void FR_PRJ_11_klocJsonHasNoGithubToken() throws Exception {
        String token = obtainAdminToken();

        com.defecttracker.entity.Project project = projectRepository.findById(1L).orElseGet(() ->
                projectRepository.save(com.defecttracker.entity.Project.builder()
                        .name("Test Project")
                        .projectId("PRJ001")
                        .status("ACTIVE")
                        .build())
        );

        // Save KLOC metric with githubToken
        Map<String, Object> klocPayload = Map.of(
                "backendRepoUrl", "https://github.com/org/backend",
                "frontendRepoUrl", "https://github.com/org/frontend",
                "githubToken", "ghp_secretTokenVal123",
                "githubUsername", "devuser",
                "calculatedKloc", 15.5
        );

        mockMvc.perform(post("/api/v1/kloc/project/" + project.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(klocPayload)))
                .andExpect(status().isOk());

        // Verify GET /api/v1/kloc/project/{projectId} does not leak githubToken
        MvcResult resGet = mockMvc.perform(get("/api/v1/kloc/project/" + project.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode getNode = objectMapper.readTree(resGet.getResponse().getContentAsString());
        assertNoSensitiveKeysInTree(getNode);

        // Verify GET /api/v1/kloc does not leak githubToken
        MvcResult resAll = mockMvc.perform(get("/api/v1/kloc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode allNode = objectMapper.readTree(resAll.getResponse().getContentAsString());
        assertNoSensitiveKeysInTree(allNode);
    }

    @Autowired
    private com.defecttracker.repository.KlocMetricRepository klocMetricRepository;

    @Test
    @DisplayName("FR-PRJ-11: KlocMetricRequest accepts both backendRepo/frontendRepo aliases and backendRepoUrl/frontendRepoUrl")
    void FR_PRJ_11_klocMetricRequestJsonAliasesWork() throws Exception {
        String token = obtainAdminToken();

        com.defecttracker.entity.Project project = projectRepository.findById(1L).orElseGet(() ->
                projectRepository.save(com.defecttracker.entity.Project.builder()
                        .name("Test Project For Aliases")
                        .projectId("PRJ_ALIAS_1")
                        .status("ACTIVE")
                        .build())
        );

        // 1. Post using backendRepo and frontendRepo (aliases)
        Map<String, Object> aliasPayload = Map.of(
                "backendRepo", "https://github.com/org/alias-backend",
                "frontendRepo", "https://github.com/org/alias-frontend",
                "githubToken", "ghp_aliasToken1",
                "githubUsername", "aliasUser",
                "calculatedKloc", 8.2
        );

        MvcResult aliasRes = mockMvc.perform(post("/api/v1/kloc/project/" + project.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aliasPayload)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode aliasNode = objectMapper.readTree(aliasRes.getResponse().getContentAsString());
        assertThat(aliasNode.path("data").path("backendRepoUrl").asText()).isEqualTo("https://github.com/org/alias-backend");
        assertThat(aliasNode.path("data").path("frontendRepoUrl").asText()).isEqualTo("https://github.com/org/alias-frontend");

        com.defecttracker.entity.KlocMetric aliasSaved = klocMetricRepository
                .findFirstByProjectIdOrderByCreatedAtDesc(project.getId())
                .orElseThrow();
        assertThat(aliasSaved.getBackendRepoUrl()).isEqualTo("https://github.com/org/alias-backend");
        assertThat(aliasSaved.getFrontendRepoUrl()).isEqualTo("https://github.com/org/alias-frontend");

        // 2. Post using backendRepoUrl and frontendRepoUrl (standard names)
        Map<String, Object> standardPayload = Map.of(
                "backendRepoUrl", "https://github.com/org/standard-backend",
                "frontendRepoUrl", "https://github.com/org/standard-frontend",
                "githubToken", "ghp_standardToken2",
                "githubUsername", "standardUser",
                "calculatedKloc", 9.4
        );

        MvcResult standardRes = mockMvc.perform(post("/api/v1/kloc/project/" + project.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardPayload)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode standardNode = objectMapper.readTree(standardRes.getResponse().getContentAsString());
        assertThat(standardNode.path("data").path("backendRepoUrl").asText()).isEqualTo("https://github.com/org/standard-backend");
        assertThat(standardNode.path("data").path("frontendRepoUrl").asText()).isEqualTo("https://github.com/org/standard-frontend");

        com.defecttracker.entity.KlocMetric standardSaved = klocMetricRepository
                .findFirstByProjectIdOrderByCreatedAtDesc(project.getId())
                .orElseThrow();
        assertThat(standardSaved.getBackendRepoUrl()).isEqualTo("https://github.com/org/standard-backend");
        assertThat(standardSaved.getFrontendRepoUrl()).isEqualTo("https://github.com/org/standard-frontend");
    }

    @Test
    @DisplayName("Group (a) Golden Schema Comparison: field names and types match golden fixtures")
    void groupAGoldenSchemaComparison() throws Exception {
        String token = obtainAdminToken();

        Map<String, String> endpoints = Map.ofEntries(
                Map.entry("/api/v1/employee?page=0&size=10", "golden/employee_list.json"),
                Map.entry("/api/v1/employee/1", "golden/employee_get.json"),
                Map.entry("/api/v1/bench", "golden/bench_list.json"),
                Map.entry("/api/v1/role", "golden/role_list.json"),
                Map.entry("/api/v1/role/1", "golden/role_get.json"),
                Map.entry("/api/v1/assign-permission/matrix", "golden/role_matrix.json"),
                Map.entry("/api/v1/permission", "golden/permission_list.json"),
                Map.entry("/api/v1/permission/1", "golden/permission_get.json"),
                Map.entry("/api/v1/email/config", "golden/email_config_list.json"),
                Map.entry("/api/v1/email/config/1", "golden/email_config_get.json"),
                Map.entry("/api/v1/email/template", "golden/email_template_list.json"),
                Map.entry("/api/v1/email/template/1", "golden/email_template_get.json"),
                Map.entry("/api/v1/kloc", "golden/kloc_all.json")
        );

        for (Map.Entry<String, String> entry : endpoints.entrySet()) {
            String url = entry.getKey();
            String goldenPath = entry.getValue();

            MvcResult result = mockMvc.perform(get(url)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode actualJson = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode goldenJson;
            try (InputStream is = new ClassPathResource(goldenPath).getInputStream()) {
                goldenJson = objectMapper.readTree(is);
            }

            // Compare field schema recursively (names and types)
            assertSchemaMatches(goldenJson, actualJson, url);
        }
    }

    private void assertSchemaMatches(JsonNode expected, JsonNode actual, String context) {
        if (expected == null || expected.isNull()) {
            return;
        }

        if (expected.isObject()) {
            assertThat(actual.isObject())
                    .withFailMessage("Expected object at %s but was %s", context, actual.getNodeType())
                    .isTrue();

            Iterator<String> fieldNames = expected.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                assertThat(actual.has(field))
                        .withFailMessage("Missing field '%s' in response at %s", field, context)
                        .isTrue();

                JsonNode expectedVal = expected.get(field);
                JsonNode actualVal = actual.get(field);

                if (!expectedVal.isNull() && !actualVal.isNull()) {
                    assertSchemaMatches(expectedVal, actualVal, context + "." + field);
                }
            }
        } else if (expected.isArray()) {
            assertThat(actual.isArray())
                    .withFailMessage("Expected array at %s but was %s", context, actual.getNodeType())
                    .isTrue();

            if (!expected.isEmpty() && !actual.isEmpty()) {
                assertSchemaMatches(expected.get(0), actual.get(0), context + "[0]");
            }
        }
    }

    private void assertNoSensitiveKeysInTree(JsonNode node) {
        assertNoSensitiveKeysInTree(node, Collections.emptySet());
    }

    private void assertNoSensitiveKeysInTree(JsonNode node, Set<String> allowedKeys) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                if (!allowedKeys.contains(key)) {
                    assertThat(key.toLowerCase())
                            .withFailMessage("Found sensitive key '%s' in response!", key)
                            .doesNotMatch(".*(password|token|secret).*");
                }
                assertNoSensitiveKeysInTree(entry.getValue(), allowedKeys);
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                assertNoSensitiveKeysInTree(element, allowedKeys);
            }
        }
    }
}
