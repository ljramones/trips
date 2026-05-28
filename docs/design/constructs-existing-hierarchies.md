# Existing Hierarchies — Inventory Note for Constructs Reconciliation

**Purpose**: §1 deliverable of [`constructs-feature-reconciliation-prompt.md`](./constructs-feature-reconciliation-prompt.md). Proves the survey was actually done by enumerating every type in the three relevant packages, exhaustively on shape, silent on implementation detail.

**Date**: 2026-05-28
**Author**: Claude (post-reconciliation pass)
**Scope**: `com.terranrepublic.assets.*`, `com.terranrepublic.infrastructure.*`, `com.terranrepublic.economy.*`, `com.terranrepublic.sim.*`, `com.teamgannon.trips.spaceshipmodeller.persistence.*`

---

## 1. `com.terranrepublic.assets`

### 1.1 The `Cataloged` seam (shared identity contract)

```java
public interface Cataloged {
    String id();
    String name();
    String source();
    String faction();
    boolean concealed();
    String description();
}
```

Six-method contract for "anything listed in a Terran Republic catalog." It does not extend any other interface. Both `SpaceAsset` and `SpaceInfrastructure` extend it; that's the seam that already unifies the two hierarchies at the identity level.

### 1.2 The `SpaceAsset` sealed hierarchy

```java
@JsonTypeInfo(use = NAME, include = PROPERTY, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SpaceshipDesign.class,    name = "SHIP"),
    @JsonSubTypes.Type(value = StationDesign.class,      name = "STATION"),
    @JsonSubTypes.Type(value = WeaponInstallation.class, name = "WEAPON_INSTALLATION")
})
public sealed interface SpaceAsset extends Cataloged
        permits SpaceshipDesign, StationDesign, WeaponInstallation {
    String designation();
    TechLevel techLevel();
    String category();
    OperationalState operationalState();
    Instant createdAt();
    Instant modifiedAt();
    List<Armament> armaments();
    double dryMassTons();
    AssetKind kind();
}
```

Adds nine accessors on top of `Cataloged`. Discriminator is `kind()` returning `AssetKind`. Jackson polymorphic deserialisation is wired in source via `@JsonTypeInfo` (no JPA hooks; see §5 for persistence reality).

### 1.3 `AssetKind` enum (discriminator)

```java
public enum AssetKind { SHIP, STATION, WEAPON_INSTALLATION }
```

Three values; one per permitted `SpaceAsset` subtype.

### 1.4 `SpaceshipDesign` record (subtype 1 of 3)

```java
public record SpaceshipDesign(
    String                id,
    String                name,
    String                designation,
    ShipClass             shipClass,           // com.teamgannon.trips.spaceshipmodeller.core
    DriveType             driveType,           // com.teamgannon.trips.spaceshipmodeller.propulsion
    MassBudget            massBudget,          // com.teamgannon.trips.spaceshipmodeller.core
    int                   crewComplement,
    double                lengthMeters,
    List<CarriedCraft>    carriedCraft,        // com.teamgannon.trips.spaceshipmodeller.core
    List<Armament>        armaments,
    String                iconPath,
    String                description,
    SourceType            sourceType,          // com.teamgannon.trips.spaceshipmodeller.core
    String                sourceUniverse,
    String                faction,
    boolean               concealed,
    OperationalState      operationalState,
    String                era,
    Instant               createdAt
) implements SpaceAsset { … }
```

Implements `Cataloged` via `source() = sourceLabel()` (composed from universe + faction + era), `techLevel() = EXOTIC iff driveType.category() == EXOTIC else UNKNOWN`, `category() = shipClass.name()`, `modifiedAt() = createdAt`, `dryMassTons() = massBudget.dryMassTons()`, `kind() = AssetKind.SHIP`. Three backwards-compatible constructor overloads exist (predate the `concealed` flag and the `armaments` list).

Domain helpers: `estimateDeltaVKmps()` (Tsiolkovsky), `isSuitableForLanding()`, `grossMassTons()`, `totalCarriedMassTons()`, `carriesCraft()`, `isMothership()`.

### 1.5 `StationDesign` record (subtype 2 of 3)

```java
public record StationDesign(
    String              id,
    String              name,
    String              designation,
    StationType         stationType,
    String              source,              // direct field, not composed
    String              faction,
    boolean             concealed,
    String              allegiance,
    String              description,
    double              overallSpanMeters,
    double              interiorSpanMeters,
    double              dryMassTons,
    double              armourThicknessMeters,
    int                 crewCapacity,
    int                 crewComplement,
    double              pressurizedVolumeM3,
    Mobility            mobility,
    DriveType           auxiliaryDrive,      // nullable; allowed only when mobility != FIXED
    List<CarriedCraft>  carriedCraft,
    List<Armament>      armaments,
    double              hangarVolumeM3,
    boolean             carrierCapable,
    TechLevel           techLevel,
    String              category,            // free text; defaults to stationType.name()
    OperationalState    operationalState,
    Instant             createdAt,
    Instant             modifiedAt
) implements SpaceAsset { … }
```

`kind() = AssetKind.STATION`. Compact-constructor rule: `auxiliaryDrive` is only valid when `mobility != FIXED`. Two BC overloads.

### 1.6 `WeaponInstallation` record (subtype 3 of 3)

```java
public record WeaponInstallation(
    String              id,
    String              name,
    String              designation,
    InstallationType    installationType,
    Emplacement         emplacement,
    String              source,
    String              faction,
    boolean             concealed,
    String              description,
    double              dryMassTons,
    double              footprintSpanMeters,
    boolean             mobile,               // distinct from station's Mobility enum
    int                 crewComplement,
    List<Armament>      armaments,
    TechLevel           techLevel,
    String              category,             // defaults to installationType.name()
    OperationalState    operationalState,
    Instant             createdAt,
    Instant             modifiedAt
) implements SpaceAsset { … }
```

`kind() = AssetKind.WEAPON_INSTALLATION`. Two BC overloads.

### 1.7 `Armament` record (composite carried by all three subtypes)

```java
public record Armament(
    String      name,
    WeaponType  type,
    int         quantity,
    double      yieldOrPowerMW,
    double      effectiveRangeKm,
    String      role,
    String      notes
)
```

### 1.8 Enums referenced from the asset hierarchy

```java
public enum AssetKind         { SHIP, STATION, WEAPON_INSTALLATION }
public enum Mobility          { FIXED, STATIONKEEPING, MANEUVERABLE }
public enum OperationalState  { OPERATIONAL, DAMAGED, DERELICT, WRECK, UNDER_CONSTRUCTION, SALVAGED }
public enum TechLevel         { UNKNOWN, CONTEMPORARY, NEAR_FUTURE, ADVANCED, EXOTIC }
public enum Emplacement       { ORBITAL_DISTRIBUTED, ORBITAL_FIXED, GROUND_FIXED, GROUND_MOBILE, SOLAR_ORBIT }
public enum InstallationType  { BEAM_ARRAY, SUPER_CANNON, DEFENCE_BATTERY, MISSILE_FIELD }
public enum StationType       { BATTLESTATION, GATE_FORT, ORBITAL_CITADEL, SHIPYARD, HABITAT,
                                 CYLINDER, GENERATION_SHIP, OUTPOST, PIRATE_BASE, DEPOT }
public enum WeaponType        { LASER, PARTICLE_BEAM, MISSILE, KINETIC_RAIL, PLASMA,
                                 SOLAR_PUMPED_LASER, NUCLEAR_PULSE_GUN, POINT_DEFENCE, OTHER }
```

### 1.9 `Catalog` — populated seed entries

`Catalog.all()` returns a hard-coded `List<SpaceAsset>` of **five constants**:

| Constant | Kind | Source |
|---|---|---|
| `TROY` | `StationDesign` (`GATE_FORT`) | "Troy Rising" |
| `SAPL` | `WeaponInstallation` (`BEAM_ARRAY`, `SOLAR_ORBIT`) | "Troy Rising" |
| `SHEVA_GUN` | `WeaponInstallation` (`SUPER_CANNON`, `GROUND_MOBILE`) | "Aldenata" |
| `POSLEEN_COMMAND_DODECAHEDRON` | `SpaceshipDesign` (`COMMAND_SHIP`, drive=POSLEEN_NORMAL_SPACE) | "Aldenata" |
| `POSLEEN_BATTLE_DODECAHEDRON` | `SpaceshipDesign` (`DREADNOUGHT`, drive=POSLEEN_NORMAL_SPACE) | "Aldenata" |

Static-only utility class with `private Catalog() {}`. Not Spring-managed.

---

## 2. `com.terranrepublic.infrastructure`

### 2.1 `SpaceInfrastructure` sealed hierarchy

```java
@JsonTypeInfo(use = NAME, include = PROPERTY, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TransportNode.class, name = "TRANSPORT_NODE"),
    @JsonSubTypes.Type(value = Conduit.class,       name = "CONDUIT")
})
public sealed interface SpaceInfrastructure extends Cataloged
        permits TransportNode, Conduit {
    InfrastructureKind kind();
    Instant createdAt();
    Instant modifiedAt();
}
```

Three accessors beyond `Cataloged`. Discriminator `kind()` returning `InfrastructureKind`. Same Jackson wiring pattern as `SpaceAsset`. **Note: extends the same `Cataloged` interface that `SpaceAsset` uses** — the two hierarchies are already siblings under a common identity contract.

### 2.2 `InfrastructureKind` enum

```java
public enum InfrastructureKind { TRANSPORT_NODE, CONDUIT }
```

### 2.3 `TransportNode` record (subtype 1 of 2)

```java
public record TransportNode(
    String       id,
    String       name,
    String       source,
    String       faction,
    boolean      concealed,
    String       description,
    NodeType     type,
    double       positionX,
    double       positionY,
    double       positionZ,
    List<String> connectedNodeIds,           // graph edges (by id, not entity)
    double       throughputTonsPerTick,
    boolean      instantaneousTransit,
    double       traversalTimeTicks,         // forced to 0 if instantaneousTransit
    Instant      createdAt,
    Instant      modifiedAt
) implements SpaceInfrastructure { … }
```

`kind() = InfrastructureKind.TRANSPORT_NODE`. Validates: id and name non-blank, type non-null, throughput and traversal time non-negative. **Jump gates are represented here**: pick a `NodeType` of `RING_GATE` / `JUMP_POINT` / `WORMHOLE_MOUTH` / `PORTAL`, set `instantaneousTransit=true` (or a finite `traversalTimeTicks`), populate `connectedNodeIds` with the partner gate ID. The model is already present; what's missing is a UI for it.

### 2.4 `Conduit` record (subtype 2 of 2)

```java
public record Conduit(
    String   id,
    String   name,
    String   source,
    String   faction,
    boolean  concealed,
    String   description,
    String   fromNodeId,                     // FK to TransportNode.id
    String   toNodeId,                       // FK to TransportNode.id
    double   lengthKm,
    Instant  createdAt,
    Instant  modifiedAt
) implements SpaceInfrastructure { … }
```

`kind() = InfrastructureKind.CONDUIT`. Validates endpoints non-blank, length non-negative.

### 2.5 `NodeType` enum (transit topology)

```java
public enum NodeType {
    RING_GATE,           // The Expanse-style
    JUMP_POINT,          // Babylon 5 / EVE
    WORMHOLE_MOUTH,      // natural / stable
    PORTAL,              // generic teleport
    BEANSTALK_ANCHOR,    // surface tether
    RELAY,               // comms / refuel
    BEACON,              // navigation
    NAV_HAZARD           // mapped but avoidable
}
```

### 2.6 `GraphRegistry` — validation registry

```java
public record GraphRegistry(
    Map<String, TransportNode> nodesById,
    Map<String, Conduit>       conduitsById
) {
    public static GraphRegistry of(Collection<TransportNode>, Collection<Conduit>);
    public static GraphRegistry ofNodes(Collection<TransportNode>);
    public List<String> validate();          // dangling-reference checks
    public void requireValid();              // throws IllegalStateException on errors
}
```

Validates:
- every `TransportNode.connectedNodeIds[i]` exists in `nodesById`
- every `Conduit.fromNodeId` and `Conduit.toNodeId` exists in `nodesById`
- no duplicate ids in either map

Constructor enforces non-null entries and unique ids. Pure in-memory; no JPA.

---

## 3. `com.terranrepublic.economy` + `com.terranrepublic.sim`

### 3.1 `Commodity`

```java
public record Commodity(
    String          id,
    String          name,
    CommodityClass  commodityClass,
    double          unitMassTons,
    String          provenanceNote
)

public enum CommodityClass {
    RAW_ORE, REFINED_METAL, VOLATILE, FUEL, FABRICATED_GOOD, EXOTIC
}
```

### 3.2 `ResourceDeposit` — joins commodities to celestial bodies

```java
public record ResourceDeposit(
    String                  id,
    String                  bodyId,                       // FK to existing TRIPS celestial-body id
    BodyKind                bodyKind,                     // disambiguator for the FK
    Map<String, Double>     abundanceByCommodityId,       // FK keys -> Commodity.id
    double                  extractionDifficulty,         // ∈ [0, 1]
    String                  notes
) {
    public ResourceDeposit withAbundanceDelta(String commodityId, double amount);
}

public enum BodyKind { STAR, PLANET, MOON, ASTEROID_BELT, OTHER }
```

`bodyId` references an external celestial-body id (whatever scheme TRIPS uses for stars / planets / moons / asteroid belts). `EconomyRegistry` does not look the id up — it only checks the field is non-blank — so the integrity of the join lives outside this hierarchy.

### 3.3 `IndustrialOperation` — process recipe

```java
public record IndustrialOperation(
    String                  id,
    OperationType           type,            // MINING / REFINING / FABRICATION
    String                  hostAssetId,     // FK to SpaceAsset.id
    Map<String, Double>     inputsPerTick,   // Commodity.id -> tons
    Map<String, Double>     outputsPerTick,  // Commodity.id -> tons
    double                  efficiency,      // ∈ [0, 1]
    String                  sourceDepositId, // nullable; FK to ResourceDeposit.id; MINING uses this
    String                  notes
)

public enum OperationType { MINING, REFINING, FABRICATION }
```

### 3.4 `Stockpile` — inventory owned by an asset

```java
public record Stockpile(
    String                  id,
    String                  ownerAssetId,                  // FK to SpaceAsset.id
    Map<String, Double>     quantitiesByCommodityId,       // FK keys -> Commodity.id
    double                  capacityTons
) {
    public Stockpile withDelta(String commodityId, double amount);
    public double quantity(String commodityId);
    public double totalTons();
    public double availableCapacityTons();
}
```

### 3.5 `SupplyRoute` — declared flow between transport nodes

```java
public record SupplyRoute(
    String  id,
    String  fromNodeId,    // FK to TransportNode.id
    String  toNodeId,      // FK to TransportNode.id
    String  commodityId,   // FK to Commodity.id
    double  tonsPerTick,
    String  routeLore
)
```

### 3.6 `EconomyRegistry` — strong-reference validation registry

```java
public record EconomyRegistry(
    Map<String, SpaceAsset>           assetsById,
    Map<String, TransportNode>        nodesById,
    Map<String, Commodity>            commoditiesById,
    Map<String, ResourceDeposit>      depositsById,
    Map<String, Stockpile>            stockpilesById,
    Map<String, IndustrialOperation>  operationsById,
    Map<String, SupplyRoute>          routesById
) {
    public static EconomyRegistry of(…);
    public List<String> validate();
    public void requireValid();
    public EconomyRegistry withStockpiles(Map<String, Stockpile>);
}
```

Validation enforced:
- `ResourceDeposit.bodyId` non-blank; every `abundanceByCommodityId` key exists in `commoditiesById`
- `Stockpile.ownerAssetId` exists in `assetsById`; every `quantitiesByCommodityId` key exists in `commoditiesById`
- `IndustrialOperation.hostAssetId` exists in `assetsById`; `sourceDepositId` (if non-null) exists in `depositsById`; every input/output commodity id exists
- `SupplyRoute.{fromNodeId, toNodeId}` exist in `nodesById`; `commodityId` exists in `commoditiesById`

**Critical observation**: the registry takes `Map<String, SpaceAsset>` directly. **`Stockpile.ownerAssetId` and `IndustrialOperation.hostAssetId` are FKs to `SpaceAsset.id`.** If `SpaceAsset` is renamed or refactored, every economy validation path is in scope.

### 3.7 `WorldState` — immutable simulation snapshot

```java
public record WorldState(
    EconomyRegistry                   economyRegistry,
    GraphRegistry                     graphRegistry,
    Map<String, Stockpile>            stockpilesById,
    Map<String, ResourceDeposit>      depositsById,
    Map<String, IndustrialOperation>  operationsById,
    Map<String, SupplyRoute>          routesById,
    Map<String, TransportNode>        nodesById,
    Map<String, String>               stockpileIdByAssetId,
    Map<String, String>               stockpileIdByNodeId,
    long                              tick
) {
    public static WorldState from(EconomyRegistry, GraphRegistry,
                                  Map<String,String> byAsset, Map<String,String> byNode,
                                  long tick);
    public WorldState withStockpiles(Map<String, Stockpile>, long nextTick);
}
```

Carries both registries plus per-tick mutable-ish state (stockpiles) plus the two stockpile-lookup maps used by the tick engine.

### 3.8 `TickEngine` — pure functional engine

```java
public final class TickEngine {
    private TickEngine() {}
    public static WorldState tick(WorldState state);
    public static List<WorldState> run(WorldState initial, int ticks);
}
```

Pure function `tick: WorldState -> WorldState`. Two passes per tick: `runOperations` (sorted by op id, deducts inputs, adds outputs scaled by `efficiency` and an affordability fraction), `runRoutes` (sorted by route id, moves commodities up to `tonsPerTick`, source-quantity-limited, dest-capacity-limited). Never mutates inputs.

---

## 4. `com.teamgannon.trips.spaceshipmodeller.persistence` — the duplication audit answer

### 4.1 The verdict: no, `SpaceshipEntity` is NOT a third duplication

The reconciliation prompt §4 asks: is `SpaceshipEntity` the same data as `SpaceshipDesign`, or is it a third duplication? **Answer: same data; not a third duplication.** They coexist *by design* as the immutable-domain / mutable-JPA seam, with `SpaceshipDesignMapper` doing bidirectional conversion. This is the standard TRIPS pattern (see also `TransferPlanEntity` ↔ `TransferPlan` in the same module).

### 4.2 `SpaceshipEntity` shape

```java
@Entity(name = "SPACESHIP_DESIGN")
@Table(indexes = {
    @Index(columnList = "name ASC"),
    @Index(columnList = "shipClass"),
    @Index(columnList = "driveType"),
    @Index(columnList = "sourceType")
})
@DynamicUpdate
public class SpaceshipEntity implements Serializable {
    @Id                     private String id;                       // UUID
    @Column(nullable=false) private String name;
                            private String designation;
    @Enumerated(STRING)
    @Column(nullable=false) private ShipClass shipClass;
    @Enumerated(STRING)
    @Column(nullable=false) private DriveType driveType;
    // --- flattened MassBudget ---
    private double structureMassTons;
    private double engineMassTons;
    private double propellantMassTons;
    private double payloadMassTons;
    private double crewMassTons;
    private double radiatorMassTons;
    // ---
    private int    crewComplement;
    private double lengthMeters;
    @Lob private String carriedCraftJson;   // List<CarriedCraft> via Jackson
    @Lob private String armamentsJson;      // List<Armament>     via Jackson
    private String iconPath;
    @Column(length=4000) private String description;
    @Enumerated(STRING)  private SourceType sourceType;
    private String sourceUniverse;
    private String faction;
    private String era;
    private Instant createdAt;
}
```

### 4.3 `SpaceshipDesignMapper.toEntity` / `toDomain`

```java
@Component
public class SpaceshipDesignMapper {
    public SpaceshipEntity   toEntity(SpaceshipDesign  design);
    public SpaceshipDesign   toDomain(SpaceshipEntity  entity);
}
```

Round-trips every field of `SpaceshipDesign` except `concealed` and `operationalState` (visible in §4.4 — those *are* dropped on the way through persistence). Collection fields go through Jackson `ObjectMapper.writeValueAsString` / `readValue`; deserialisation failures are logged at `ERROR` and degrade to empty lists.

### 4.4 Fields on `SpaceshipDesign` *not* preserved by the mapper

Comparing §1.4 to the mapper:

| `SpaceshipDesign` field | `SpaceshipEntity` field | Round-trips? |
|---|---|---|
| `concealed` | — | **No** — silently lost on persist |
| `operationalState` | — | **No** — silently lost on persist; reconstituted as `OPERATIONAL` by the compact constructor's default |

These are existing bugs / known shortcuts the mapper takes; not introduced by Constructs. Worth knowing for any v2 persistence redesign so we don't regress them further.

### 4.5 `SpaceshipRepository`

```java
public interface SpaceshipRepository extends JpaRepository<SpaceshipEntity, String> {
    Optional<SpaceshipEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<SpaceshipEntity> findByShipClass(ShipClass shipClass);
    List<SpaceshipEntity> findByDriveType(DriveType driveType);
    List<SpaceshipEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String fragment);
}
```

Standard Spring Data JPA repository. Filtering by `Category` (drive's category) is delegated to the service layer.

---

## 5. Persistence wiring — what's actually persisted vs in-memory

This is the most important section for v2 planning. **Read it twice.**

| Type | Persistence | Notes |
|---|---|---|
| `SpaceshipDesign` (the `SHIP` variant of `SpaceAsset`) | **JPA** via `SpaceshipEntity` + `SpaceshipDesignMapper` + `SpaceshipRepository` | Table `SPACESHIP_DESIGN`; collections JSON-in-LOB; `concealed` / `operationalState` lost on round-trip |
| `StationDesign` (the `STATION` variant) | **In-memory only** | Lives in `Catalog.TROY` constant + any test fixtures; no entity, no repository, no table |
| `WeaponInstallation` (the `WEAPON_INSTALLATION` variant) | **In-memory only** | Same — `Catalog.SAPL`, `Catalog.SHEVA_GUN` |
| `TransportNode`, `Conduit` | **In-memory only** | Via `GraphRegistry.of(...)`; no JPA |
| `Commodity`, `ResourceDeposit`, `Stockpile`, `IndustrialOperation`, `SupplyRoute` | **In-memory only** | Via `EconomyRegistry` + `WorldState`; no JPA |
| `TransferPlan` (transfer planner output) | **JPA** via `TransferPlanEntity` + `TransferPlanMapper` + `TransferPlanRepository` | Separate concern; mentioned for completeness — uses the same immutable-domain / mutable-JPA pattern as `SpaceshipDesign` |

### 5.1 Jackson wiring is for import / export, not active persistence

The `@JsonTypeInfo(property = "kind")` on `SpaceAsset` and `SpaceInfrastructure` is wired so the sealed hierarchies can survive a JSON round-trip (file export, API exchange, etc.). It is **not** doing JPA serialisation. The `payload_json` CLOB idea floated in the original v1 plan is not how anything is persisted today; the closest existing pattern is `SpaceshipEntity.carriedCraftJson` (a Jackson-serialised collection inside a LOB on an otherwise flat entity), not a Jackson-serialised whole-entity payload.

### 5.2 Implication for any cross-tx UI reader

Persistence today goes flat-table-per-subtype (`SpaceshipEntity`), not single-table-with-JSON. Any v2 design that introduces a CLOB `payload_json` is introducing a *new* persistence shape that the codebase hasn't validated against the LazyInitializationException constraint from Issue 46. The safest v2 path is to keep extending the flat-entity pattern: add `StationEntity` + `WeaponInstallationEntity` (mirroring `SpaceshipEntity` shape) when those subtypes need persistence, rather than collapsing everything into one table with a JSON column.

---

## 6. Where the v1 plan's row-by-row overlap table needs correcting

Walking the §2 overlap table from the reconciliation prompt against this inventory:

| v1 row | What's actually true |
|---|---|
| `Construct` ≈ `SpaceAsset` | Partly — `Construct` ≈ `SpaceAsset ∪ SpaceInfrastructure`. The two hierarchies are already siblings via `Cataloged`. |
| `Spaceship` ≈ `SpaceshipDesign` | **Exact** — same data; v1's `Spaceship` is a rename. |
| `Starbase` / `BattleMoon` / `MiningStation` ≈ `StationDesign + StationType` | **Confirmed** — `StationType` already enumerates `BATTLESTATION`, `GATE_FORT`, `ORBITAL_CITADEL`, `SHIPYARD`, `HABITAT`, `CYLINDER`, `GENERATION_SHIP`, `OUTPOST`, `PIRATE_BASE`, `DEPOT`. Battle moon / Death Star would naturally land as `BATTLESTATION` or `GATE_FORT`. Asteroid mining station as `OUTPOST` or `DEPOT` (or one extra enum value). |
| `PlanetaryDefenceInstallation` ≈ `WeaponInstallation` | **Confirmed** — the existing `Emplacement.GROUND_FIXED` + `InstallationType.DEFENCE_BATTERY` already model planetary defence. |
| `JumpGate` ≈ `TransportNode(type=RING_GATE)` | **Confirmed** — the data model is fully there. `TransportNode.connectedNodeIds` is the partner-pair link. What's missing is a UI for editing them, not the data. |
| `ConstructIdentity` ≈ `Cataloged` | **Mostly** — `Cataloged` already carries `id, name, source, faction, concealed, description`. v1's `ConstructIdentity` also added `designation, category, sourceType, sourceUniverse, era, iconPath, createdAt, mobility`. Those are sometimes on the SpaceAsset interface (`designation`, `category`, `createdAt`) and sometimes on subtype records (`sourceType`, `sourceUniverse`, `era` are on `SpaceshipDesign`; `Mobility` is a station-specific enum). |
| `Mobility` enum | **Already exists** — `assets.Mobility{FIXED, STATIONKEEPING, MANEUVERABLE}`. v1's proposed `{FIXED, MOBILE_LIMITED, MOBILE}` is the same idea with different labels; existing wins. |
| `category` discriminator | **Two of them exist** — `AssetKind` on `SpaceAsset` and `InfrastructureKind` on `SpaceInfrastructure`. v1's single `ConstructCategory` enum would have collapsed both, which is fine as a UI-level discriminator but not as a domain-level one. |
| Single-table `construct` with `payload_json` | **Different from today** — see §5.1. The existing pattern is per-subtype flat entities with Jackson-LOB collection fields, not whole-entity JSON. |

---

## 7. What v1 would have broken if implemented as written

The v1 plan's §4.3 "Spaceship migration" table proposed dropping fields from `SpaceshipDesign` onto a new `Construct` / `Spaceship` shape. Following the proposed move literally would have:

- **Lost or split** the existing `Cataloged` seam by introducing a parallel `ConstructIdentity` record.
- **Renamed enums that already exist** under different but functionally identical names (the proposed `Mobility{FIXED, MOBILE_LIMITED, MOBILE}` vs the existing `Mobility{FIXED, STATIONKEEPING, MANEUVERABLE}`).
- **Forced a `payload_json` CLOB** that the codebase has never validated against the Issue 46 LazyInitializationException constraint (see §5).
- **Required updating every economy callsite** that takes `SpaceAsset` as a Spring/registry key — `EconomyRegistry`, `Stockpile.ownerAssetId`, `IndustrialOperation.hostAssetId`, `WorldState.stockpileIdByAssetId`, and `TickEngine`'s asset-lookup paths.
- **Introduced a third copy of the spaceship data** if the v1 author had also kept `SpaceshipEntity` (the persistence mirror) and the in-memory `Catalog.POSLEEN_*` constants while adding a fourth shape under `com.teamgannon.trips.construct`.

That last point is the one that hurts most: v1 framed `SpaceshipEntity` as a candidate "third duplication" but actually the entity-vs-record pair is the existing seam, *and v1 was the proposed third*.

---

## 8. Read-order for the v2 plan

When writing `constructs-feature-plan-v2.md`, cite this inventory's section numbers (not file paths) so the v2 reader sees what's grounded in the survey and what isn't. The mapping is:

| Reconciliation prompt question | This note's section |
|---|---|
| §2.row(`Construct` ≈ `SpaceAsset`) | §1.2, §2.1 |
| §2.row(`Spaceship` ≈ `SpaceshipDesign`) | §1.4 |
| §2.row(`Starbase`/`BattleMoon`/`MiningStation` ≈ `StationDesign`) | §1.5, §1.8 (`StationType`) |
| §2.row(`PlanetaryDefenceInstallation` ≈ `WeaponInstallation`) | §1.6, §1.8 (`Emplacement`, `InstallationType`) |
| §2.row(`JumpGate` ≈ `TransportNode`) | §2.3, §2.5 |
| §2.row(`ConstructIdentity` ≈ `Cataloged`) | §1.1 |
| §2.row(`Mobility` enum) | §1.8 (`Mobility`) |
| §3 split between `SpaceAsset` and `SpaceInfrastructure` | §1.2, §2.1 (same `Cataloged` parent) |
| §4 `SpaceshipEntity` vs `SpaceshipDesign` | §4.1–4.4 (not a third duplication; round-trip-lossy on `concealed`/`operationalState`) |
| §5 Issue-46 / CLOB constraint | §5.1–5.2 (existing pattern is flat-entity, not payload_json) |
| §6 menu lesson | (handled at the v2 plan layer; nothing in the inventory needs to address it) |
