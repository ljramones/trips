-- ---------------------------------------------------------------------------
-- V12__catalog_id_stabilization.sql
-- Constructs feature v2 Phase D.8 Step 2 (see
-- docs/design/constructs-d8-catalog-sync-and-megastructure-wiring.md §3.2.1).
--
-- Two transformations, one migration:
--
--   1. CLEANUP — delete legacy random-UUID rows whose canonical name matches
--      a Catalog entry. These are pre-D.8 rows seeded by the now-deprecated
--      seedFromCatalogIfEmpty() contract; after Step 1.5's stable-id preamble
--      they would otherwise coexist with the new "catalog-*"-id rows that
--      Step 4's syncCatalogEntries() will insert.
--
--   2. RENAME — update the eight D.5 real-station ids from "real-station-*"
--      to the universal "catalog-*" convention. After D.8, every Catalog
--      constant uses a single id pattern: "catalog-<lowercase-kebab-slug>".
--
-- The migration is forward-only, no down. Schema-stable (no DDL). Idempotent:
-- re-running on an already-migrated DB matches no DELETEs and no UPDATEs.
-- FlywayBaselineSmokeTest stays green because no entity↔schema relationships
-- change.
-- ---------------------------------------------------------------------------

-- ---------------------------------------------------------------------------
-- Block 1: Cleanup — delete legacy random-UUID rows by belt-and-braces predicate.
--
-- The predicate matches BOTH on the canonical Catalog name AND on the absence
-- of the new "catalog-*" / D.5 "real-station-*" id patterns. This deletes
-- only genuine legacy rows. A user who edited a row's description but left
-- the name untouched loses the row (recoverable: the seeder reinserts a
-- canonical "catalog-*" row on the next launch). A user who renamed Troy to
-- e.g. "Troy II" keeps their row (the name predicate falls through), and
-- ends up with a one-time redundant "catalog-troy" row — the correct
-- trade-off vs. wrongful deletion of user-edited data.
-- ---------------------------------------------------------------------------

DELETE FROM station_design
 WHERE id NOT LIKE 'catalog-%'
   AND id NOT LIKE 'real-station-%'
   AND name = 'Troy';

DELETE FROM weapon_installation
 WHERE id NOT LIKE 'catalog-%'
   AND name IN ('SAPL', 'SheVa Gun');

DELETE FROM spaceship_design
 WHERE id NOT LIKE 'catalog-%'
   AND name IN ('Posleen Command Dodecahedron', 'Posleen Battle Dodecahedron');

-- ---------------------------------------------------------------------------
-- Block 2: Rename — D.5 "real-station-*" ids to the universal "catalog-*"
-- convention. After this migration, every Catalog constant in JPA uses
-- "catalog-<slug>" as its id; the previous "real-station-*" namespace is
-- retired.
--
-- The Step 1.5 preamble updated Catalog.java to reference the new ids; the
-- D.5 real-station rows in pre-existing DBs still carry the old ids until
-- this UPDATE runs. The syncCatalogEntries() contract (Step 4) uses
-- existsById against the new ids, so without this rename the sync would
-- mistakenly insert duplicates of each D.5 station.
-- ---------------------------------------------------------------------------

UPDATE station_design SET id = 'catalog-iss'           WHERE id = 'real-station-iss';
UPDATE station_design SET id = 'catalog-tiangong'      WHERE id = 'real-station-tiangong';
UPDATE station_design SET id = 'catalog-mir'           WHERE id = 'real-station-mir';
UPDATE station_design SET id = 'catalog-skylab'        WHERE id = 'real-station-skylab';
UPDATE station_design SET id = 'catalog-salyut-1'      WHERE id = 'real-station-salyut-1';
UPDATE station_design SET id = 'catalog-salyut-7'      WHERE id = 'real-station-salyut-7';
UPDATE station_design SET id = 'catalog-lunar-gateway' WHERE id = 'real-station-lunar-gateway';
UPDATE station_design SET id = 'catalog-axiom'         WHERE id = 'real-station-axiom';
