-- ---------------------------------------------------------------------------
-- V15__universe_table.sql
-- Worldbuilding Platform Phase F.1 §4.2 (see
-- docs/design/phase-f1-worldbuilding-universes-foundation.md).
--
-- Introduces JPA persistence for Universe, the foundational entity of the
-- Worldbuilding Platform. A Universe is a bounded collection of fiction-
-- specific content; catalog entries reference a Universe via the universe_id
-- FK added in V16. Entries with universe_id = NULL are canonical/real-data
-- and always visible regardless of activation state.
--
-- Pipeline parallels the existing catalog families:
--   UniverseEntity, UniverseMapper, UniverseRepository,
--   UniverseDesignerService, UniverseSeeder.
--
-- The seeder is vacuous in F.1 — Catalog ships zero canonical Universe
-- constants in this phase. The 15 actual Universe rows ship via V16's
-- INSERT statements (V16 also adds the universe_id FK columns to the 6
-- catalog tables and UPDATEs existing fiction-canon entries to point at
-- the appropriate universe). V15 is intentionally narrow — just the table —
-- so the JPA entity pipeline can ship in Phase F.1 Step 2 while the
-- column-additions + data migration land separately in Step 4.
--
-- Schema notes:
--   - No collection columns (no JSON LOBs) — Universe has no Set/List fields.
--   - No CatalogProvenance columns — Universe IS the provenance scope; see
--     the Universe javadoc.
--   - 'active' column persists activation state across application restarts
--     (R5.2). Indexed for cheap filtering in UniverseFilteringService.
--   - L2 cache NOT enabled (no read pressure predicted at F.1 scale).
--
-- Idempotent via IF NOT EXISTS; forward-only; no down migration.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS universe (
    id              VARCHAR(64)  NOT NULL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     VARCHAR(4000),
    source_author   VARCHAR(255),
    version         VARCHAR(32)  NOT NULL DEFAULT '1.0',
    lifecycle       VARCHAR(32)  NOT NULL DEFAULT 'AVAILABLE',
    active          BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    modified_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_universe_name
    ON universe (name ASC);

CREATE INDEX IF NOT EXISTS idx_universe_lifecycle
    ON universe (lifecycle);

CREATE INDEX IF NOT EXISTS idx_universe_active
    ON universe (active);
