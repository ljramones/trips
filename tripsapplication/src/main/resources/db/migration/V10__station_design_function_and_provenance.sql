-- ---------------------------------------------------------------------------
-- V10__station_design_function_and_provenance.sql
-- Constructs feature v2 Phase D.6 (see docs/design/constructs-feature-plan-v2.md
-- and space-assets-functional-taxonomy-v2.md).
--
-- Adds the function and provenance axes to STATION_DESIGN. Three logical fields
-- land in six physical columns:
--
--   primaryFunction (StationFunction)
--     → primary_function VARCHAR(64) NOT NULL DEFAULT 'UNKNOWN'
--
--   secondaryFunctions (Set<StationFunction>)
--     → secondary_functions_json CLOB (nullable; Jackson-serialised; empty
--       set persists as '[]' or null per existing LOB pattern)
--
--   provenance (CatalogProvenance composite)
--     → provenance_source_type      VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN'
--     → provenance_source_universe  VARCHAR(255) NOT NULL DEFAULT ''
--     → provenance_source_work      VARCHAR(255) nullable
--     → provenance_status           VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN'
--
-- Provenance is decomposed into flat columns (not a single JSON blob) because
-- v2 §7 picked it as "small and frequently filtered on" — the Installations
-- Designer panel's universe filter and the future function filter both query
-- against these columns directly. The v2 §4 Issue 46 LazyInitializationException
-- analysis discouraged whole-entity payload_json CLOBs; per-field flat columns
-- avoid that pitfall.
--
-- The existing source VARCHAR(255) column stays in place. v2 Phase D.6 dropped
-- the dedicated source field from StationDesign in favour of
-- provenance.sourceUniverse, but the column is kept as a legacy mirror — the
-- mapper continues to write the same string to both, and on first run after
-- this migration the UPDATE below copies pre-existing source values into the
-- new provenance_source_universe column so no row's universe label is lost.
--
-- Indexes on the two query-relevant columns (primary_function for the future
-- function filter, provenance_source_universe for the Phase D.5 tab strip's
-- next-generation read path).
--
-- Idempotent via IF NOT EXISTS on every statement. Forward-only; no down.
-- ---------------------------------------------------------------------------

ALTER TABLE station_design
    ADD COLUMN IF NOT EXISTS primary_function VARCHAR(64) NOT NULL DEFAULT 'UNKNOWN';

ALTER TABLE station_design
    ADD COLUMN IF NOT EXISTS secondary_functions_json CLOB;

ALTER TABLE station_design
    ADD COLUMN IF NOT EXISTS provenance_source_type VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN';

ALTER TABLE station_design
    ADD COLUMN IF NOT EXISTS provenance_source_universe VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE station_design
    ADD COLUMN IF NOT EXISTS provenance_source_work VARCHAR(255);

ALTER TABLE station_design
    ADD COLUMN IF NOT EXISTS provenance_status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN';

-- Backfill the new provenance_source_universe column from the legacy source
-- column for rows that exist at V10 time. After this UPDATE, the new column
-- carries the universe label for every pre-existing row; future writes set
-- both columns in lock-step via StationDesignMapper. Safe to run repeatedly
-- though Flyway will only run V10 once.
UPDATE station_design
   SET provenance_source_universe = COALESCE(source, '')
 WHERE provenance_source_universe = '';

CREATE INDEX IF NOT EXISTS idx_station_design_primary_function
    ON station_design (primary_function);

CREATE INDEX IF NOT EXISTS idx_station_design_provenance_source_universe
    ON station_design (provenance_source_universe);
