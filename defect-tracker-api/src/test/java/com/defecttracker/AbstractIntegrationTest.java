package com.defecttracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AbstractIntegrationTest.TestFlywayConfig.class)
public abstract class AbstractIntegrationTest {

    protected static final String TEST_ADMIN_PASSWORD = "AdminTest_" + UUID.randomUUID().toString().replace("-", "");
    protected static final String TEST_JWT_SECRET;

    static {
        String envPassword = System.getenv("TEST_DB_PASSWORD");
        if (envPassword == null || envPassword.isBlank()) {
            envPassword = System.getProperty("TEST_DB_PASSWORD");
        }
        if (envPassword == null || envPassword.isBlank()) {
            throw new IllegalStateException(
                    "TEST_DB_PASSWORD environment variable or system property is required to run tests against the test database, but it was not set."
            );
        }

        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        TEST_JWT_SECRET = HexFormat.of().formatHex(keyBytes);
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> TEST_JWT_SECRET);
        registry.add("app.seed.admin.password", () -> TEST_ADMIN_PASSWORD);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @TestConfiguration
    public static class TestFlywayConfig {
        private static final AtomicBoolean CLEANED = new AtomicBoolean(false);

        @Bean
        public FlywayMigrationStrategy cleanMigrateStrategy() {
            return flyway -> {
                if (CLEANED.compareAndSet(false, true)) {
                    try (Connection conn = flyway.getConfiguration().getDataSource().getConnection()) {
                        String jdbcUrl = conn.getMetaData().getURL();
                        validateTestDatabaseName(jdbcUrl);
                        flyway.clean();
                    } catch (SQLException e) {
                        throw new IllegalStateException("Failed to verify database URL or clean database", e);
                    }
                }
                flyway.migrate();
            };
        }

        private static void validateTestDatabaseName(String jdbcUrl) {
            if (jdbcUrl == null) {
                throw new IllegalStateException("JDBC URL is null. Refusing to perform Flyway clean.");
            }
            String cleanUrl = jdbcUrl.contains("?") ? jdbcUrl.substring(0, jdbcUrl.indexOf('?')) : jdbcUrl;
            int lastSlash = cleanUrl.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new IllegalStateException("Cannot parse database name from JDBC URL: " + jdbcUrl);
            }
            String dbName = cleanUrl.substring(lastSlash + 1);
            if (!dbName.endsWith("_test")) {
                throw new IllegalStateException(
                        "SAFETY CHECK FAILED: Refusing to clean database '" + dbName
                                + "'. The database name must end with '_test' (JDBC URL: " + jdbcUrl + ")"
                );
            }
        }
    }

    protected String obtainAdminToken() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "admin@defecttracker.com",
                "password", TEST_ADMIN_PASSWORD
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode dataNode = responseNode.get("data");
        if (dataNode != null && dataNode.has("token")) {
            return dataNode.get("token").asText();
        } else if (dataNode != null && dataNode.has("accessToken")) {
            return dataNode.get("accessToken").asText();
        }
        throw new IllegalStateException("Token not found in login response: " + result.getResponse().getContentAsString());
    }
}
