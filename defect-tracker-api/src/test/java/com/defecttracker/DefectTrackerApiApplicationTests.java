package com.defecttracker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DefectTrackerApiApplicationTests extends AbstractIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Smoke Test: Application context loads successfully")
    void contextLoads() {
        assertThat(entityManagerFactory).isNotNull();
        assertThat(entityManager).isNotNull();
    }

    @Test
    @DisplayName("Smoke Test: Flyway applies all migration files on empty database")
    void flywayAppliesAllMigrationsOnEmptyDatabase() throws Exception {
        Resource[] migrationFiles = resourcePatternResolver.getResources("classpath:db/migration/V*.sql");
        int expectedMigrationCount = migrationFiles.length;
        assertThat(expectedMigrationCount).isGreaterThan(0);

        MigrationInfo[] appliedMigrations = flyway.info().applied();
        assertThat(appliedMigrations).hasSize(expectedMigrationCount);

        for (MigrationInfo info : appliedMigrations) {
            assertThat(info.getState().isApplied())
                    .as("Migration %s should be applied", info.getScript())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Smoke Test: Hibernate schema validation passes")
    void hibernateSchemaValidationPasses() {
        assertThat(entityManagerFactory.isOpen()).isTrue();
        // Execute a native query to confirm persistence context works against the validated schema
        Object count = entityManager.createNativeQuery("SELECT count(*) FROM designations").getSingleResult();
        assertThat(count).isNotNull();
    }

    @Test
    @DisplayName("Smoke Test: Seeded super admin can successfully log in")
    void seededAdminCanLogIn() throws Exception {
        Map<String, String> loginPayload = Map.of(
                "username", "admin@defecttracker.com",
                "password", TEST_ADMIN_PASSWORD
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }
}
