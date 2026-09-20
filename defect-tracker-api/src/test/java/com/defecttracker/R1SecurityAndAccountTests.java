package com.defecttracker;

import com.defecttracker.entity.Role;
import com.defecttracker.entity.User;
import com.defecttracker.repository.RoleRepository;
import com.defecttracker.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
class R1SecurityAndAccountTests extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("NFR_SEC_01: App fails to boot when JWT_SECRET is missing or shorter than 32 bytes")
    void NFR_SEC_01_appFailsToBootWithoutJwtSecret() {
        assertThrows(Exception.class, () -> {
            new SpringApplicationBuilder(DefectTrackerApiApplication.class)
                    .profiles("test")
                    .properties(
                            "spring.datasource.password=" + System.getenv("TEST_DB_PASSWORD"),
                            "app.seed.admin.password=" + TEST_ADMIN_PASSWORD,
                            "app.jwt.secret=too-short-secret"
                    )
                    .run();
        });
    }

    @Test
    @DisplayName("NFR_SEC_01: App fails to boot when SEED_ADMIN_PASSWORD is shorter than 12 chars or known weak")
    void NFR_SEC_01_weakAdminPasswordRejected() {
        assertThrows(Exception.class, () -> {
            new SpringApplicationBuilder(DefectTrackerApiApplication.class)
                    .profiles("test")
                    .properties(
                            "spring.datasource.password=" + System.getenv("TEST_DB_PASSWORD"),
                            "app.jwt.secret=" + TEST_JWT_SECRET,
                            "app.seed.admin.password=admin123"
                    )
                    .run();
        });
    }

    @Test
    @DisplayName("NFR_SEC_08: Application logs contain no plaintext passwords or JWT tokens")
    void NFR_SEC_08_logsContainNoSecrets(CapturedOutput output) throws Exception {
        obtainAdminToken();

        String logOutput = output.getAll();
        assertThat(logOutput)
                .as("Logs must not contain the admin password")
                .doesNotContain(TEST_ADMIN_PASSWORD);

        // Pattern matching standard JWT format: header.payload.signature
        Pattern jwtPattern = Pattern.compile("ey[A-Za-z0-9_-]{10,}\\.ey[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}");
        assertThat(jwtPattern.matcher(logOutput).find())
                .as("Logs must not contain any JWT strings")
                .isFalse();
    }

    @Test
    @DisplayName("FR_AUTH_01: Inactive user cannot log in, receives generic 401 message")
    void FR_AUTH_01_inactiveUserCannotLogin() throws Exception {
        String email = "inactive_user@defecttracker.com";
        String rawPassword = "StrongPassword123!";

        Role devRole = roleRepository.findByRoleName("Developer")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("Developer").description("Developer").build()));

        userRepository.findByEmail(email).ifPresent(userRepository::delete);

        userRepository.save(User.builder()
                .userId("INACT01")
                .firstName("Inactive")
                .lastName("User")
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .userStatus("INACTIVE")
                .userType("CompanyStaff")
                .roles(Set.of(devRole))
                .build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", email,
                                "password", rawPassword
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("FR_AUTH_01: Existing token of deactivated user is rejected immediately")
    void FR_AUTH_01_tokenOfDeactivatedUserRejected() throws Exception {
        String email = "to_deactivate@defecttracker.com";
        String rawPassword = "ValidPassword123!";

        Role devRole = roleRepository.findByRoleName("Developer")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("Developer").description("Developer").build()));

        userRepository.findByEmail(email).ifPresent(userRepository::delete);

        User user = userRepository.save(User.builder()
                .userId("DEACT01")
                .firstName("Deactivate")
                .lastName("Me")
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .userStatus("ACTIVE")
                .userType("CompanyStaff")
                .roles(Set.of(devRole))
                .build());

        // Login while active
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", email,
                                "password", rawPassword
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // Deactivate user in database
        user.setUserStatus("INACTIVE");
        userRepository.save(user);

        // Calling protected endpoint with existing token must now be rejected
        mockMvc.perform(get("/api/v1/user/me/permissions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("NFR_SEC_02: Swagger/OpenAPI endpoints require authentication outside dev profile")
    void NFR_SEC_02_swaggerRequiresAuthOutsideDev() throws Exception {
        // In "test" profile (outside "dev"), unauthenticated access is rejected
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().is4xxClientError());

        // When authenticated, access is allowed
        String adminToken = obtainAdminToken();
        mockMvc.perform(get("/v3/api-docs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("R1: Seeded admin login response includes mustChangePassword = true")
    void R1_seededAdminLoginResponseHasMustChangePasswordTrue() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin@defecttracker.com",
                                "password", TEST_ADMIN_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.mustChangePassword").value(true));
    }
}
