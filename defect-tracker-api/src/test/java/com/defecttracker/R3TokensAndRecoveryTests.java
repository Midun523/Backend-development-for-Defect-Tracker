package com.defecttracker;

import com.defecttracker.entity.PasswordResetToken;
import com.defecttracker.entity.RefreshToken;
import com.defecttracker.entity.Role;
import com.defecttracker.entity.User;
import com.defecttracker.repository.PasswordResetTokenRepository;
import com.defecttracker.repository.RefreshTokenRepository;
import com.defecttracker.repository.RoleRepository;
import com.defecttracker.repository.UserRepository;
import com.defecttracker.security.JwtTokenProvider;
import com.defecttracker.service.EmailService;
import com.defecttracker.service.TokenCleanupService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class R3TokensAndRecoveryTests extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private TokenCleanupService tokenCleanupService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @SpyBean
    private EmailService emailService;

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User createTestUser(String prefix) {
        Role role = roleRepository.findByRoleName("Developer")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("Developer").description("Dev").build()));

        String email = prefix.toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 6) + "@defecttracker.com";
        return userRepository.save(User.builder()
                .userId("U_" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password(passwordEncoder.encode("ValidPass1!"))
                .userStatus("ACTIVE")
                .userType("CompanyStaff")
                .roles(Set.of(role))
                .build());
    }

    private String generateRawResetToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    @Test
    @DisplayName("FR_AUTH_02: Refresh token is rejected as access token with HTTP 401")
    void FR_AUTH_02_refreshTokenRejectedAsAccessToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin@defecttracker.com",
                                "password", TEST_ADMIN_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String refreshToken = json.get("data").get("refreshToken").asText();

        // Calling protected endpoint with refresh token in Authorization header must be rejected
        mockMvc.perform(get("/api/v1/employee")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    @DisplayName("FR_AUTH_05: Password reset token can only be used once")
    void FR_AUTH_05_resetTokenSingleUse() throws Exception {
        User user = createTestUser("single_use");
        String rawToken = generateRawResetToken();

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256Hex(rawToken))
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        // First use: must succeed
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", rawToken,
                                "newPassword", "NewStrongPass1!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        // Second use: must be rejected with 400 Bad Request
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", rawToken,
                                "newPassword", "AnotherPass2@"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Invalid or expired reset token")));
    }

    @Test
    @DisplayName("FR_AUTH_05: Reset token expires after configured duration")
    void FR_AUTH_05_resetTokenExpiresAfterConfiguredMinutes() throws Exception {
        User user = createTestUser("expired_reset");
        String rawToken = generateRawResetToken();

        // Create token already expired in the past
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256Hex(rawToken))
                .expiresAt(Instant.now().minusSeconds(10))
                .build());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", rawToken,
                                "newPassword", "NewStrongPass1!"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Invalid or expired reset token")));
    }

    @Test
    @DisplayName("FR_AUTH_10: Forgot password returns neutral HTTP 200 for known and unknown emails")
    void FR_AUTH_10_forgotPasswordIsNeutral() throws Exception {
        // Known email
        MvcResult knownResult = mockMvc.perform(post("/api/v1/auth/forget-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "admin@defecttracker.com"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andReturn();

        // Unknown email
        MvcResult unknownResult = mockMvc.perform(post("/api/v1/auth/forget-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "unknown_user_12345@defecttracker.com"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andReturn();

        JsonNode knownJson = objectMapper.readTree(knownResult.getResponse().getContentAsString());
        JsonNode unknownJson = objectMapper.readTree(unknownResult.getResponse().getContentAsString());

        assertThat(knownJson.get("message").asText()).isEqualTo(unknownJson.get("message").asText());
    }

    @Test
    @DisplayName("FR_AUTH_06: Validate endpoint reflects token state accurately")
    void FR_AUTH_06_validateEndpointReflectsState() throws Exception {
        User user = createTestUser("validate_state");

        // 1. Valid token
        String validRaw = generateRawResetToken();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256Hex(validRaw))
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        mockMvc.perform(get("/api/v1/auth/validate-reset-token").param("token", validRaw))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true));

        // 2. Expired token
        String expiredRaw = generateRawResetToken();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256Hex(expiredRaw))
                .expiresAt(Instant.now().minusSeconds(10))
                .build());

        mockMvc.perform(get("/api/v1/auth/validate-reset-token").param("token", expiredRaw))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false));

        // 3. Used token
        String usedRaw = generateRawResetToken();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256Hex(usedRaw))
                .expiresAt(Instant.now().plusSeconds(600))
                .usedAt(Instant.now().minusSeconds(30))
                .build());

        mockMvc.perform(get("/api/v1/auth/validate-reset-token").param("token", usedRaw))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false));

        // 4. Non-existent token
        mockMvc.perform(get("/api/v1/auth/validate-reset-token").param("token", "completely_unknown_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false));
    }

    @Test
    @DisplayName("FR_AUTH_06: Weak password is rejected by policy")
    void FR_AUTH_06_weakPasswordRejected() throws Exception {
        User user = createTestUser("weak_pass");
        String rawToken = generateRawResetToken();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256Hex(rawToken))
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        // Too short (< 8)
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken, "newPassword", "Sh1!"))))
                .andExpect(status().isBadRequest());

        // No uppercase
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken, "newPassword", "lowercase1!"))))
                .andExpect(status().isBadRequest());

        // No digit
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken, "newPassword", "NoDigitHere!"))))
                .andExpect(status().isBadRequest());

        // No special character
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken, "newPassword", "NoSpecialChar1"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("FR_AUTH_08: Change password requires new password to differ from current password")
    void FR_AUTH_08_newPasswordMustDiffer() throws Exception {
        User user = createTestUser("change_pass");
        String currentPassword = "ValidPass1!";

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", user.getEmail(),
                                "password", currentPassword
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // Attempt to change password to the same current password
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", currentPassword,
                                "newPassword", currentPassword,
                                "confirmPassword", currentPassword
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("differ")));
    }

    @Test
    @DisplayName("Change password succeeds when confirmPassword is null or omitted")
    void changePassword_succeedsWhenConfirmPasswordOmitted() throws Exception {
        User user = createTestUser("change_pass_no_confirm");
        String currentPassword = "ValidPass1!";

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", user.getEmail(),
                                "password", currentPassword
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // Change password without confirmPassword (null / omitted)
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", currentPassword,
                                "newPassword", "BrandNewPass1!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Change password fails 400 when confirmPassword is provided and does not match newPassword")
    void changePassword_failsWhenConfirmPasswordMismatches() throws Exception {
        User user = createTestUser("change_pass_mismatch");
        String currentPassword = "ValidPass1!";

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", user.getEmail(),
                                "password", currentPassword
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", currentPassword,
                                "newPassword", "BrandNewPass1!",
                                "confirmPassword", "DifferentPass1!"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("match")));
    }

    @Test
    @DisplayName("Refresh token rotation: old refresh token is revoked upon refresh")
    void refreshRotationRevokesOldToken() throws Exception {
        User user = createTestUser("rotate_revokes");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", user.getEmail(),
                                "password", "ValidPass1!"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String oldRefreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();

        // Refresh: should rotate
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isOk())
                .andReturn();

        String newRefreshToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);

        // Immediate reuse of oldRefreshToken is rejected with 401
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("R3_rotatedTokenReusedWithinGraceDoesNotRevokeFamily: reuse within 30s does not revoke family")
    void R3_rotatedTokenReusedWithinGraceDoesNotRevokeFamily() throws Exception {
        User user = createTestUser("grace_window");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", user.getEmail(),
                                "password", "ValidPass1!"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String oldRefreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();

        // Rotate
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isOk())
                .andReturn();

        String newRefreshToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();

        // Reusing old token immediately (within 30s) -> 401, but does NOT revoke family
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isUnauthorized());

        // newRefreshToken must still work!
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", newRefreshToken))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("R3_rotatedTokenReusedAfterGraceRevokesFamily: reuse after grace window revokes whole family")
    void R3_rotatedTokenReusedAfterGraceRevokesFamily() throws Exception {
        User user = createTestUser("theft_detection");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", user.getEmail(),
                                "password", "ValidPass1!"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String token1 = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();

        // Rotate token1 -> token2
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", token1))))
                .andExpect(status().isOk())
                .andReturn();

        String token2 = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();

        // Simulate that token1 was rotated 45 seconds ago (beyond 30s grace window)
        RefreshToken rt1 = refreshTokenRepository.findByTokenHash(sha256Hex(token1)).orElseThrow();
        rt1.setRevokedAt(Instant.now().minusSeconds(45));
        rt1.setRevokedReason("ROTATED");
        refreshTokenRepository.save(rt1);

        // Present token1: detected as theft after grace window -> 401 and revokes family!
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", token1))))
                .andExpect(status().isUnauthorized());

        // token2 must now also be rejected with 401 because the whole family was revoked!
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", token2))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("revokedRefreshRevokesFamily: alias test confirming revoked refresh revokes family")
    void revokedRefreshRevokesFamily() throws Exception {
        R3_rotatedTokenReusedAfterGraceRevokesFamily();
    }

    @Test
    @DisplayName("R3_replayOfLoggedOutTokenDoesNotRevokeOtherSessions: Replay of logged out token returns 401 and does not revoke other sessions")
    void R3_replayOfLoggedOutTokenDoesNotRevokeOtherSessions() throws Exception {
        User user = createTestUser("session_isolation");

        // Login Session 1
        MvcResult login1 = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", user.getEmail(),
                                "password", "ValidPass1!"
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        String refresh1 = objectMapper.readTree(login1.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();

        // Login Session 2
        MvcResult login2 = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", user.getEmail(),
                                "password", "ValidPass1!"
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        String refresh2 = objectMapper.readTree(login2.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();

        // Verify different families
        RefreshToken rt1 = refreshTokenRepository.findByTokenHash(sha256Hex(refresh1)).orElseThrow();
        RefreshToken rt2 = refreshTokenRepository.findByTokenHash(sha256Hex(refresh2)).orElseThrow();
        assertThat(rt1.getFamilyId()).isNotEqualTo(rt2.getFamilyId());

        // Logout Session 1
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refresh1))))
                .andExpect(status().isOk());

        // Verify Session 1 token is revoked with reason LOGOUT
        RefreshToken rt1AfterLogout = refreshTokenRepository.findByTokenHash(sha256Hex(refresh1)).orElseThrow();
        assertThat(rt1AfterLogout.getRevokedAt()).isNotNull();
        assertThat(rt1AfterLogout.getRevokedReason()).isEqualTo("LOGOUT");

        // Replay of logged out token: returns 401
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refresh1))))
                .andExpect(status().isUnauthorized());

        // Verify Session 2 is NOT revoked
        RefreshToken rt2AfterReplay = refreshTokenRepository.findByTokenHash(sha256Hex(refresh2)).orElseThrow();
        assertThat(rt2AfterReplay.getRevokedAt()).isNull();

        // Session 2 can still refresh successfully
        MvcResult refresh2Result = mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refresh2))))
                .andExpect(status().isOk())
                .andReturn();

        String newRefresh2 = objectMapper.readTree(refresh2Result.getResponse().getContentAsString())
                .get("data").get("refreshToken").asText();
        RefreshToken rt2Rotated = refreshTokenRepository.findByTokenHash(sha256Hex(newRefresh2)).orElseThrow();
        assertThat(rt2Rotated.getFamilyId()).isEqualTo(rt2.getFamilyId());
    }

    @Test
    @DisplayName("Email service receives reset link exactly once and no log line contains the raw token")
    void emailServiceReceivesResetLinkAndNoTokenInLogs() throws Exception {
        User user = createTestUser("email_check");

        // Clear previous invocations on the spy
        Mockito.clearInvocations(emailService);

        // Capture logs using Logback
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> listAppender = new ch.qos.logback.core.read.ListAppender<>();
        listAppender.start();
        rootLogger.addAppender(listAppender);

        try {
            mockMvc.perform(post("/api/v1/auth/forget-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("email", user.getEmail()))))
                    .andExpect(status().isOk());

            // Verify emailService received sendTemplatedEmail exactly once
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
            verify(emailService, times(1))
                    .sendTemplatedEmail(eq(user.getEmail()), eq("PASSWORD_RESET"), captor.capture());

            Map<String, String> variables = captor.getValue();
            assertThat(variables).isNotNull();
            assertThat(variables).containsKey("token");
            assertThat(variables).containsKey("link");
            assertThat(variables).containsKey("name");

            String rawToken = variables.get("token");
            String link = variables.get("link");

            assertThat(rawToken).isNotBlank();
            assertThat(link).isEqualTo("http://localhost:5173/reset-password?token=" + rawToken);

            // Verify the token hashes to what is stored in DB
            Optional<PasswordResetToken> tokenInDb = passwordResetTokenRepository.findByTokenHash(sha256Hex(rawToken));
            assertThat(tokenInDb).isPresent();
            assertThat(tokenInDb.get().getUser().getId()).isEqualTo(user.getId());

            // Verify NO log event contains the raw token or link
            for (ch.qos.logback.classic.spi.ILoggingEvent event : listAppender.list) {
                String formattedMessage = event.getFormattedMessage();
                assertThat(formattedMessage).doesNotContain(rawToken);
                assertThat(formattedMessage).doesNotContain("token=" + rawToken);
            }
        } finally {
            rootLogger.detachAppender(listAppender);
        }
    }

    @Test
    @DisplayName("FR_AUTH_07: Cleanup job removes expired refresh tokens and expired/used reset tokens")
    void FR_AUTH_07_cleanupJobRemovesExpired() {
        User user = createTestUser("cleanup");

        // Expired refresh token
        RefreshToken expiredRefresh = refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(sha256Hex("expired_refresh_" + UUID.randomUUID()))
                .familyId(UUID.randomUUID())
                .expiresAt(Instant.now().minusSeconds(100))
                .build());

        // Expired reset token
        PasswordResetToken expiredReset = passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256Hex("expired_reset_" + UUID.randomUUID()))
                .expiresAt(Instant.now().minusSeconds(100))
                .build());

        // Used reset token
        PasswordResetToken usedReset = passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256Hex("used_reset_" + UUID.randomUUID()))
                .expiresAt(Instant.now().plusSeconds(600))
                .usedAt(Instant.now().minusSeconds(10))
                .build());

        // Active tokens
        RefreshToken activeRefresh = refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(sha256Hex("active_refresh_" + UUID.randomUUID()))
                .familyId(UUID.randomUUID())
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        PasswordResetToken activeReset = passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256Hex("active_reset_" + UUID.randomUUID()))
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        // Run cleanup
        tokenCleanupService.cleanupExpiredTokens();

        assertThat(refreshTokenRepository.findById(expiredRefresh.getId())).isEmpty();
        assertThat(passwordResetTokenRepository.findById(expiredReset.getId())).isEmpty();
        assertThat(passwordResetTokenRepository.findById(usedReset.getId())).isEmpty();

        assertThat(refreshTokenRepository.findById(activeRefresh.getId())).isPresent();
        assertThat(passwordResetTokenRepository.findById(activeReset.getId())).isPresent();
    }
}
