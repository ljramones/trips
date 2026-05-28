-- ---------------------------------------------------------------------------
-- V7__station_design_table.sql
-- Constructs feature v2 Phase A (see docs/design/constructs-feature-plan-v2.md).
--
-- Introduces JPA persistence for StationDesign, the first of three currently-
-- in-memory-only SpaceAsset subtypes (the others, WeaponInstallation and
-- TransportNode, ship in v2 Phase B).
--
-- Schema mirrors com.teamgannon.trips.spaceshipmodeller.persistence.StationEntity
-- column-for-column; FlywayBaselineSmokeTest runs ddl-auto=validate against
-- this table, so any drift between this file and the entity will fail the
-- smoke test rather than reaching production.
--
-- Collection fields (carriedCraft, armaments) are serialised to JSON LOBs by
-- StationDesignMapper. v2 §4 documents why this is the existing pattern and
-- why a whole-entity payload_json CLOB would re-trip the Issue 46
-- LazyInitializationException constraint.
--
-- Idempotent via IF NOT EXISTS: re-running this migration is a no-op.
-- Forward-only; no down migration.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS station_design (
    id                       VARCHAR(255) NOT NULL,
    name                     VARCHAR(255) NOT NULL,
    designation              VARCHAR(255),
    station_type             VARCHAR(64)  NOT NULL,
    source                   VARCHAR(255),
    faction                  VARCHAR(255),
    concealed                BOOLEAN      NOT NULL DEFAULT FALSE,
    allegiance               VARCHAR(255),
    description              VARCHAR(4000),
    overall_span_meters      FLOAT(53)    NOT NULL,
    interior_span_meters     FLOAT(53)    NOT NULL,
    dry_mass_tons            FLOAT(53)    NOT NULL,
    armour_thickness_meters  FLOAT(53)    NOT NULL,
    crew_capacity            INTEGER      NOT NULL,
    crew_complement          INTEGER      NOT NULL,
    pressurized_volume_m3    FLOAT(53)    NOT NULL,
    mobility                 VARCHAR(64)  NOT NULL,
    auxiliary_drive          VARCHAR(64),
    carried_craft_json       CLOB,
    armaments_json           CLOB,
    hangar_volume_m3         FLOAT(53)    NOT NULL,
    carrier_capable          BOOLEAN      NOT NULL DEFAULT FALSE,
    tech_level               VARCHAR(64)  NOT NULL,
    category                 VARCHAR(255),
    operational_state        VARCHAR(64)  NOT NULL DEFAULT 'OPERATIONAL',
    created_at               TIMESTAMP(6) WITH TIME ZONE,
    modified_at              TIMESTAMP(6) WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_station_design_name           ON station_design (name ASC);
CREATE INDEX IF NOT EXISTS idx_station_design_station_type   ON station_design (station_type);
CREATE INDEX IF NOT EXISTS idx_station_design_mobility       ON station_design (mobility);
CREATE INDEX IF NOT EXISTS idx_station_design_faction        ON station_design (faction);
