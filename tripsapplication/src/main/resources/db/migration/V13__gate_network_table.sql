-- ---------------------------------------------------------------------------
-- V13__gate_network_table.sql
-- Constructs feature v2 Phase E.1 §5 (see
-- docs/design/constructs-e1-in-system-feature-foundation.md if added retroactively;
-- otherwise the in-tree design conversation that produced E.1).
--
-- Introduces JPA persistence for GateNetwork, the first top-level catalog entity
-- outside the SpaceAsset/SpaceInfrastructure sealed hierarchies. A GateNetwork is
-- a *grouping* of TransportNode gate instances forming a connected transit
-- network (e.g. "Aldenata Civilian Network", "Posleen Military Network").
--
-- Pipeline parallels the four sealed-hierarchy catalog families:
--   GateNetworkEntity, GateNetworkMapper, GateNetworkRepository,
--   GateNetworkDesignerService, GateNetworkCatalogSeeder.
--
-- The seeder is vacuous in E.1 — Catalog ships zero canonical GateNetwork
-- constants in this phase. Phase E.2 populates Aldenata + Posleen + other
-- canonical networks; the seeder activates automatically when they land.
--
-- Schema notes:
--   - No collection columns (no JSON LOBs) — GateNetwork has no Set/List fields.
--   - Provenance flattened to four columns per the D.6 pattern.
--   - L2 cache NOT enabled (no read pressure predicted at E.1 scale; SolarSystem
--     and Megastructure are the only L2-cached entities today).
--
-- Idempotent via IF NOT EXISTS; forward-only; no down migration.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS gate_network (
    id                          VARCHAR(64)  NOT NULL PRIMARY KEY,
    name                        VARCHAR(255) NOT NULL,
    builder_polity              VARCHAR(255),
    lifecycle                   VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    transponder_name            VARCHAR(255),
    description                 VARCHAR(4000),
    notes                       VARCHAR(4000),
    category                    VARCHAR(255),

    provenance_source_type      VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN',
    provenance_source_universe  VARCHAR(255) NOT NULL DEFAULT '',
    provenance_source_work      VARCHAR(255),
    provenance_status           VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN',

    created_at                  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    modified_at                 TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_gate_network_name
    ON gate_network (name ASC);

CREATE INDEX IF NOT EXISTS idx_gate_network_lifecycle
    ON gate_network (lifecycle);

CREATE INDEX IF NOT EXISTS idx_gate_network_builder_polity
    ON gate_network (builder_polity);

CREATE INDEX IF NOT EXISTS idx_gate_network_provenance_source_universe
    ON gate_network (provenance_source_universe);
