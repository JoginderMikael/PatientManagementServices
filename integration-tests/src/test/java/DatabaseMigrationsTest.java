import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class DatabaseMigrationsTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("patient_management")
            .withUsername("test")
            .withPassword("test");

    @Test
    void appliesAuthProductionMigrationsWithoutDevelopmentUsers() throws Exception {
        MigrateResult result = migrate("auth_service", "auth-service");

        assertEquals(1, result.migrationsExecuted);
        assertTrue(tableExists("auth_service", "users"));
        assertEquals(0, rowCount("auth_service", "users"));
    }

    @Test
    void appliesPatientProductionMigrationsWithoutDevelopmentPatients() throws Exception {
        MigrateResult result = migrate("patient_service", "patient-service");

        assertEquals(2, result.migrationsExecuted);
        assertTrue(tableExists("patient_service", "patient"));
        assertTrue(tableExists("patient_service", "patient_identifier"));
        assertTrue(tableExists("patient_service", "patient_merge_history"));
        assertTrue(tableExists("patient_service", "outbox_event"));
        assertEquals(0, rowCount("patient_service", "patient"));
    }

    @Test
    void appliesBillingProductionMigrations() throws Exception {
        assertEquals(1, migrate("billing_service", "billing-service").migrationsExecuted);
        assertTrue(tableExists("billing_service", "billing_account"));
    }

    @Test
    void appliesAppointmentProductionMigrations() throws Exception {
        assertEquals(1, migrate("appointment_service", "appointment-service").migrationsExecuted);
        assertTrue(tableExists("appointment_service", "appointment"));
        assertTrue(tableExists("appointment_service", "appointment_slot"));
    }

    @Test
    void appliesEhrProductionMigrations() throws Exception {
        assertEquals(1, migrate("ehr_service", "ehr-service").migrationsExecuted);
        assertTrue(tableExists("ehr_service", "encounter"));
        assertTrue(tableExists("ehr_service", "diagnosis"));
        assertTrue(tableExists("ehr_service", "prescription"));
        assertTrue(tableExists("ehr_service", "lab_result"));
        assertTrue(tableExists("ehr_service", "clinical_note"));
    }

    @Test
    void appliesNotificationProductionMigrations() throws Exception {
        assertEquals(1, migrate("notification_service", "notification-service").migrationsExecuted);
        assertTrue(tableExists("notification_service", "notification_message"));
    }

    @Test
    void appliesAuditProductionMigrations() throws Exception {
        assertEquals(1, migrate("audit_service", "audit-compliance-service").migrationsExecuted);
        assertTrue(tableExists("audit_service", "audit_event"));
    }

    private MigrateResult migrate(String schema, String module) {
        Path migrationDirectory = repositoryRoot()
                .resolve(module)
                .resolve("src/main/resources/db/migration");

        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .defaultSchema(schema)
                .schemas(schema)
                .locations("filesystem:" + migrationDirectory.toString().replace('\\', '/'))
                .load();

        return flyway.migrate();
    }

    private boolean tableExists(String schema, String table) throws Exception {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
        try (Connection connection = postgres.createConnection("");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, table);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) == 1;
            }
        }
    }

    private int rowCount(String schema, String table) throws Exception {
        String sql = "SELECT COUNT(*) FROM " + schema + "." + table;
        try (Connection connection = postgres.createConnection("");
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private Path repositoryRoot() {
        Path candidate = Path.of(System.getProperty("maven.multiModuleProjectDirectory", System.getProperty("user.dir")))
                .toAbsolutePath()
                .normalize();
        if (Files.isDirectory(candidate.resolve("auth-service"))) {
            return candidate;
        }

        Path parent = candidate.getParent();
        if (parent != null && Files.isDirectory(parent.resolve("auth-service"))) {
            return parent;
        }

        throw new IllegalStateException("Cannot locate repository root from " + candidate);
    }
}
