-- ---------------------------------------------------------------------------
-- V6__spaceship_entity_concealed_and_operational_state.sql
-- Phase A0 of the Constructs feature (see docs/design/).
--
-- Background: the SpaceshipDesign record carries two fields the
-- SpaceshipDesignMapper has been silently dropping on persist since the
-- module was built:
--   * concealed       (boolean)  — entirely lost
--   * operationalState (enum)    — reconstituted as OPERATIONAL by the
--                                   record's compact constructor
--
-- The drop was harmless until the new Construct subtypes (Station,
-- WeaponInstallation, TransportNode) were about to inherit the same mapper
-- pattern in v2 Phase A. Phase A0 fixes the bug on the existing entity so
-- the new entities don't inherit the defect.
--
-- This migration adds the two missing columns. Both are NOT NULL with
-- defaults that match the pre-V6 reconstituted values, so existing rows
-- (which had no concealed flag and were silently coerced to OPERATIONAL on
-- read) surface as concealed=false / operational_state='OPERATIONAL' —
-- behaviour-preserving for legacy data.
--
-- Idempotent via IF NOT EXISTS: re-running this migration on a DB that
-- already has the columns is a no-op. Forward-only; no down migration.
-- ---------------------------------------------------------------------------

ALTER TABLE spaceship_design
    ADD COLUMN IF NOT EXISTS concealed BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE spaceship_design
    ADD COLUMN IF NOT EXISTS operational_state VARCHAR(64) NOT NULL DEFAULT 'OPERATIONAL';
