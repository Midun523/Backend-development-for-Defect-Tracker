package com.defecttracker;

import com.defecttracker.entity.Role;
import com.defecttracker.entity.User;
import com.defecttracker.repository.RoleRepository;
import com.defecttracker.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(R2StatusCodesTests.TestOnlyControllerConfig.class)
public class R2StatusCodesTests extends AbstractIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @TestConfiguration
    static class TestOnlyControllerConfig {

        @RestController
        static class TestOnlyController {

            @Autowired
            private RoleRepository roleRepository;

            @GetMapping("/test-only/unhandled-error")
            public void triggerUnhandledError() {
                throw new RuntimeException("Sensitive internal details: db_password=super_secret_123");
            }

            @PostMapping("/test-only/duplicate-key")
            public void triggerDuplicate() {
                roleRepository.saveAndFlush(Role.builder()
                        .roleName("ROLE_UNIQUE_TEST_DUP")
                        .description("First insert")
                        .build());
                roleRepository.saveAndFlush(Role.builder()
                        .roleName("ROLE_UNIQUE_TEST_DUP")
                        .description("Second duplicate insert")
                        .build());
            }
        }
    }

    private String createExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject("admin@defecttracker.com")
                .issuedAt(new Date(now.getTime() - 100000))
                .expiration(new Date(now.getTime() - 50000))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("1. Calling protected endpoint without token returns HTTP 401 with standard envelope")
    void missingTokenIs401() throws Exception {
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));
    }

    @Test
    @DisplayName("2. Calling protected endpoint with expired token returns HTTP 401 'Token expired'")
    void expiredTokenIs401() throws Exception {
        String expiredToken = createExpiredToken();
        mockMvc.perform(get("/api/v1/employee")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Token expired"));
    }

    @Test
    @DisplayName("3. Calling protected endpoint with garbage token returns HTTP 401 'Invalid token'")
    void garbageTokenIs401() throws Exception {
        mockMvc.perform(get("/api/v1/employee")
                        .header("Authorization", "Bearer totally.invalid.garbage.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    @DisplayName("4. Authenticated user without required permission receives HTTP 403 Forbidden")
    void missingPermissionIs403() throws Exception {
        Role noPermRole = roleRepository.findByRoleName("ROLE_NO_PERM_R2")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("ROLE_NO_PERM_R2")
                        .description("Role with zero permissions for R2 test")
                        .build()));

        String email = "noperm_r2@defecttracker.com";
        String password = "Password123!";
        userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .userId("NOPM_R2")
                        .firstName("NoPerm")
                        .lastName("R2")
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .userStatus("ACTIVE")
                        .userType("CompanyStaff")
                        .roles(Set.of(noPermRole))
                        .build())
        );

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

        mockMvc.perform(get("/api/v1/employee")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied: you do not have permission to access this resource"));
    }

    @Test
    @DisplayName("5. Unique constraint violation returns HTTP 409 with only constraint name, never Detail text")
    void duplicateKeyIs409() throws Exception {
        String adminToken = obtainAdminToken();

        // Ensure clean state for duplicate test role
        roleRepository.findByRoleName("ROLE_UNIQUE_TEST_DUP").ifPresent(roleRepository::delete);

        // Call the test endpoint which inserts a duplicate role
        mockMvc.perform(post("/test-only/duplicate-key")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value(containsString("roles_role_name_key")))
                .andExpect(jsonPath("$.message").value(not(containsString("Detail:"))))
                .andExpect(jsonPath("$.message").value(not(containsString("Key ("))));

        // Also test direct repository insert throwing DataIntegrityViolationException
        assertThatThrownBy(() -> {
            roleRepository.saveAndFlush(Role.builder().roleName("ROLE_UNIQUE_TEST_DUP").build());
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("6. Calling non-existent route returns HTTP 404")
    void unknownRouteIs404() throws Exception {
        String adminToken = obtainAdminToken();
        mockMvc.perform(get("/api/v1/non-existent-route-for-testing-404")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("7. Unhandled exception returns HTTP 500 with Reference ID and does not leak exception details")
    void unexpectedExceptionIs500_withoutMessageLeak() throws Exception {
        String adminToken = obtainAdminToken();
        mockMvc.perform(get("/test-only/unhandled-error")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value(startsWith("An unexpected server error occurred. Reference ID: ")))
                .andExpect(jsonPath("$.message").value(not(containsString("Sensitive internal details"))))
                .andExpect(jsonPath("$.message").value(not(containsString("super_secret_123"))));
    }

    @Test
    @DisplayName("8. POST /api/v1/auth/refresh-token matches exact frontend contract")
    void refreshEndpointMatchesFrontendShape() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin@defecttracker.com",
                                "password", TEST_ADMIN_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String refreshToken = loginJson.get("data").get("refreshToken").asText();
        assertThat(refreshToken).isNotBlank();

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", refreshToken
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());
    }

    @Test
    @DisplayName("9. R2_loginAndRefreshWorkEvenWithStaleAuthorizationHeader: stale Authorization header does not block login or refresh")
    void R2_loginAndRefreshWorkEvenWithStaleAuthorizationHeader() throws Exception {
        String expiredToken = createExpiredToken();

        // 1. POST /api/v1/auth/login with stale Authorization header must succeed
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin@defecttracker.com",
                                "password", TEST_ADMIN_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.token").isString())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String validRefreshToken = loginJson.get("data").get("refreshToken").asText();

        // 2. POST /api/v1/auth/refresh-token with stale Authorization header must succeed
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", validRefreshToken
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());
    }

    @Test
    @DisplayName("10. Method not allowed returns HTTP 405")
    void methodNotAllowedIs405() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.statusCode").value(405))
                .andExpect(jsonPath("$.message").value("HTTP request method not supported"));
    }

    @Test
    @DisplayName("11. Unsupported media type returns HTTP 415")
    void unsupportedMediaTypeIs415() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<request></request>"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.statusCode").value(415))
                .andExpect(jsonPath("$.message").value("Unsupported media type"));
    }

    @Test
    @DisplayName("12. Malformed JSON request body returns HTTP 400 with fixed message")
    void malformedJsonIs400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed: json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid request body format"));
    }
}
