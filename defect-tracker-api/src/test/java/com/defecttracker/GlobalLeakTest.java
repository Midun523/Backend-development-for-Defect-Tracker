package com.defecttracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalLeakTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFixture testDataFixture;

    private static final Pattern LEAK_PATTERN = Pattern.compile("(?i).*(password|token|secret).*");

    // Explicit, commented allow-list for GET endpoints that legitimately return non-200 in this test context
    private static final Set<String> ALLOWED_NON_200_ENDPOINTS = Set.of(
            // Currently empty: all registered application GET endpoints must return HTTP 200 with valid fixture data.
            // If any endpoint legitimately returns a non-200 (e.g. 204 No Content), document and add here.
    );

    private String getAdminToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin@defecttracker.com",
                                "password", TEST_ADMIN_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return node.get("data").get("token").asText();
    }

    private void assertNoSensitiveKeys(String path, JsonNode node, AtomicInteger objectsInspected) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return;
        }
        if (node.isObject()) {
            objectsInspected.incrementAndGet();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                if (LEAK_PATTERN.matcher(key).matches()) {
                    if (!"passwordSet".equalsIgnoreCase(key)) {
                        throw new AssertionError("Leaked sensitive key '" + key + "' found at endpoint: " + path + " in node: " + node);
                    }
                }
                assertNoSensitiveKeys(path, entry.getValue(), objectsInspected);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                assertNoSensitiveKeys(path, child, objectsInspected);
            }
        }
    }

    private String resolvePathVariables(String path, TestDataFixture.FixtureIds ids) {
        String resolved = path
                .replace("{projectId}", String.valueOf(ids.projectId()))
                .replace("{releaseId}", String.valueOf(ids.releaseId()))
                .replace("{employeeId}", String.valueOf(ids.devEmployeeId()))
                .replace("{roleId}", String.valueOf(ids.roleId()))
                .replace("{permissionId}", String.valueOf(ids.permissionId()))
                .replace("{designationId}", String.valueOf(ids.designationId()))
                .replace("{moduleId}", String.valueOf(ids.moduleId()))
                .replace("{subModuleId}", String.valueOf(ids.subModuleId()))
                .replace("{testcaseId}", String.valueOf(ids.testCaseId()))
                .replace("{templateId}", String.valueOf(ids.emailTemplateId()))
                .replace("{defectId}", String.valueOf(ids.defectId()))
                .replace("{allocateModuleId}", String.valueOf(ids.moduleId()))
                .replace("{userId}", String.valueOf(ids.userId()));

        if (resolved.contains("{id}")) {
            long targetId;
            if (path.contains("/sub-module/") && path.contains("/test-case/")) {
                targetId = ids.testCaseId();
            } else if (path.contains("/release/") && path.contains("/test-case/")) {
                targetId = ids.testCaseId();
            } else if (path.contains("/project/") && path.contains("/module/")) {
                targetId = ids.moduleId();
            } else if (path.contains("/module/") && path.contains("/sub-module/")) {
                targetId = ids.subModuleId();
            } else if (path.startsWith("/api/v1/client/")) {
                targetId = ids.clientId();
            } else if (path.startsWith("/api/v1/employee/")) {
                targetId = ids.devEmployeeId();
            } else if (path.startsWith("/api/v1/role/")) {
                targetId = ids.roleId();
            } else if (path.startsWith("/api/v1/permission/")) {
                targetId = ids.permissionId();
            } else if (path.startsWith("/api/v1/designation/")) {
                targetId = ids.designationId();
            } else if (path.startsWith("/api/v1/email/config/")) {
                targetId = ids.emailConfigId();
            } else if (path.startsWith("/api/v1/email/template/")) {
                targetId = ids.emailTemplateId();
            } else if (path.startsWith("/api/v1/release/")) {
                targetId = ids.releaseId();
            } else if (path.startsWith("/api/v1/defect/")) {
                targetId = ids.defectId();
            } else if (path.startsWith("/api/v1/project/")) {
                targetId = ids.projectId();
            } else {
                targetId = ids.projectId();
            }
            resolved = resolved.replace("{id}", String.valueOf(targetId));
        }

        return resolved;
    }

    @Test
    @DisplayName("Global Leak Test: Call every GET endpoint from /v3/api-docs and assert no sensitive keys in responses")
    void globalLeakTest_noSensitiveKeysInAnyGetEndpoint() throws Exception {
        // Seed reusable test fixture
        TestDataFixture.FixtureIds fixture = testDataFixture.seedFixtureData();
        String token = getAdminToken();

        MvcResult docsResult = mockMvc.perform(get("/v3/api-docs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode docsJson = objectMapper.readTree(docsResult.getResponse().getContentAsString());
        JsonNode paths = docsJson.path("paths");
        assertThat(paths.isObject()).isTrue();

        Iterator<String> pathNames = paths.fieldNames();
        int testedEndpoints = 0;
        AtomicInteger nestedObjectsInspected = new AtomicInteger(0);
        List<String> failedEndpoints = new ArrayList<>();

        while (pathNames.hasNext()) {
            String path = pathNames.next();
            JsonNode pathItem = paths.get(path);

            if (pathItem.has("get")) {
                // Whitelist /auth/** endpoints as instructed
                if (path.contains("/auth/")) {
                    continue;
                }
                // Exclude /test-only/** diagnostic endpoints (only exist in test configuration)
                if (path.startsWith("/test-only/")) {
                    continue;
                }

                String resolvedPath = resolvePathVariables(path, fixture);

                MvcResult result = mockMvc.perform(get(resolvedPath)
                                .header("Authorization", "Bearer " + token))
                        .andReturn();

                int status = result.getResponse().getStatus();
                if (status >= 200 && status < 300) {
                    String content = result.getResponse().getContentAsString();
                    if (content != null && !content.isBlank() && content.startsWith("{")) {
                        JsonNode responseNode = objectMapper.readTree(content);
                        assertNoSensitiveKeys(path, responseNode, nestedObjectsInspected);
                        testedEndpoints++;
                    }
                } else {
                    if (!ALLOWED_NON_200_ENDPOINTS.contains(path)) {
                        failedEndpoints.add(String.format("Endpoint %s (called: %s) returned HTTP %d: %s",
                                path, resolvedPath, status, result.getResponse().getContentAsString()));
                    }
                }
            }
        }

        System.out.println("=================================================");
        System.out.println("GLOBAL LEAK TEST SUMMARY:");
        System.out.println("Endpoints checked: " + testedEndpoints);
        System.out.println("Nested objects inspected: " + nestedObjectsInspected.get());
        System.out.println("Failed non-200 endpoints: " + failedEndpoints.size());
        System.out.println("=================================================");

        if (!failedEndpoints.isEmpty()) {
            StringBuilder sb = new StringBuilder("GlobalLeakTest failed because endpoints returned non-200:\n");
            for (String f : failedEndpoints) {
                sb.append(" - ").append(f).append("\n");
            }
            throw new AssertionError(sb.toString());
        }

        assertThat(testedEndpoints).isGreaterThan(50);
        assertThat(nestedObjectsInspected.get()).isGreaterThan(100);
    }
}
