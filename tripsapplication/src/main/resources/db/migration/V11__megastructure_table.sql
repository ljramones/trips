-- ---------------------------------------------------------------------------
-- V11__megastructure_table.sql
-- Constructs feature v2 Phase D.7 (see docs/design/constructs-feature-plan-v2.md
-- and the megastructure-subtype design under docs/design/).
--
-- Introduces JPA persistence for Megastructure, the fourth sealed subtype of
-- SpaceAsset (after SpaceshipDesign, StationDesign, WeaponInstallation). Scale-
-- class, self-contained-setting catalog assets: hollowed asteroids (Troy),
-- purpose-built war machines (Death Star), disguised moons (Dahak), found
-- enigmas (Rama), engineered worlds (Ringworld).
--
-- Schema mirrors com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureEntity
-- column-for-column; FlywayBaselineSmokeTest runs ddl-auto=validate against
-- this table, so any drift between this file and the entity will fail the
-- smoke test rather than reaching production.
--
-- Collection fields (secondaryFunctions, armaments) are serialised to JSON LOBs
-- by MegastructureDesignMapper, matching the StationEntity / StationDesignMapper
-- pattern. Provenance is decomposed into four flat columns per the v2 Phase D.6
-- precedent (filter-friendly; avoids the Issue 46 whole-entity payload_json
-- pitfall).
--
-- Mass is canonically stored as megatons (10^6 tons) for scale honesty;
-- megastructures routinely span 10^6 to 10^15 tons, which is unwieldy at ton
-- scale. The Megastructure record overrides SpaceAsset#dryMassTons() to derive
-- the ton-scale value from dry_mass_megatons × 10^6.
--
-- Indexes on archetype, origin_type, and provenance_source_universe per design
-- §5.3 — these are the dimensions the future Megastructure browser will
-- filter on.
--
-- Idempotent via IF NOT EXISTS; forward-only; no down migration.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS megastructure (
    id                            VARCHAR(255) NOT NULL,
    name                          VARCHAR(255) NOT NULL,
    designation                   VARCHAR(255),
    description                   VARCHAR(4000),
    category                      VARCHAR(255),
    notes                         VARCHAR(4000),

    archetype                     VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN',
    dimensions_km                 FLOAT(53)    NOT NULL DEFAULT 0,
    dry_mass_megatons             FLOAT(53)    NOT NULL DEFAULT 0,
    internal_volume_km3           FLOAT(53)    NOT NULL DEFAULT 0,

    mobility                      VARCHAR(32)  NOT NULL DEFAULT 'STATIONKEEPING',
    auxiliary_drive               VARCHAR(64),

    origin_type                   VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN',
    builder_polity                VARCHAR(255),
    discovery_year                INTEGER,
    construction_year             INTEGER,

    primary_function              VARCHAR(64)  NOT NULL DEFAULT 'UNKNOWN',
    secondary_functions_json      CLOB,

    has_interior_setting          BOOLEAN      NOT NULL DEFAULT FALSE,
    interior_population           BIGINT       NOT NULL DEFAULT 0,
    interior_gravity              VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN',

    operational_state             VARCHAR(64)  NOT NULL DEFAULT 'OPERATIONAL',
    concealed                     BOOLEAN      NOT NULL DEFAULT FALSE,

    armaments_json                CLOB,

    provenance_source_type        VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN',
    provenance_source_universe    VARCHAR(255) NOT NULL DEFAULT '',
    provenance_source_work        VARCHAR(255),
    provenance_status             VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN',

    faction                       VARCHAR(255),
    allegiance                    VARCHAR(255),
    tech_level                    VARCHAR(64)  NOT NULL DEFAULT 'UNKNOWN',

    created_at                    TIMESTAMP(6) WITH TIME ZONE,
    modified_at                   TIMESTAMP(6) WITH TIME ZONE,

    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_megastructure_name
    ON megastructure (name ASC);

CREATE INDEX IF NOT EXISTS idx_megastructure_archetype
    ON megastructure (archetype);

CREATE INDEX IF NOT EXISTS idx_megastructure_origin_type
    ON megastructure (origin_type);

CREATE INDEX IF NOT EXISTS idx_megastructure_provenance_source_universe
    ON megastructure (provenance_source_universe);
