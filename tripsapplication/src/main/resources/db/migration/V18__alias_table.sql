-- ---------------------------------------------------------------------------
-- V18__alias_table.sql
-- Worldbuilding Platform Phase F.2 §4.4 (see
-- docs/design/phase-f2-aliases.md).
--
-- Introduces JPA persistence for Alias — the universe-scoped fictional-name
-- overlay on real astronomical targets (StarObject or ExoPlanet). Phase F.2
-- is the first F-series phase to add a universe-required (not universe-
-- optional) entity: every Alias must reference an active Universe row, and
-- deleting a Universe deletes all its aliases (ON DELETE CASCADE).
--
-- Pipeline parallels GateNetwork (E.1) and Universe (F.1):
--   AliasEntity, AliasMapper, AliasRepository, AliasDesignerService.
--
-- Polymorphic target reference: (target_kind, target_id) discriminator +
-- scalar id, mirroring SolarSystemFeature.catalogReferenceKind/Id from
-- E.1 Step 4 and CatalogedKind from F.1. AliasTargetKind values:
--   STAR      — target_id references star_obj.id
--   EXOPLANET — target_id references exoplanet.id (REAL or PROMOTED-ACRETE)
--
-- Uniqueness invariant (F.2 §1.5): one alias per (universe, target) pair.
-- Enforced by uk_alias_universe_target unique constraint. The Star Trek
-- universe can't have two distinct Alias rows for the same star — to
-- represent multiple names (e.g. "Vulcan / T'Khut"), users put them in
-- the aliasText field as separated text.
--
-- FK to universe(id) with ON DELETE CASCADE — distinct from F.1's catalog
-- tables (V16) which use SET NULL because catalog entries can exist
-- canonically without a universe. Aliases cannot; an alias without a
-- universe is semantically meaningless.
--
-- No seed data. Empty table at F.2 ship. Users create aliases via the
-- Worldbuilding > Aliases dialog (lands in Step 5).
--
-- Idempotent via IF NOT EXISTS; forward-only; no down migration.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS alias (
    id              VARCHAR(64)  NOT NULL PRIMARY KEY,
    universe_id     VARCHAR(64)  NOT NULL,
    target_kind     VARCHAR(32)  NOT NULL,
    target_id       VARCHAR(64)  NOT NULL,
    alias_text      VARCHAR(255) NOT NULL,
    description     VARCHAR(1000),
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    modified_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_alias_universe
        FOREIGN KEY (universe_id) REFERENCES universe(id) ON DELETE CASCADE,
    CONSTRAINT uk_alias_universe_target
        UNIQUE (universe_id, target_kind, target_id)
);

CREATE INDEX IF NOT EXISTS idx_alias_universe
    ON alias (universe_id);

CREATE INDEX IF NOT EXISTS idx_alias_target
    ON alias (target_kind, target_id);
