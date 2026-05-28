-- ---------------------------------------------------------------------------
-- V9__transport_node_table.sql
-- Constructs feature v2 Phase B (see docs/design/constructs-feature-plan-v2.md).
--
-- Introduces JPA persistence for TransportNode, the first persisted subtype
-- of the SpaceInfrastructure sealed hierarchy. This unlocks the infrastructure
-- branch of ConstructRegistry (which had been returning empty for every
-- InfrastructureKind since Phase A0).
--
-- Schema mirrors com.teamgannon.trips.spaceshipmodeller.persistence
-- .TransportNodeEntity column-for-column; FlywayBaselineSmokeTest runs
-- ddl-auto=validate so any drift fails the smoke test rather than reaching
-- production.
--
-- The connectedNodeIds list is serialised to a JSON LOB by TransportNodeMapper.
-- The column intentionally has NO foreign key to transport_node.id —
-- the in-memory GraphRegistry retains dangling-id validation. v2 Phase B
-- prompt flagged FK promotion as out of scope.
--
-- No seed: v2 §5 Phase B chose not to ship canonical transport nodes in the
-- Catalog. TransportNodeService.seedFromCatalogIfEmpty() exists for pattern
-- symmetry but is a no-op today (and no Spring component triggers it).
--
-- Idempotent via IF NOT EXISTS. Forward-only; no down migration.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS transport_node (
    id                       VARCHAR(255) NOT NULL,
    name                     VARCHAR(255) NOT NULL,
    source                   VARCHAR(255),
    faction                  VARCHAR(255),
    concealed                BOOLEAN      NOT NULL DEFAULT FALSE,
    description              VARCHAR(4000),
    type                     VARCHAR(64)  NOT NULL,
    -- Note: positionX/Y/Z map to positionx/positiony/positionz (no underscore),
    -- because CamelCaseToUnderscoresNamingStrategy only inserts a separator
    -- between a lowercase letter and the following uppercase letter when the
    -- uppercase letter is itself followed by lowercase. Trailing single-letter
    -- suffixes don't qualify.
    positionx                FLOAT(53)    NOT NULL,
    positiony                FLOAT(53)    NOT NULL,
    positionz                FLOAT(53)    NOT NULL,
    connected_node_ids_json  CLOB,
    throughput_tons_per_tick FLOAT(53)    NOT NULL,
    instantaneous_transit    BOOLEAN      NOT NULL DEFAULT FALSE,
    traversal_time_ticks     FLOAT(53)    NOT NULL,
    created_at               TIMESTAMP(6) WITH TIME ZONE,
    modified_at              TIMESTAMP(6) WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_transport_node_name    ON transport_node (name ASC);
CREATE INDEX IF NOT EXISTS idx_transport_node_type    ON transport_node (type);
CREATE INDEX IF NOT EXISTS idx_transport_node_faction ON transport_node (faction);
