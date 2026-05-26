-- ---------------------------------------------------------------------------
-- V3__drop_spaceship_design_series.sql
-- Phase 1.4 of the codebase-review remediation.
--
-- The Spaceship Modeller phase 18 (provenance restructure) replaced the
-- single `series` text column on SpaceshipDesign with three structured
-- columns: source_universe, faction, era. Under ddl-auto=update the new
-- columns were added but `series` was left orphaned in the schema — present
-- in legacy DBs, absent from V1__baseline.sql (since the entity model no
-- longer references it).
--
-- This migration drops the orphan column. IF EXISTS makes the change a
-- no-op on fresh installs where V1 already produced the clean schema.
-- ---------------------------------------------------------------------------

ALTER TABLE spaceship_design DROP COLUMN IF EXISTS series;
