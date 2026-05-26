package com.teamgannon.trips.tools;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates the Flyway baseline DDL by booting a JPA slice and asking Hibernate
 * to write its schema-create script to disk.
 * <p>
 * Disabled by default — this is a regeneration utility, not a regular test.
 * <p>
 * To regenerate {@code db/migration/V1__baseline.sql}:
 * <ol>
 *   <li>Remove the {@code @Disabled} annotation on this class.</li>
 *   <li>Run {@code ./mvnw-java25.sh -pl tripsapplication test -Dtest=SchemaBaselineExporterTest}.</li>
 *   <li>Inspect {@code target/baseline-schema.sql}; copy into
 *       {@code src/main/resources/db/migration/V1__baseline.sql}.</li>
 *   <li>Re-add {@code @Disabled} and commit.</li>
 * </ol>
 * See {@code src/main/resources/db/migration/README.md} for the full workflow.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=false",
        "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create",
        "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=target/baseline-schema.sql",
        "spring.jpa.properties.jakarta.persistence.schema-generation.create-source=metadata",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Disabled("Regeneration tool. See class Javadoc to enable.")
class SchemaBaselineExporterTest {

    /**
     * Hibernate writes the schema-create script during context startup when
     * {@code jakarta.persistence.schema-generation.scripts.action=create} is set.
     * This method just confirms the file appeared and reports its size.
     */
    @Test
    void exportSchema() throws Exception {
        Path out = Path.of("target/baseline-schema.sql");
        if (!Files.exists(out)) {
            throw new IllegalStateException(
                    "Schema export did not produce " + out.toAbsolutePath()
                    + " — check schema-generation properties and Hibernate config.");
        }
        long bytes = Files.size(out);
        System.out.println("Baseline schema written: " + out.toAbsolutePath() + " (" + bytes + " bytes)");
    }
}
