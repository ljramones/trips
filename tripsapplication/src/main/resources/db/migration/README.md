# Flyway migrations

This directory holds the Flyway migration files for TRIPS. Naming follows the
standard `V<version>__<description>.sql` convention, applied in version order.

## Conventions

- **`V1__baseline.sql`** captures the JPA-generated schema at the point Flyway
  was adopted (Phase 0.2 of the codebase-review remediation). It is generated
  from entity annotations, not hand-written — re-generate it via
  `SchemaBaselineExporterTest` (see "Regenerating the baseline" below).
- **`V2+`** are hand-written migrations for every entity change going forward.
  - Adding a column? `V2__add_star_object_some_column.sql`
  - Dropping an orphan column? `V3__drop_spaceship_series.sql`
  - Data backfill? `V4__backfill_transfer_plan_available_propellant.sql`
- Do **NOT** edit a migration file once it has been applied to a non-dev DB.
  Add a new migration instead.

## ddl-auto interaction

`application.yml` keeps `spring.jpa.hibernate.ddl-auto: update` for the **dev**
profile (Hibernate auto-syncs the schema during entity prototyping). The
**prod** profile sets `validate`, so deployed instances enforce that Flyway's
schema matches the entity model.

Plan: flip the dev default to `validate` once Phase 1.4 (orphan-column cleanup)
lands. Until then, devs may see "ddl-auto update modified the schema but no
migration was written" — that's the cue to capture the change as a new V*
migration before merging.

## Regenerating the baseline

The baseline must match what Hibernate would generate for the current entity
graph against the H2 dialect.

1. Open `src/test/java/com/teamgannon/trips/tools/SchemaBaselineExporterTest.java`.
2. Remove the `@Disabled` annotation.
3. Run:
   ```
   ./mvnw-java25.sh -pl tripsapplication test -Dtest=SchemaBaselineExporterTest
   ```
4. The test writes the schema to `tripsapplication/target/baseline-schema.sql`.
5. Inspect; copy/replace this file:
   ```
   cp tripsapplication/target/baseline-schema.sql \
      tripsapplication/src/main/resources/db/migration/V1__baseline.sql
   ```
6. Re-add `@Disabled` to the test (it stays in the tree as a regeneration tool).
7. Commit.

## Flyway baseline-on-migrate

`spring.flyway.baseline-on-migrate=true` is set, so existing H2 databases that
were created under the old `ddl-auto: update` regime will be silently versioned
at V1 without re-running the baseline. New empty databases will run V1 to
create the schema.
