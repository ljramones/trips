-- ---------------------------------------------------------------------------
-- V16__catalog_universe_id_columns.sql
-- Worldbuilding Platform Phase F.1 §4.3 (see
-- docs/design/phase-f1-worldbuilding-universes-foundation.md).
--
-- Extends the 6 existing catalog tables with a nullable universe_id FK
-- referencing universe(id). NULL means canonical/real-data (always visible);
-- non-NULL scopes the entry to the referenced universe (visible only when
-- that universe is active).
--
-- The 6 catalog tables:
--   - station_design       (V7)
--   - weapon_installation  (V8)
--   - megastructure        (V11)
--   - spaceship_design     (V1 baseline)
--   - gate_network         (V13)
--   - transport_node       (V9, currently empty per V9 header but column
--                           added for schema uniformity — when canonical
--                           transport nodes are seeded they'll need scoping)
--
-- ON DELETE SET NULL: deleting a universe row orphans its catalog entries
-- as canonical/real rather than cascade-deleting them. Per design doc
-- §4.3, R2.10's "deleting a universe loses its content" is interpreted
-- as "orphan content; user must explicitly clean up" — safer for data
-- preservation, especially user-created universes.
--
-- Indexes on universe_id support cheap filtering in UniverseFilteringService
-- (Step 6). Even though the column is mostly-NULL today (only a handful
-- of pre-existing fiction-canon entries will be populated in V17's
-- UPDATE statements), the index pays off as soon as content grows.
--
-- V16 only adds columns + FKs + indexes. V17 ships:
--   - 15 INSERT statements creating the Universe rows from the §3.2 audit
--   - UPDATE statements retagging existing fiction-canon entries to point
--     at the appropriate universe_id
-- The split keeps V16 self-contained as "schema extension" and V17 as
-- "data migration" — easier to review, easier to roll back conceptually.
--
-- Idempotent via IF NOT EXISTS; forward-only; no down migration.
-- ---------------------------------------------------------------------------

-- ----- station_design -----
ALTER TABLE station_design
    ADD COLUMN IF NOT EXISTS universe_id VARCHAR(64) DEFAULT NULL;
ALTER TABLE station_design
    ADD CONSTRAINT IF NOT EXISTS fk_station_design_universe
    FOREIGN KEY (universe_id) REFERENCES universe(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_station_design_universe_id
    ON station_design (universe_id);

-- ----- weapon_installation -----
ALTER TABLE weapon_installation
    ADD COLUMN IF NOT EXISTS universe_id VARCHAR(64) DEFAULT NULL;
ALTER TABLE weapon_installation
    ADD CONSTRAINT IF NOT EXISTS fk_weapon_installation_universe
    FOREIGN KEY (universe_id) REFERENCES universe(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_weapon_installation_universe_id
    ON weapon_installation (universe_id);

-- ----- megastructure -----
ALTER TABLE megastructure
    ADD COLUMN IF NOT EXISTS universe_id VARCHAR(64) DEFAULT NULL;
ALTER TABLE megastructure
    ADD CONSTRAINT IF NOT EXISTS fk_megastructure_universe
    FOREIGN KEY (universe_id) REFERENCES universe(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_megastructure_universe_id
    ON megastructure (universe_id);

-- ----- spaceship_design -----
ALTER TABLE spaceship_design
    ADD COLUMN IF NOT EXISTS universe_id VARCHAR(64) DEFAULT NULL;
ALTER TABLE spaceship_design
    ADD CONSTRAINT IF NOT EXISTS fk_spaceship_design_universe
    FOREIGN KEY (universe_id) REFERENCES universe(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_spaceship_design_universe_id
    ON spaceship_design (universe_id);

-- ----- gate_network -----
ALTER TABLE gate_network
    ADD COLUMN IF NOT EXISTS universe_id VARCHAR(64) DEFAULT NULL;
ALTER TABLE gate_network
    ADD CONSTRAINT IF NOT EXISTS fk_gate_network_universe
    FOREIGN KEY (universe_id) REFERENCES universe(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_gate_network_universe_id
    ON gate_network (universe_id);

-- ----- transport_node -----
ALTER TABLE transport_node
    ADD COLUMN IF NOT EXISTS universe_id VARCHAR(64) DEFAULT NULL;
ALTER TABLE transport_node
    ADD CONSTRAINT IF NOT EXISTS fk_transport_node_universe
    FOREIGN KEY (universe_id) REFERENCES universe(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_transport_node_universe_id
    ON transport_node (universe_id);
