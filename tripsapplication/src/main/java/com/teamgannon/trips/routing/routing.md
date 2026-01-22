# Routing Package

## Overview

The routing package provides functionality for finding and visualizing **interstellar routes** between stars in the TRIPS 3D stellar cartography application. A route represents a multi-hop path connecting two stars through a series of intermediate waypoints within a specified jump distance.

## What Are Routes?

Routes are paths connecting stars that help users:

- Plan interstellar travel between distant star systems
- Visualize the shortest paths using different jump distance constraints
- Compare multiple alternative paths between the same endpoints
- Build custom routes manually by clicking on stars

Users can define **route parameters** including:
- Origin and destination stars
- Upper and lower bounds for jump distance (light years)
- Number of alternative paths to find
- Visual properties (color, line width)
- Spectral class and polity exclusions

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        RouteManager                              │
│  (Spring @Component - orchestrates all route operations)        │
└──────────────────────────────┬──────────────────────────────────┘
                               │
           ┌───────────────────┼───────────────────┐
           │                   │                   │
           ▼                   ▼                   ▼
┌──────────────────┐  ┌─────────────────┐  ┌─────────────────────┐
│  RouteDisplay    │  │ CurrentManual   │  │ RouteFindingService │
│  (3D rendering,  │  │ Route           │  │ (algorithm,         │
│   label mgmt)    │  │ (real-time      │  │  JGraphT)           │
│                  │  │  user drawing)  │  │                     │
└──────────────────┘  └─────────────────┘  └─────────────────────┘
         │                    │                      │
         │                    │                      ▼
         │                    │            ┌─────────────────────┐
         │                    │            │     RouteGraph      │
         │                    │            │  (JGraphT wrapper,  │
         │                    │            │   Yen's K-shortest) │
         │                    │            └─────────────────────┘
         │                    │
         └────────────┬───────┘
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                       RouteDescriptor                            │
│  (Data model - stars, segments, lengths, color, line width)     │
└─────────────────────────────────────────────────────────────────┘
```

## Key Components

### Core Services

| Class | Purpose |
|-------|---------|
| `RouteManager` | Main entry point - coordinates all route visualization and operations |
| `RouteFindingService` | Core algorithm using JGraphT for K-shortest paths |
| `RouteDisplay` | 3D rendering of routes, label management, visibility control |
| `CurrentManualRoute` | Manages real-time manual route creation with undo support |

### Route Finding

| Class | Purpose |
|-------|---------|
| `RouteFinderInView` | Route finder for stars currently visible (fast) |
| `RouteFinderDataset` | Route finder across entire dataset (async, for large datasets) |
| `RouteGraph` | JGraphT wrapper for weighted graph and Yen's algorithm |
| `RouteBuilderHelper` | Constructs RouteDescriptor from path results |

### Utilities

| Class | Purpose |
|-------|---------|
| `RouteBuilderUtils` | Creates route graphics, checks visibility |
| `PartialRouteUtils` | Handles visualization when some stars not visible |
| `RouteGraphicsUtil` | Creates 3D line segments and labels |
| `RoutingConstants` | Centralized constants for the package |

### UI Components

| Class | Purpose |
|-------|---------|
| `RoutingPanel` | Side panel TableView showing all routes |
| `RouteFinderDialogInView` | Dialog for configuring route search (in-view) |
| `RouteFinderDialogInDataSet` | Dialog for configuring route search (full dataset) |
| `DisplayAutoRoutesDialog` | Dialog for selecting which routes to display |
| `RouteEditDialog` | Dialog for editing route properties |
| `ContextManualRoutingDialog` | Dialog for manual route creation |

### Data Models

| Class | Purpose |
|-------|---------|
| `RouteDescriptor` | Complete route specification with segments and metadata |
| `RouteFindingOptions` | User-specified parameters for route search |
| `RouteFindingResult` | Result object (success with routes or failure with message) |
| `RoutingMetric` | Route evaluation data (rank, length, segments) |
| `RouteSegment` | Single segment between two stars |
| `PossibleRoutes` | Container for multiple route alternatives |

### Interfaces

| Interface | Purpose |
|-----------|---------|
| `RoutingCallback` | Callback for color change notifications |
| `RouteVisibility` | Enum: FULL, PARTIAL |
| `RoutingType` | Enum: AUTOMATIC, MANUAL, NONE |

## Usage Workflow

### 1. Automated Route Finding (In-View)

Users can find routes between stars currently visible in the viewport:

```
┌────────────────────────────────────────────────────────────┐
│ Find Routes (In View)                                       │
├────────────────────────────────────────────────────────────┤
│ Origin Star:      [Sol              ▼]                     │
│ Destination Star: [Alpha Centauri   ▼]                     │
│                                                            │
│ Upper Bound (ly): [8.0    ]  Lower Bound (ly): [3.0    ]  │
│ Line Width:       [0.5    ]  Number of Paths:  [3      ]  │
│ Route Color:      [■ Coral]                                │
│                                                            │
│ Exclude Spectral Classes:                                  │
│ [ ] O  [ ] B  [ ] A  [✓] M  [✓] L  [✓] T  [✓] Y           │
│                                                            │
│              [Find Routes]  [Cancel]                       │
└────────────────────────────────────────────────────────────┘
```

### 2. Route Finding Process

When "Find Routes" is clicked:

1. `RouteFindingService.findRoutes()` is called with options
2. Stars are pruned based on spectral class and polity exclusions
3. `StarMeasurementService` calculates all valid transits (jumps)
4. `RouteGraph` builds a weighted graph using JGraphT
5. Connectivity is checked between origin and destination
6. Yen's K-shortest paths algorithm finds alternatives
7. Results returned as `RouteFindingResult`

```
Stars in View → Prune by Exclusions → Calculate Transits
                                              ↓
                                    Build Weighted Graph
                                              ↓
                              Check Connectivity (origin ↔ dest)
                                              ↓
                              Yen's K-Shortest Paths Algorithm
                                              ↓
                                    Return Route Results
```

### 3. Route Selection

After routes are found, users select which to display:

```
┌────────────────────────────────────────────────────────────┐
│ Route Results                                               │
├────────────────────────────────────────────────────────────┤
│ [✓] Route 1: Sol → Barnard's → Alpha Centauri (7.2 ly)    │
│ [ ] Route 2: Sol → Wolf 359 → Alpha Centauri (8.1 ly)     │
│ [ ] Route 3: Sol → Luyten → Alpha Centauri (9.4 ly)       │
│                                                            │
│              [Plot Selected]  [Cancel]                     │
└────────────────────────────────────────────────────────────┘
```

### 4. Manual Route Creation

Users can also create routes manually by clicking on stars:

1. Click "Manual Route" button
2. `RouteManager.setManualRoutingActive(true)`
3. Click on origin star → `CurrentManualRoute.startRoute()`
4. Click on subsequent stars → `CurrentManualRoute.continueRoute()`
5. Each segment is drawn in real-time with distance labels
6. Click "Finish" → `CurrentManualRoute.finishRoute()`
7. Route is saved and published via `NewRouteEvent`

### 5. Route Visualization

Routes are rendered as 3D cylinders connecting stars:

```java
// In RouteGraphicsUtil.createLineSegment()
Point3D yAxis = new Point3D(0, 1, 0);
Point3D diff = target.subtract(origin);
double height = diff.magnitude();

// Position at midpoint between stars
Point3D mid = target.midpoint(origin);
Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

// Rotate to align with vector between stars
Point3D axisOfRotation = diff.crossProduct(yAxis);
double angle = Math.acos(diff.normalize().dotProduct(yAxis));
Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);
```

### 6. Label Positioning (Billboard Pattern)

Route distance labels use the billboard pattern for screen-aligned display:

```
sceneRoot (2D)
├── subScene (3D viewport)
│   └── world (3D group - rotates with camera)
│       └── Route cylinders and endpoint spheres
└── labelDisplayGroup (2D group - stays fixed)
    └── Distance labels (always flat to screen)
```

**Two-Step Coordinate Transformation:**

```java
// In RouteDisplay.updateLabels()
Point3D sceneCoords = node.localToScene(Point3D.ZERO, true);  // 3D → Scene
Point2D localPoint = labelDisplayGroup.sceneToLocal(xs, ys);   // Scene → Label group
label.getTransforms().setAll(new Translate(x, y));
```

## Algorithm Details

### Yen's K-Shortest Paths Algorithm

The routing system uses JGraphT's implementation of Yen's algorithm:

- **Complexity**: O(K × n × (m + n log n)) where K = paths, n = nodes, m = edges
- **Graph Type**: SimpleWeightedGraph with star names as vertices
- **Edge Weights**: Distance in light years between connected stars

### Star Pruning

Before route finding, stars are filtered based on user preferences:

```java
// Spectral class exclusion
if (options.getExcludedSpectralClasses().contains(spectralType)) {
    continue;  // Skip this star
}

// Polity exclusion
if (options.getExcludedPolities().contains(polity)) {
    continue;  // Skip this star
}
```

### Transit Calculation

Transits (possible jumps) are calculated using `StarMeasurementService`:

- Only star pairs within the specified distance bounds are connected
- Upper bound defines maximum jump distance
- Lower bound prevents very short jumps (optional)

### Graph Threshold

For performance, there's a limit on the number of stars:

| Stars | Behavior |
|-------|----------|
| ≤ 1500 | Normal route finding |
| > 1500 | Error message, use dataset-wide search instead |

## Event Integration

The routing system publishes events for cross-component communication:

| Event | When Published |
|-------|----------------|
| `NewRouteEvent` | Route created or finished |
| `RoutingStatusEvent` | Manual routing starts/stops |
| `DisplayRouteEvent` | Route visibility toggled |
| `UpdateRouteEvent` | Route properties edited |
| `DeleteRouteEvent` | Route deleted |

## Partial Route Handling

When some stars in a route are not visible in the current viewport:

1. `PartialRouteUtils.findPartialRoutes()` extracts visible segments
2. Each continuous visible section becomes a separate partial route
3. Partial routes are rendered with the same color/style as the full route
4. When view changes, partial routes are recalculated

```
Full Route: A → B → C → D → E → F
Visible:    A   B       D   E
Partial 1:  A → B
Partial 2:      D → E
```

## Constants

Key constants from `RoutingConstants`:

| Constant | Value | Purpose |
|----------|-------|---------|
| `GRAPH_THRESHOLD` | 1500 | Max stars for in-view route finding |
| `DEFAULT_NUMBER_PATHS` | 3 | Default K for K-shortest paths |
| `DEFAULT_LINE_WIDTH` | 0.5 | Default route line thickness |
| `DEFAULT_UPPER_DISTANCE` | 8.0 | Default max jump distance (ly) |
| `LABEL_CLIPPING_PADDING` | 20.0 | Padding for label visibility |
| `LABEL_EDGE_MARGIN` | 5.0 | Margin for label clamping |

## Testing

Tests for the routing package should cover:

- `RouteFindingService` - algorithm correctness, pruning logic
- `RouteGraph` - graph construction, connectivity, path finding
- `PartialRouteUtils` - edge cases for visibility extraction
- `RouteGraphicsUtil` - coordinate transformations

Run routing tests with:
```bash
./mvnw-java17.sh test -Dtest="com.teamgannon.trips.routing.**"
```

## Performance Considerations

1. **Large Datasets**: Use `RouteFinderDataset` with async `LargeGraphSearchService`
2. **Many Routes**: Label updates occur on every viewport change
3. **Complex Paths**: Yen's algorithm complexity increases with K
4. **Partial Routes**: Recalculated on each view change

## Known Limitations

1. **Graph Threshold**: 1500 star limit for in-view route finding
2. **No Route Caching**: Routes recalculated each time
3. **Label Performance**: Many labels may cause frame drops
4. **Single Thread**: Route finding is synchronous for in-view mode
