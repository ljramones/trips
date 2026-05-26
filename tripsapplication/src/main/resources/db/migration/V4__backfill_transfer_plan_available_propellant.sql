-- ---------------------------------------------------------------------------
-- V4__backfill_transfer_plan_available_propellant.sql
-- Phase 1.4 of the codebase-review remediation.
--
-- The Spaceship Modeller phase 12 added `available_propellant_tons` to
-- TransferPlan and used it to compute three-level feasibility (FEASIBLE /
-- MARGINAL / INSUFFICIENT). Under ddl-auto=update the column was added with
-- a default of 0 for any row that pre-dated the schema change, so every
-- legacy plan now displays INSUFFICIENT even though it was saved as feasible
-- at the time.
--
-- This backfill recovers a best-effort value for those rows: if a plan was
-- saved, its required propellant must have been considered available at the
-- time, so we set `available_propellant_tons` equal to `total_propellant_tons`.
-- Result: legacy plans show as MARGINAL (using "all available propellant")
-- rather than INSUFFICIENT — a more honest report of the historical state.
--
-- Touches only rows that look like legacy defaults (available=0 with a
-- positive total). Re-running is a no-op (no rows match after first
-- application).
-- ---------------------------------------------------------------------------

UPDATE transfer_plan
SET available_propellant_tons = total_propellant_tons
WHERE available_propellant_tons = 0
  AND total_propellant_tons > 0;
