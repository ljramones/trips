# Transits Package

## Overview

The transits package provides functionality for visualizing and managing **transit routes** between stars in the TRIPS 3D stellar cartography application. A transit represents a possible "jump" or travel path between two stars within a defined distance range.

## What Are Transits?

Transits are visual connections drawn between stars that fall within specified distance ranges. They help users:

- Identify which stars are reachable from a given location
- Plan interstellar routes using distance-banded connections
- Visualize travel networks across star systems

Users can define multiple **transit bands**, each with its own distance range (e.g., 0-5 light years, 5-10 light years) and visual properties (color, line width). This allows simultaneous visualization of short-range and long-range connections.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        TransitManager                            │
│  (Spring @Component - orchestrates all transit visualization)   │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              │ creates/manages
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  TransitRouteVisibilityGroup                     │
│  (One per transit band - handles 3D rendering for that band)    │
│                                                                  │
│  Uses:                                                          │
│  ├── TransitGraphicsContext (graphics dependencies)             │
│  ├── ITransitDistanceCalculator (calculates star distances)     │
│  └── ITransitRouteBuilder (builds routes from segments)         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ calculates
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       TransitRoute                               │
│  (Data model - source star, target star, distance, color)       │
└─────────────────────────────────────────────────────────────────┘
```

## Key Components

### Data Models

| Class | Purpose |
|-------|---------|
| `TransitDefinitions` | Container for all transit band definitions for a dataset |
| `TransitRangeDef` | Single transit band configuration (range, color, line width, enabled) |
| `TransitRoute` | A calculated transit between two stars |
| `TransitConstants` | Shared constants (range limits, default line widths, label sizing) |

### Services

| Class | Purpose |
|-------|---------|
| `TransitManager` | Main entry point - coordinates all transit visualization |
| `TransitRouteBuilderService` | Builds multi-segment routes from individual transits |
| `StarMeasurementService` | Calculates distances between stars (implements `ITransitDistanceCalculator`) |

### Visualization

| Class | Purpose |
|-------|---------|
| `TransitRouteVisibilityGroup` | Renders transits for one band as 3D cylinders with labels |
| `TransitGraphicsContext` | Bundles graphics dependencies (SubScene, pane, services) |

### UI Components

| Class | Purpose |
|-------|---------|
| `FindTransitsBetweenStarsDialog` | Main dialog for configuring transit bands |
| `TransitBandEditor` | Inline editor for a single transit band (checkbox, slider, color picker) |

### Interfaces

| Interface | Purpose |
|-----------|---------|
| `ITransitDistanceCalculator` | Abstracts distance calculation for testability |
| `ITransitRouteBuilder` | Abstracts route building for testability |

## Usage Workflow

### 1. Configure Transit Bands

Users open `FindTransitsBetweenStarsDialog` to define transit bands:

```
┌────────────────────────────────────────────────────────────┐
│ Select a Range to Find Transits                            │
├────────────────────────────────────────────────────────────┤
│ Use?  Band Name    Lower  ──────Slider──────  Upper  Width │
│ [✓]   Short Range   0.0   ████████░░░░░░░░░░   5.0    0.5  │
│ [✓]   Medium Range  5.0   ░░░░░░░░████████░░  10.0    0.5  │
│ [ ]   Long Range   10.0   ░░░░░░░░░░░░████░░  15.0    0.5  │
├────────────────────────────────────────────────────────────┤
│              [Generate Transits]  [Cancel]                  │
└────────────────────────────────────────────────────────────┘
```

Each band can be:
- **Enabled/disabled** via checkbox
- **Named** for identification
- **Configured** with lower/upper distance bounds (0-20 light years)
- **Styled** with custom color and line width

### 2. Generate Transits

When "Generate Transits" is clicked:

1. `TransitManager.findTransits()` is called with the definitions and visible stars
2. For each enabled band, a `TransitRouteVisibilityGroup` is created
3. `StarMeasurementService.calculateDistances()` computes all star pairs within range
4. 3D cylinders are rendered between qualifying star pairs
5. Distance labels appear at each transit's midpoint

### 3. Interact with Transits

Users can:
- **Hover** over a transit to see tooltip: "transit: Sol <--> Alpha Centauri is 4.37 ly"
- **Click** to open context menu with options:
  - Create New Route (start a multi-hop route)
  - Add To Route (extend current route)
  - Complete Route (finish and save route)
  - Remove (hide this transit)

### 4. Build Routes

The route-building workflow uses `TransitRouteBuilderService`:

```
Start New Route → Add segments → Complete Route
      │                │              │
      ▼                ▼              ▼
RouteDialog     currentRouteList   NewRouteEvent
(name/color)    accumulates        published
```

Routes are persisted via `NewRouteEvent` and displayed by `RouteManager`.

## 3D Visualization Details

### Transit Lines

Transits are rendered as 3D cylinders connecting star positions:

```java
// In TransitRouteVisibilityGroup.createLineSegment()
var line = StellarEntityFactory.createCylinder(lineWeight, color, height);
line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
```

The cylinder is:
1. Created with the band's line weight and color
2. Positioned at the midpoint between stars
3. Rotated to align with the vector between stars

### Labels

Distance labels use the billboard pattern (see CLAUDE.md):
- Labels are 2D nodes in a separate `labelGroup`
- A small anchor sphere marks the label position in 3D space
- `updateLabels()` repositions labels after camera rotation/zoom

### Visibility Control

```java
// Show/hide a specific band
transitManager.showTransit(bandId, true);

// Show/hide labels for a band
transitManager.showLabels(bandId, true);

// Toggle all transit labels
transitManager.toggleTransitLengths(showLabels);
```

## Validation

The dialog validates that enabled bands don't have overlapping ranges:

```java
// In FindTransitsBetweenStarsDialog.rangesOverlap()
return (b.getLowerRange() > a.getLowerRange() && b.getLowerRange() < a.getUpperRange()) ||
       (b.getUpperRange() > a.getLowerRange() && b.getUpperRange() < a.getUpperRange());
```

**Note:** The current validation uses strict inequality, so:
- Adjacent ranges (0-5, 5-10) are valid
- Identical ranges are not detected as overlapping (edge case)

## Constants

Key constants from `TransitConstants`:

| Constant | Value | Purpose |
|----------|-------|---------|
| `RANGE_MIN` | 0.0 | Minimum distance slider value |
| `RANGE_MAX` | 20.0 | Maximum distance slider value |
| `RANGE_MAJOR_TICK` | 5.0 | Slider major tick spacing |
| `DEFAULT_LINE_WIDTH` | 1.0 | Default transit line thickness |
| `DEFAULT_BAND_LINE_WIDTH` | 0.5 | Default band line thickness |
| `LABEL_ANCHOR_SPHERE_RADIUS` | 1.0 | Size of label anchor point |
| `LABEL_PADDING` | 20.0 | Padding for label visibility checks |
| `LABEL_EDGE_MARGIN` | 5.0 | Margin for label edge clamping |

## Performance Optimization

The system automatically selects the optimal algorithm based on star count:

| Star Count | Algorithm | Complexity | Notes |
|------------|-----------|------------|-------|
| ≤ 100 | Brute Force | O(n²) | Lower overhead for small datasets |
| > 100 | KD-Tree | O(n log n) | Spatial indexing with parallel queries |

### KD-Tree Implementation

For large datasets, the system uses a 3D KD-Tree with parallel query processing:

```
┌──────────────────────────────────────────────────────────────┐
│                  TransitCalculatorFactory                     │
│  (Selects optimal algorithm based on star count)             │
└──────────────────────────┬───────────────────────────────────┘
                           │
           ┌───────────────┴───────────────┐
           ▼                               ▼
┌──────────────────────┐      ┌──────────────────────────────┐
│ StarMeasurementService│      │   KDTreeTransitCalculator    │
│      (O(n²))         │      │       (O(n log n))           │
│  Best for n ≤ 100    │      │    Best for n > 100          │
└──────────────────────┘      │                              │
                              │  ┌─────────────────────────┐ │
                              │  │       KDTree3D          │ │
                              │  │  (3D spatial index)     │ │
                              │  └─────────────────────────┘ │
                              │                              │
                              │  Features:                   │
                              │  • O(n log n) construction   │
                              │  • O(log n + k) range query  │
                              │  • Parallel query execution  │
                              │  • Thread-safe after build   │
                              └──────────────────────────────┘
```

### Algorithm Details

**KD-Tree Range Search:**
```java
// Build tree once - O(n log n)
KDTree3D<StarDisplayRecord> tree = new KDTree3D<>(points);

// Query each star - O(log n + k) per star, parallel execution
stars.parallelStream()
    .flatMap(star -> tree.rangeSearch(star.getCoordinates(), maxRange).stream())
    .filter(/* distance checks */)
    .collect(toList());
```

**Key optimizations:**
1. **Spatial partitioning**: Only checks nearby stars, not all pairs
2. **Parallel execution**: Queries run on multiple CPU cores for n > 500 stars
3. **Consistent pair keys**: Prevents duplicate A↔B routes
4. **Single tree for all bands**: Multi-band calculation reuses spatial structure

### Performance Benchmarks

| Stars | Brute Force | KD-Tree | Speedup |
|-------|-------------|---------|---------|
| 100 | ~5ms | ~8ms | 0.6x (overhead) |
| 500 | ~125ms | ~15ms | 8x |
| 1,000 | ~500ms | ~25ms | 20x |
| 5,000 | ~12.5s | ~80ms | 156x |
| 10,000 | ~50s | ~150ms | 333x |

*Benchmarks on M1 Mac, typical transit range 0-10 ly*

## Event Integration

The transits system publishes events for route building:

| Event | When Published |
|-------|----------------|
| `RoutingStatusEvent` | Route building starts/stops |
| `NewRouteEvent` | A route is completed and ready for display |

These events are consumed by `RouteManager` and UI components.

## Testing

Comprehensive tests are available in `src/test/java/com/teamgannon/trips/transits/`:

**Data Model Tests:**
- `TransitConstantsTest` - Constant value verification
- `TransitRangeDefTest` - Data model tests
- `TransitRouteTest` - Transit route model tests
- `TransitDefinitionsTest` - Definitions container tests

**Validation Tests:**
- `TransitRangeValidationTest` - Range overlap validation tests

**Algorithm Tests:**
- `kdtree/KDTree3DTest` - KD-Tree spatial index correctness and performance
- `kdtree/KDTreeTransitCalculatorTest` - Transit calculator with parallel processing

Run all transit tests with:
```bash
./mvnw-java17.sh test -Dtest="com.teamgannon.trips.transits.**"
```

Total: 162 tests covering data models, validation, spatial indexing, and parallel processing.
