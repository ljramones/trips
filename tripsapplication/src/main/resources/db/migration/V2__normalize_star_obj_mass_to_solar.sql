-- ---------------------------------------------------------------------------
-- V2__normalize_star_obj_mass_to_solar.sql
-- Phase 1.1 of the codebase-review remediation.
--
-- star_obj.mass was historically ingested as kilograms from CSV/ChView while
-- the entity's Javadoc claimed solar masses. The 30ly sample CSV is the
-- smoking gun — the Sun appears there as 1.99E30 (one solar mass in kg). The
-- workaround in TransferCalculator.toSolarMasses (a >1000 magnitude heuristic)
-- has been removed; the column is now canonically in solar masses (M☉).
--
-- This migration normalises legacy rows. Real stars top out around ~150 M☉,
-- so any value above 1000 must have been authored in kg.
--
-- Safe to re-run: after a single application no values exceed 1000, so the
-- WHERE clause matches zero rows on subsequent runs.
-- ---------------------------------------------------------------------------

UPDATE star_obj
SET mass = mass / 1.989E30
WHERE mass > 1000;
