package com.teamgannon.trips.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test for the Flyway baseline added in Phase 0.2.
 * <p>
 * Boots a minimal Spring + JPA slice against an in-memory H2, lets Flyway
 * apply {@code V1__baseline.sql}, then runs Hibernate {@code ddl-auto=validate}
 * to assert that the schema Flyway produced exactly matches the current entity
 * model. If this test fails after an entity change, add a new
 * {@code V2__...sql} (or {@code V3__}, etc.) migration that brings the schema
 * back in line — do not edit the V1 baseline.
 * <p>
 * This is the regression guard that will make {@code ddl-auto=validate} safe
 * to flip as the default in Phase 1.4 of the remediation plan.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=true",
        "spring.flyway.baseline-version=1",
        "spring.flyway.locations=classpath:db/migration"
})
class FlywayBaselineSmokeTest {

    @Test
    void flywayAppliesBaselineAndHibernateValidates() {
        // Context-load success is the assertion: it means Flyway ran V1
        // cleanly AND Hibernate's validate step found no schema/entity drift.
    }
}
