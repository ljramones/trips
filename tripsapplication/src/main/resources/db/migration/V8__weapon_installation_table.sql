-- ---------------------------------------------------------------------------
-- V8__weapon_installation_table.sql
-- Constructs feature v2 Phase B (see docs/design/constructs-feature-plan-v2.md).
--
-- Introduces JPA persistence for WeaponInstallation, the second of the three
-- currently-in-memory-only SpaceAsset subtypes (Station landed in V7; the
-- last, TransportNode, is on the SpaceInfrastructure side in V9).
--
-- Schema mirrors com.teamgannon.trips.spaceshipmodeller.persistence
-- .WeaponInstallationEntity column-for-column; FlywayBaselineSmokeTest runs
-- ddl-auto=validate against this table, so any drift between this file and
-- the entity will fail the smoke test rather than reaching production.
--
-- The armaments collection is serialised to a JSON LOB by
-- WeaponInstallationMapper. This is the existing collection-LOB pattern that
-- v2 §4 chose over a whole-entity payload_json CLOB (the latter would
-- re-trip the Issue 46 LazyInitializationException constraint).
--
-- Idempotent via IF NOT EXISTS: re-running this migration is a no-op.
-- Forward-only; no down migration.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS weapon_installation (
    id                    VARCHAR(255) NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    designation           VARCHAR(255),
    installation_type     VARCHAR(64)  NOT NULL,
    emplacement           VARCHAR(64)  NOT NULL,
    source                VARCHAR(255),
    faction               VARCHAR(255),
    concealed             BOOLEAN      NOT NULL DEFAULT FALSE,
    description           VARCHAR(4000),
    dry_mass_tons         FLOAT(53)    NOT NULL,
    footprint_span_meters FLOAT(53)    NOT NULL,
    mobile                BOOLEAN      NOT NULL DEFAULT FALSE,
    crew_complement       INTEGER      NOT NULL,
    armaments_json        CLOB,
    tech_level            VARCHAR(64)  NOT NULL,
    category              VARCHAR(255),
    operational_state     VARCHAR(64)  NOT NULL DEFAULT 'OPERATIONAL',
    created_at            TIMESTAMP(6) WITH TIME ZONE,
    modified_at           TIMESTAMP(6) WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_weapon_installation_name              ON weapon_installation (name ASC);
CREATE INDEX IF NOT EXISTS idx_weapon_installation_installation_type ON weapon_installation (installation_type);
CREATE INDEX IF NOT EXISTS idx_weapon_installation_emplacement       ON weapon_installation (emplacement);
CREATE INDEX IF NOT EXISTS idx_weapon_installation_faction           ON weapon_installation (faction);
