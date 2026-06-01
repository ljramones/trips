-- ---------------------------------------------------------------------------
-- V17__universe_seed_and_catalog_retagging.sql
-- Worldbuilding Platform Phase F.1 Step 4 (see
-- docs/design/phase-f1-worldbuilding-universes-foundation.md §4.5 + §3.2).
--
-- The data migration. Two parts:
--
-- Part A: INSERT 13 Universe rows. Two get curated metadata (Legacy of the
-- Aldenata, Caine Riordan — the "first-class" examples per design doc §1.2);
-- eleven get auto-generated descriptions ("thin" universes auto-seeded from
-- existing catalog entries — see §3.2). All ship with active=FALSE so a
-- fresh installation is real-only (R1.8).
--
-- Part B: UPDATE existing catalog rows to point universe_id at the appropriate
-- Universe row, scoped by §3.2's audit mapping table. The §3.2 mapping has
-- three concept levels baked into the single sourceUniverse String:
--   1. Universe names ("Star Trek", "Foundation", "Mass Effect")
--   2. Faction names ("Hkh'Rkh", "MCRN", "Starfleet" — scoped within a universe)
--   3. Era names ("Imperial era", "Late Seldon era" — scoped within a universe)
--
-- All three concept levels share the same FK target (the Universe row); F.3
-- and F.4 will extract Faction and Era entities respectively. For F.1, the
-- mapping is "any of these strings -> this Universe."
--
-- Per-table column differences:
--   - spaceship_design uses `source_universe` (pre-D.6 legacy column;
--     SpaceshipEntity didn't get the D.6 provenance refactor)
--   - station_design, megastructure, gate_network use `provenance_source_universe`
--     (D.6 provenance columns)
--   - weapon_installation uses `source` (didn't get the D.6 refactor; the
--     source string IS the universe label there)
--   - transport_node uses `faction` (no rows today; UPDATE is a no-op)
--
-- Idempotency: every UPDATE has `WHERE universe_id IS NULL` so re-running
-- doesn't double-tag. Matches the D.8 sync-by-id discipline and E.1's V12
-- pattern.
--
-- Ambiguous strings stay NULL (canonical/real visibility, R1.9):
--   - "UN Navy", "United Earth (UEG)", "United Earth" — Caine Riordan or real
--   - "NASA", "Near future", "United States", "Earth" — real
--   - "Independent" — generic
--   - "Source universe or setting for this design." — documentation accident
--   These remain visible regardless of activation state.
--
-- Step 1 audit addendum: "Troy Rising" was missed in the original §3.2 audit
-- because the grep didn't catch positional CatalogProvenance constructor
-- arguments. It's the actual sourceUniverse string used by Catalog.TROY +
-- the Posleen Dodecahedra. Maps to Legacy of the Aldenata.
--
-- FlywayBaselineSmokeTest stays green — V17 only mutates data; no schema
-- changes. ddl-auto=validate doesn't care about row contents.
--
-- Forward-only; no down migration.
-- ---------------------------------------------------------------------------

-- =============================================================================
-- PART A: INSERT the 13 Universe rows
-- =============================================================================

-- ----- 2 first-class universes (curated metadata) -----

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-legacy-of-the-aldenata', 'Legacy of the Aldenata',
       'John Ringo''s Posleen War setting. Includes Troy (hollowed-asteroid command megastructure), SAPL elements, SheVa Gun, Posleen Battle and Command Dodecahedra, and the fictional Posleen interstellar drive. Spans the Aldenata-Posleen conflict and post-war reconstruction.',
       'John Ringo', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-legacy-of-the-aldenata');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-caine-riordan', 'Caine Riordan',
       'Charles Gannon''s Terran Republic / Caine Riordan setting. Includes Hkh''Rkh, Slaasriithi, Arat Kur Wholenest, Dornaani Collective, Ktoran Dominion factions; Grtul Gates; the GALACTIC_HYPER, KTORAN_ADVANCED, HKHRKH_THRUST, and GRTUL_GATE drive types. Spans first contact through the early Terran Republic era.',
       'Charles Gannon', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-caine-riordan');

-- ----- 11 thin universes (auto-generated description) -----

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-battlestar-galactica', 'Battlestar Galactica',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-battlestar-galactica');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-firefly', 'Firefly',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-firefly');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-foundation', 'Foundation',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-foundation');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-honor-harrington', 'Honor Harrington',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-honor-harrington');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-mass-effect', 'Mass Effect',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-mass-effect');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-project-hail-mary', 'Project Hail Mary',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-project-hail-mary');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-star-trek', 'Star Trek',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-star-trek');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-star-wars', 'Star Wars',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-star-wars');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-the-expanse', 'The Expanse',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-the-expanse');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-the-hitchhikers-guide-to-the-galaxy', 'The Hitchhiker''s Guide to the Galaxy',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-the-hitchhikers-guide-to-the-galaxy');

INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at)
SELECT 'catalog-universe-the-martian', 'The Martian',
       'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).',
       '', '1.0', 'AVAILABLE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM universe WHERE id = 'catalog-universe-the-martian');

-- =============================================================================
-- PART B: UPDATE existing catalog rows to point at the appropriate Universe
-- =============================================================================
--
-- Per universe, scoped by §3.2 mapping. Each UPDATE is idempotent
-- (WHERE universe_id IS NULL). Per-table column names differ — see the
-- file header for the rationale.

-- ----- catalog-universe-legacy-of-the-aldenata -----
-- "Troy Rising" is the actual Catalog.TROY/Posleen-content sourceUniverse
-- string (per Step 4 addendum to the §3.2 audit).

UPDATE spaceship_design SET universe_id = 'catalog-universe-legacy-of-the-aldenata'
WHERE universe_id IS NULL AND source_universe IN ('Troy Rising', 'Legacy of the Aldenata');

UPDATE megastructure SET universe_id = 'catalog-universe-legacy-of-the-aldenata'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Troy Rising', 'Legacy of the Aldenata');

UPDATE station_design SET universe_id = 'catalog-universe-legacy-of-the-aldenata'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Troy Rising', 'Legacy of the Aldenata');

UPDATE weapon_installation SET universe_id = 'catalog-universe-legacy-of-the-aldenata'
WHERE universe_id IS NULL AND source IN ('Troy Rising', 'Legacy of the Aldenata');

UPDATE gate_network SET universe_id = 'catalog-universe-legacy-of-the-aldenata'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Troy Rising', 'Legacy of the Aldenata');

-- ----- catalog-universe-caine-riordan -----
-- Includes the Caine Riordan factions (Hkh'Rkh, Slaasriithi, etc.) +
-- the Terran Republic faction labels + the Lost Soldiers sub-series.

UPDATE spaceship_design SET universe_id = 'catalog-universe-caine-riordan'
WHERE universe_id IS NULL AND source_universe IN (
    'Caine Riordan', 'Terran Republic', 'IRIS / Terran Republic',
    'Hkh''Rkh', 'Slaasriithi', 'Arat Kur Wholenest',
    'Dornaani Collective', 'Ktoran Dominion',
    'Roaches', 'Gok', 'SpinDog', 'RockHound',
    'Lost Soldiers', 'Lost Soldiers Era'
);

UPDATE megastructure SET universe_id = 'catalog-universe-caine-riordan'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'Caine Riordan', 'Terran Republic', 'IRIS / Terran Republic',
    'Hkh''Rkh', 'Slaasriithi', 'Arat Kur Wholenest',
    'Dornaani Collective', 'Ktoran Dominion',
    'Roaches', 'Gok', 'SpinDog', 'RockHound',
    'Lost Soldiers', 'Lost Soldiers Era'
);

UPDATE station_design SET universe_id = 'catalog-universe-caine-riordan'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'Caine Riordan', 'Terran Republic', 'IRIS / Terran Republic',
    'Hkh''Rkh', 'Slaasriithi', 'Arat Kur Wholenest',
    'Dornaani Collective', 'Ktoran Dominion',
    'Roaches', 'Gok', 'SpinDog', 'RockHound',
    'Lost Soldiers', 'Lost Soldiers Era'
);

UPDATE weapon_installation SET universe_id = 'catalog-universe-caine-riordan'
WHERE universe_id IS NULL AND source IN (
    'Caine Riordan', 'Terran Republic', 'IRIS / Terran Republic',
    'Hkh''Rkh', 'Slaasriithi', 'Arat Kur Wholenest',
    'Dornaani Collective', 'Ktoran Dominion',
    'Roaches', 'Gok', 'SpinDog', 'RockHound',
    'Lost Soldiers', 'Lost Soldiers Era'
);

UPDATE gate_network SET universe_id = 'catalog-universe-caine-riordan'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'Caine Riordan', 'Terran Republic', 'IRIS / Terran Republic',
    'Hkh''Rkh', 'Slaasriithi', 'Arat Kur Wholenest',
    'Dornaani Collective', 'Ktoran Dominion',
    'Roaches', 'Gok', 'SpinDog', 'RockHound',
    'Lost Soldiers', 'Lost Soldiers Era'
);

-- ----- catalog-universe-battlestar-galactica -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-battlestar-galactica'
WHERE universe_id IS NULL AND source_universe IN ('Battlestar Galactica', 'Colonial Fleet', 'Second Cylon War');

UPDATE station_design SET universe_id = 'catalog-universe-battlestar-galactica'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Battlestar Galactica', 'Colonial Fleet', 'Second Cylon War');

UPDATE weapon_installation SET universe_id = 'catalog-universe-battlestar-galactica'
WHERE universe_id IS NULL AND source IN ('Battlestar Galactica', 'Colonial Fleet', 'Second Cylon War');

UPDATE megastructure SET universe_id = 'catalog-universe-battlestar-galactica'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Battlestar Galactica', 'Colonial Fleet', 'Second Cylon War');

UPDATE gate_network SET universe_id = 'catalog-universe-battlestar-galactica'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Battlestar Galactica', 'Colonial Fleet', 'Second Cylon War');

-- ----- catalog-universe-firefly -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-firefly'
WHERE universe_id IS NULL AND source_universe = 'Firefly';

UPDATE station_design SET universe_id = 'catalog-universe-firefly'
WHERE universe_id IS NULL AND provenance_source_universe = 'Firefly';

UPDATE weapon_installation SET universe_id = 'catalog-universe-firefly'
WHERE universe_id IS NULL AND source = 'Firefly';

UPDATE megastructure SET universe_id = 'catalog-universe-firefly'
WHERE universe_id IS NULL AND provenance_source_universe = 'Firefly';

UPDATE gate_network SET universe_id = 'catalog-universe-firefly'
WHERE universe_id IS NULL AND provenance_source_universe = 'Firefly';

-- ----- catalog-universe-foundation -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-foundation'
WHERE universe_id IS NULL AND source_universe IN (
    'Foundation', 'Foundation Traders',
    'Late Seldon era', 'Cleon dynasty', 'Interregnum', 'The Mule', 'Early Foundation era'
);

UPDATE station_design SET universe_id = 'catalog-universe-foundation'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'Foundation', 'Foundation Traders',
    'Late Seldon era', 'Cleon dynasty', 'Interregnum', 'The Mule', 'Early Foundation era'
);

UPDATE weapon_installation SET universe_id = 'catalog-universe-foundation'
WHERE universe_id IS NULL AND source IN (
    'Foundation', 'Foundation Traders',
    'Late Seldon era', 'Cleon dynasty', 'Interregnum', 'The Mule', 'Early Foundation era'
);

UPDATE megastructure SET universe_id = 'catalog-universe-foundation'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'Foundation', 'Foundation Traders',
    'Late Seldon era', 'Cleon dynasty', 'Interregnum', 'The Mule', 'Early Foundation era'
);

UPDATE gate_network SET universe_id = 'catalog-universe-foundation'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'Foundation', 'Foundation Traders',
    'Late Seldon era', 'Cleon dynasty', 'Interregnum', 'The Mule', 'Early Foundation era'
);

-- ----- catalog-universe-honor-harrington -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-honor-harrington'
WHERE universe_id IS NULL AND source_universe IN ('Honor Harrington', 'Royal Manticoran Navy');

UPDATE station_design SET universe_id = 'catalog-universe-honor-harrington'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Honor Harrington', 'Royal Manticoran Navy');

UPDATE weapon_installation SET universe_id = 'catalog-universe-honor-harrington'
WHERE universe_id IS NULL AND source IN ('Honor Harrington', 'Royal Manticoran Navy');

UPDATE megastructure SET universe_id = 'catalog-universe-honor-harrington'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Honor Harrington', 'Royal Manticoran Navy');

UPDATE gate_network SET universe_id = 'catalog-universe-honor-harrington'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Honor Harrington', 'Royal Manticoran Navy');

-- ----- catalog-universe-mass-effect -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-mass-effect'
WHERE universe_id IS NULL AND source_universe IN ('Mass Effect', 'Cerberus / Systems Alliance');

UPDATE station_design SET universe_id = 'catalog-universe-mass-effect'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Mass Effect', 'Cerberus / Systems Alliance');

UPDATE weapon_installation SET universe_id = 'catalog-universe-mass-effect'
WHERE universe_id IS NULL AND source IN ('Mass Effect', 'Cerberus / Systems Alliance');

UPDATE megastructure SET universe_id = 'catalog-universe-mass-effect'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Mass Effect', 'Cerberus / Systems Alliance');

UPDATE gate_network SET universe_id = 'catalog-universe-mass-effect'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Mass Effect', 'Cerberus / Systems Alliance');

-- ----- catalog-universe-project-hail-mary -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-project-hail-mary'
WHERE universe_id IS NULL AND source_universe = 'Project Hail Mary';

UPDATE station_design SET universe_id = 'catalog-universe-project-hail-mary'
WHERE universe_id IS NULL AND provenance_source_universe = 'Project Hail Mary';

UPDATE weapon_installation SET universe_id = 'catalog-universe-project-hail-mary'
WHERE universe_id IS NULL AND source = 'Project Hail Mary';

UPDATE megastructure SET universe_id = 'catalog-universe-project-hail-mary'
WHERE universe_id IS NULL AND provenance_source_universe = 'Project Hail Mary';

UPDATE gate_network SET universe_id = 'catalog-universe-project-hail-mary'
WHERE universe_id IS NULL AND provenance_source_universe = 'Project Hail Mary';

-- ----- catalog-universe-star-trek -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-star-trek'
WHERE universe_id IS NULL AND source_universe IN ('Star Trek', 'Starfleet');

UPDATE station_design SET universe_id = 'catalog-universe-star-trek'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Star Trek', 'Starfleet');

UPDATE weapon_installation SET universe_id = 'catalog-universe-star-trek'
WHERE universe_id IS NULL AND source IN ('Star Trek', 'Starfleet');

UPDATE megastructure SET universe_id = 'catalog-universe-star-trek'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Star Trek', 'Starfleet');

UPDATE gate_network SET universe_id = 'catalog-universe-star-trek'
WHERE universe_id IS NULL AND provenance_source_universe IN ('Star Trek', 'Starfleet');

-- ----- catalog-universe-star-wars -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-star-wars'
WHERE universe_id IS NULL AND source_universe IN (
    'Star Wars', 'Rebel Alliance', 'Galactic Empire', 'Galactic Civil War', 'Imperial era'
);

UPDATE station_design SET universe_id = 'catalog-universe-star-wars'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'Star Wars', 'Rebel Alliance', 'Galactic Empire', 'Galactic Civil War', 'Imperial era'
);

UPDATE weapon_installation SET universe_id = 'catalog-universe-star-wars'
WHERE universe_id IS NULL AND source IN (
    'Star Wars', 'Rebel Alliance', 'Galactic Empire', 'Galactic Civil War', 'Imperial era'
);

UPDATE megastructure SET universe_id = 'catalog-universe-star-wars'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'Star Wars', 'Rebel Alliance', 'Galactic Empire', 'Galactic Civil War', 'Imperial era'
);

UPDATE gate_network SET universe_id = 'catalog-universe-star-wars'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'Star Wars', 'Rebel Alliance', 'Galactic Empire', 'Galactic Civil War', 'Imperial era'
);

-- ----- catalog-universe-the-expanse -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-the-expanse'
WHERE universe_id IS NULL AND source_universe IN (
    'The Expanse', 'MCRN', 'OPA', 'Free Navy', 'Tycho / OPA',
    'Pur''n''Kleen Water Co.', 'MCRN / independent',
    'First Contact (2105)', 'First Contact era', 'Post-Contact', 'Pre-Epstein era'
);

UPDATE station_design SET universe_id = 'catalog-universe-the-expanse'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'The Expanse', 'MCRN', 'OPA', 'Free Navy', 'Tycho / OPA',
    'Pur''n''Kleen Water Co.', 'MCRN / independent',
    'First Contact (2105)', 'First Contact era', 'Post-Contact', 'Pre-Epstein era'
);

UPDATE weapon_installation SET universe_id = 'catalog-universe-the-expanse'
WHERE universe_id IS NULL AND source IN (
    'The Expanse', 'MCRN', 'OPA', 'Free Navy', 'Tycho / OPA',
    'Pur''n''Kleen Water Co.', 'MCRN / independent',
    'First Contact (2105)', 'First Contact era', 'Post-Contact', 'Pre-Epstein era'
);

UPDATE megastructure SET universe_id = 'catalog-universe-the-expanse'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'The Expanse', 'MCRN', 'OPA', 'Free Navy', 'Tycho / OPA',
    'Pur''n''Kleen Water Co.', 'MCRN / independent',
    'First Contact (2105)', 'First Contact era', 'Post-Contact', 'Pre-Epstein era'
);

UPDATE gate_network SET universe_id = 'catalog-universe-the-expanse'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'The Expanse', 'MCRN', 'OPA', 'Free Navy', 'Tycho / OPA',
    'Pur''n''Kleen Water Co.', 'MCRN / independent',
    'First Contact (2105)', 'First Contact era', 'Post-Contact', 'Pre-Epstein era'
);

-- ----- catalog-universe-the-hitchhikers-guide-to-the-galaxy -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-the-hitchhikers-guide-to-the-galaxy'
WHERE universe_id IS NULL AND source_universe IN (
    'The Hitchhiker''s Guide to the Galaxy', 'Galactic Government'
);

UPDATE station_design SET universe_id = 'catalog-universe-the-hitchhikers-guide-to-the-galaxy'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'The Hitchhiker''s Guide to the Galaxy', 'Galactic Government'
);

UPDATE weapon_installation SET universe_id = 'catalog-universe-the-hitchhikers-guide-to-the-galaxy'
WHERE universe_id IS NULL AND source IN (
    'The Hitchhiker''s Guide to the Galaxy', 'Galactic Government'
);

UPDATE megastructure SET universe_id = 'catalog-universe-the-hitchhikers-guide-to-the-galaxy'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'The Hitchhiker''s Guide to the Galaxy', 'Galactic Government'
);

UPDATE gate_network SET universe_id = 'catalog-universe-the-hitchhikers-guide-to-the-galaxy'
WHERE universe_id IS NULL AND provenance_source_universe IN (
    'The Hitchhiker''s Guide to the Galaxy', 'Galactic Government'
);

-- ----- catalog-universe-the-martian -----

UPDATE spaceship_design SET universe_id = 'catalog-universe-the-martian'
WHERE universe_id IS NULL AND source_universe = 'The Martian';

UPDATE station_design SET universe_id = 'catalog-universe-the-martian'
WHERE universe_id IS NULL AND provenance_source_universe = 'The Martian';

UPDATE weapon_installation SET universe_id = 'catalog-universe-the-martian'
WHERE universe_id IS NULL AND source = 'The Martian';

UPDATE megastructure SET universe_id = 'catalog-universe-the-martian'
WHERE universe_id IS NULL AND provenance_source_universe = 'The Martian';

UPDATE gate_network SET universe_id = 'catalog-universe-the-martian'
WHERE universe_id IS NULL AND provenance_source_universe = 'The Martian';

-- transport_node intentionally omitted — table is empty in F.1 per V9 header.
-- When canonical transport nodes ship in a future phase, the seeder will set
-- universe_id directly rather than relying on a UPDATE-by-faction-string pattern.
