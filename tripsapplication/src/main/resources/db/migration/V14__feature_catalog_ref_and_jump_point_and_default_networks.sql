-- ---------------------------------------------------------------------------
-- V14__feature_catalog_ref_and_jump_point_and_default_networks.sql
-- Constructs feature v2 Phase E.1 §6 + §5.4 (see in-tree design conversation
-- that produced E.1).
--
-- Three logical schema extensions in one migration (per design §6.4's "agent's
-- discretion" on grouping):
--
--   1. solar_system_feature gains four new columns (E.1 §6.1 + Divergence C):
--        parent_body_id          — per-body parent reference (e.g. star id for
--                                  per-star JUMP_POINT features); resolves
--                                  Divergence C ("no parentBodyId column today").
--        catalog_reference_id    — points at a catalog entry (catalog-* id).
--        catalog_reference_kind  — CatalogedKind discriminator for the above.
--        network_id              — for JUMP_GATE features only, the GateNetwork
--                                  id this gate belongs to.
--
--   2. Indexes on the new columns. The (catalog_reference_id,
--      catalog_reference_kind) composite supports the polymorphic-dispatch
--      lookup pattern in E.1 §6.2.
--
--   3. spaceship_design gains default_accessible_network_ids_json — JSON LOB
--      carrying the set of GateNetwork ids that ships of this design have
--      transponder access to by default (E.1 §5.4). Per-instance overrides
--      are deferred to a future phase.
--
-- The JUMP_POINT feature-type *value* is added in the same step at the Java
-- layer (SolarSystemFeature.FeatureType static-final constant); the database
-- column is a free-form String, so no schema change is needed to accept it.
--
-- Idempotent via IF NOT EXISTS; forward-only; no down migration.
-- ---------------------------------------------------------------------------

-- ---- Block 1: solar_system_feature column additions --------------------

ALTER TABLE solar_system_feature
    ADD COLUMN IF NOT EXISTS parent_body_id VARCHAR(64) DEFAULT NULL;

ALTER TABLE solar_system_feature
    ADD COLUMN IF NOT EXISTS catalog_reference_id VARCHAR(64) DEFAULT NULL;

ALTER TABLE solar_system_feature
    ADD COLUMN IF NOT EXISTS catalog_reference_kind VARCHAR(32) DEFAULT NULL;

ALTER TABLE solar_system_feature
    ADD COLUMN IF NOT EXISTS network_id VARCHAR(64) DEFAULT NULL;

-- ---- Block 2: indexes for the new columns ------------------------------

CREATE INDEX IF NOT EXISTS idx_solar_system_feature_parent_body
    ON solar_system_feature (parent_body_id);

CREATE INDEX IF NOT EXISTS idx_solar_system_feature_catalog_reference
    ON solar_system_feature (catalog_reference_id, catalog_reference_kind);

CREATE INDEX IF NOT EXISTS idx_solar_system_feature_network
    ON solar_system_feature (network_id);

-- ---- Block 3: spaceship_design default-network-access column -----------

ALTER TABLE spaceship_design
    ADD COLUMN IF NOT EXISTS default_accessible_network_ids_json CLOB DEFAULT NULL;
