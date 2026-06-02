-- ---------------------------------------------------------------------------
-- V19__drop_legacy_worldbuilding_columns.sql
-- Worldbuilding Data Model Normalization task (see
-- docs/design/worldbuilding-data-model-normalization.md).
--
-- Removes the legacy universe-agnostic worldbuilding fields that were
-- embedded in the astronomical entities StarObject and ExoPlanet. These
-- predated the F.1 Worldbuilding Platform (Universe + activation +
-- filtering chokepoint) and F.2 Aliases work, and conflated real
-- astronomical data with universe-scoped fictional metadata.
--
-- Per the task's §1.1 reframe: every affected feature is a worldbuilding
-- feature wrongly modeled as universe-agnostic astronomical metadata. The
-- columns drop here; F.3+ phases reintroduce proper universe-scoped
-- equivalents (FactionAssignment for polity, F.6 PopulationAssignment for
-- population, F.7 TechAssignment for techLevel, etc.).
--
-- Forward-only and destructive. Existing column values are lost. ChView-
-- imported polity values are the most populated; their loss is a known
-- regression and the forcing function for F.3 to reintroduce via
-- FactionAssignment auto-seeding.
--
-- Column names match V1__baseline.sql verbatim (snake_case per standard
-- JPA convention; no @Column(name=...) overrides exist on these fields).
-- Verified during Step 1 audit (§13.5).
--
-- Fields that STAY on these tables (preserved per §1.4):
--   StarObject.notes        (workflow — user observations)
--   StarObject.source       (provenance — import source)
--   StarObject.alias_list   (catalog identifier variants — Simbad/Bayer/
--                            HIP — distinct from F.2 Alias entity)
--   StarObject.real_star    (astronomical — real catalog vs procedural)
--   ExoPlanet.notes         (workflow)
--   ExoPlanet.publication   (provenance — scientific paper)
--   ExoPlanet.detection_type, mass_detection_type, radius_detection_type
--                           (provenance — detection method)
--   ExoPlanet.alternate_names (catalog identifiers)
--   All ExoPlanet atmospheric/physical/orbital fields (astronomical)
--
-- ---------------------------------------------------------------------------

-- Drop StarObject legacy worldbuilding columns (the 11 StarWorldBuilding
-- @Embedded fields). The Java field group + Java accessors were removed in
-- the same commit; the StarWorldBuilding class is gone.
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS polity;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS world_type;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS fuel_type;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS port_type;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS population_type;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS tech_type;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS product_type;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS mil_space_type;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS mil_plan_type;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS other;
ALTER TABLE STAR_OBJ DROP COLUMN IF EXISTS anomaly;

-- Drop ExoPlanet sci-fi worldbuilding columns (7 fields).
ALTER TABLE EXOPLANET DROP COLUMN IF EXISTS population;
ALTER TABLE EXOPLANET DROP COLUMN IF EXISTS tech_level;
ALTER TABLE EXOPLANET DROP COLUMN IF EXISTS colonized;
ALTER TABLE EXOPLANET DROP COLUMN IF EXISTS colonization_year;
ALTER TABLE EXOPLANET DROP COLUMN IF EXISTS polity;
ALTER TABLE EXOPLANET DROP COLUMN IF EXISTS strategic_importance;
ALTER TABLE EXOPLANET DROP COLUMN IF EXISTS primary_resource;

-- SolarSystem polity propagation removed too (the SolarSystem.polity field
-- + fromStar() propagation were deleted in the Java cleanup).
ALTER TABLE SOLAR_SYSTEM DROP COLUMN IF EXISTS polity;
