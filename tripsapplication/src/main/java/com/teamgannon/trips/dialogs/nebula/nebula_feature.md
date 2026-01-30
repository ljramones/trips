# Nebula Visualization Feature

## Overview

The Nebula Visualization feature allows users to create, import, edit, and visualize nebulae in the interstellar 3D view. Nebulae are rendered as particle clouds using procedural generation, creating visually rich representations of emission nebulae, dark nebulae, planetary nebulae, reflection nebulae, and supernova remnants.

This feature integrates with the existing particle fields system (`RingFieldRenderer`, `DustCloudGenerator`) to render nebulae as 3D point clouds with configurable colors, density distributions, and filamentary structure.

## Architecture

### Component Layers

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI Layer                                  │
├─────────────────────────────────────────────────────────────────┤
│  ViewMenu.fxml          │  Menu items for nebula management     │
│  NebulaListDialog       │  List/edit/delete nebulae in dataset  │
│  NebulaEditorDialog     │  Create/edit individual nebulae       │
│  NebulaCatalogImportDialog │ Import from Messier/NGC catalogs   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Service Layer                               │
├─────────────────────────────────────────────────────────────────┤
│  NebulaService          │  CRUD operations for Nebula entities  │
│  NebulaCatalogService   │  Built-in Messier/NGC catalog data    │
│  NebulaConfigConverter  │  Converts Nebula → RingConfiguration  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Rendering Layer                               │
├─────────────────────────────────────────────────────────────────┤
│  NebulaManager          │  Orchestrates rendering in 3D view    │
│  RingFieldRenderer      │  Renders particle clouds              │
│  DustCloudGenerator     │  Generates 3D particle distributions  │
│  NoiseGenerator         │  Creates filamentary structure        │
│  InterstellarRingAdapter│  Converts light-years to screen units │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Persistence Layer                              │
├─────────────────────────────────────────────────────────────────┤
│  Nebula (JPA Entity)    │  Stores nebula data in database       │
│  NebulaType (Enum)      │  Nebula classification with defaults  │
│  NebulaRepository       │  Database queries including spatial   │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow

1. **Creation/Import**: User creates nebula via dialog or imports from catalog
2. **Persistence**: `NebulaService` saves `Nebula` entity to database
3. **Plot Trigger**: When stars are plotted, `PlotManager` calls `renderNebulae()`
4. **Query**: `NebulaManager` queries nebulae within plot range via `NebulaRepository`
5. **Conversion**: `NebulaConfigConverter` converts each `Nebula` to `RingConfiguration`
6. **Generation**: `DustCloudGenerator` creates particle positions using procedural algorithms
7. **Rendering**: `RingFieldRenderer` creates JavaFX 3D meshes and adds to scene

## Data Model

### Nebula Entity

The `Nebula` JPA entity stores all configuration needed to procedurally generate a nebula:

```java
@Entity(name = "NEBULA")
public class Nebula {
    // Identity
    String id;              // UUID
    String name;            // Display name (e.g., "Orion Nebula")
    String dataSetName;     // Dataset this nebula belongs to
    NebulaType type;        // EMISSION, DARK, REFLECTION, PLANETARY, SUPERNOVA_REMNANT

    // Position (light-years from Sol)
    double centerX, centerY, centerZ;

    // Size (light-years)
    double innerRadius;     // Hollow center (0 for filled)
    double outerRadius;     // Outer boundary

    // Procedural Generation Parameters
    double radialPower;     // Density falloff (0.3=core, 0.7=shell)
    double noiseStrength;   // Filament intensity (0.0-1.0)
    int noiseOctaves;       // Detail level (1-6)
    long seed;              // Random seed for reproducibility

    // Particle Count
    double particleDensity;      // Particles per cubic light-year
    Integer numElementsOverride; // Explicit count (overrides density)

    // Appearance
    String primaryColor;    // Hex color (e.g., "#FF6496")
    String secondaryColor;  // Hex color for gradient
    double opacity;         // Overall opacity (0.0-1.0)

    // Animation
    boolean enableAnimation;
    double baseAngularSpeed;

    // Catalog Metadata
    String catalogId;       // e.g., "M42", "NGC 1976"
    String sourceCatalog;   // e.g., "Messier", "User"
    String notes;
}
```

### NebulaType Enum

Each type has default visual parameters:

| Type | Description | Radial Power | Noise | Colors |
|------|-------------|--------------|-------|--------|
| EMISSION | Glowing ionized gas | 0.4 (core) | 0.4 | Pink/Cyan |
| DARK | Obscuring dust | 0.5 (uniform) | 0.3 | Dark brown/Black |
| REFLECTION | Reflecting starlight | 0.35 (concentrated) | 0.2 | Blue |
| PLANETARY | Expanding shell | 0.7 (shell-like) | 0.25 | Green/Purple |
| SUPERNOVA_REMNANT | Explosion debris | 0.65 (shell) | 0.6 | Orange/Yellow |

## Procedural Generation

### Radial Power Distribution

Controls how particles are distributed from center to edge:

```
r = innerRadius + (outerRadius - innerRadius) * u^radialPower
```

Where `u` is uniform random [0,1].

- **Low values (0.3-0.4)**: Dense core, particles concentrated at center
- **Medium values (0.5)**: Uniform distribution
- **High values (0.6-0.8)**: Shell-like, particles concentrated at edges

### Noise-Based Filaments

The `NoiseGenerator` creates filamentary structure:

1. **3D Position**: Particle placed at spherical coordinates
2. **Noise Sampling**: Sample 3D noise at particle position
3. **Displacement**: Offset particle based on noise value
4. **Anisotropy**: Different displacement strengths per axis create elongated filaments

```java
double[] displacement = noise.filamentDisplacement(x, y, z, octaves, strength);
// Anisotropic factors: [1.0, 0.7, 0.4] create horizontal streaks
```

### Color Gradients

Particles are colored with core-biased gradients:

1. **Base Color**: Interpolate between primary and secondary colors
2. **Core Brightness**: Particles near center are brighter/more saturated
3. **Noise Variation**: Subtle hue shifts based on position noise
4. **Opacity**: Higher near core, more transparent at edges

## UI Components

### NebulaListDialog

**Location**: View > Manage Nebulae...

Lists all nebulae in the current dataset with options to:

| Action | Description |
|--------|-------------|
| Add New | Opens NebulaEditorDialog in create mode |
| Edit | Opens NebulaEditorDialog for selected nebula |
| Duplicate | Creates copy with new name and seed |
| Delete | Removes nebula (with confirmation) |

**Table Columns**: Name, Type, Position, Radius, Catalog ID, Source

**Filter**: Dropdown to filter by nebula type

### NebulaEditorDialog

**Tabs**:

1. **General**
   - Name, Type (dropdown with auto-defaults)
   - Dataset (read-only)
   - Position: X, Y, Z coordinates (light-years)
   - Size: Inner radius, Outer radius (light-years)
   - Catalog ID, Source catalog

2. **Appearance**
   - Primary Color (color picker)
   - Secondary Color (color picker)
   - Opacity slider (0.1 - 1.0)
   - Animation toggle and speed

3. **Procedural**
   - Radial Power slider (0.1 - 1.0) with hint
   - Noise Strength slider (0.0 - 1.0)
   - Noise Octaves spinner (1 - 6)
   - Seed field with Randomize button
   - Particle Density
   - Particle Override (explicit count)

### NebulaCatalogImportDialog

**Location**: View > Import Nebulae from Catalog...

Browse and import from built-in astronomical catalogs:

**Filters**:
- Type dropdown (All Types, Emission, Planetary, etc.)
- Catalog dropdown (All Catalogs, Messier, NGC, Barnard)
- Search text field (searches name, ID, constellation)

**Selection**:
- Checkbox column for multi-select
- Select All / Select None / Invert buttons
- Selection count display

**Columns**: Catalog ID, Name, Type, Distance, Radius, Constellation, Source

## How to Use

### Creating a Custom Nebula

1. Load a dataset (File > Open Dataset or select from loaded datasets)
2. Go to **View > Manage Nebulae...**
3. Click **Add New...**
4. In the General tab:
   - Enter a name (e.g., "My Nebula")
   - Select a type (defaults will be applied)
   - Set position in X, Y, Z coordinates (light-years from Sol)
   - Set outer radius (size in light-years)
5. (Optional) Customize Appearance tab:
   - Pick colors using color pickers
   - Adjust opacity
6. (Optional) Customize Procedural tab:
   - Adjust radial power for density distribution
   - Adjust noise strength for filament intensity
7. Click **Create**
8. Close the list dialog - nebula will appear on next plot

### Importing from Catalogs

1. Load a dataset
2. Go to **View > Import Nebulae from Catalog...**
3. Browse the 24 built-in nebulae
4. Use filters to narrow selection:
   - Select "Planetary" type to see only planetary nebulae
   - Select "Messier" catalog to see only Messier objects
   - Type "Orion" in search to find Orion-related objects
5. Check the boxes next to nebulae you want to import
   - Or click **Select All** to import everything
6. Click **Import Selected**
7. Nebulae are added to dataset and rendered when you re-plot

### Editing Existing Nebulae

1. Go to **View > Manage Nebulae...**
2. Select a nebula in the table
3. Click **Edit...** (or double-click the row)
4. Modify properties as desired
5. Click **Save**
6. Close dialog and re-plot to see changes

### Tips for Good-Looking Nebulae

1. **Size**: Keep outer radius proportional to distance from Sol. A nebula 1000 ly away with radius 5-20 ly looks reasonable.

2. **Type Selection**: Start with an appropriate type - it sets good defaults:
   - Use EMISSION for bright, colorful nebulae
   - Use PLANETARY for ring/shell shapes
   - Use DARK for obscuring dust clouds

3. **Procedural Tuning**:
   - Lower radial power (0.3-0.4) for concentrated cores
   - Higher radial power (0.6-0.7) for hollow shells
   - Increase noise strength for more dramatic filaments
   - Add octaves for finer detail (but higher rendering cost)

4. **Colors**:
   - Emission nebulae: Pink/red primary, blue/cyan secondary
   - Reflection nebulae: Blues and light blues
   - Dark nebulae: Very dark browns, near-black

5. **Animation**: Enable for slow, turbulent motion effect

## Built-in Catalog

The following nebulae are available for import:

### Messier Catalog (12 objects)

| ID | Name | Type | Distance | Constellation |
|----|------|------|----------|---------------|
| M1 | Crab Nebula | Supernova Remnant | 6,500 ly | Taurus |
| M8 | Lagoon Nebula | Emission | 5,200 ly | Sagittarius |
| M16 | Eagle Nebula | Emission | 7,000 ly | Serpens |
| M17 | Omega Nebula | Emission | 5,000 ly | Sagittarius |
| M20 | Trifid Nebula | Emission | 5,200 ly | Sagittarius |
| M27 | Dumbbell Nebula | Planetary | 1,360 ly | Vulpecula |
| M42 | Orion Nebula | Emission | 1,344 ly | Orion |
| M43 | De Mairan's Nebula | Emission | 1,600 ly | Orion |
| M57 | Ring Nebula | Planetary | 2,300 ly | Lyra |
| M76 | Little Dumbbell | Planetary | 2,500 ly | Perseus |
| M78 | M78 | Reflection | 1,600 ly | Orion |
| M97 | Owl Nebula | Planetary | 2,030 ly | Ursa Major |

### NGC/Other Catalogs (12 objects)

| ID | Name | Type | Distance | Constellation |
|----|------|------|----------|---------------|
| NGC 7000 | North America Nebula | Emission | 2,200 ly | Cygnus |
| NGC 7293 | Helix Nebula | Planetary | 700 ly | Aquarius |
| NGC 6543 | Cat's Eye Nebula | Planetary | 3,300 ly | Draco |
| NGC 2392 | Eskimo Nebula | Planetary | 3,000 ly | Gemini |
| NGC 6960 | Western Veil | Supernova Remnant | 2,400 ly | Cygnus |
| NGC 6992 | Eastern Veil | Supernova Remnant | 2,400 ly | Cygnus |
| NGC 2237 | Rosette Nebula | Emission | 5,200 ly | Monoceros |
| B33 | Horsehead Nebula | Dark | 1,500 ly | Orion |
| NGC 6826 | Blinking Planetary | Planetary | 2,200 ly | Cygnus |
| NGC 1499 | California Nebula | Emission | 1,000 ly | Perseus |
| NGC 2024 | Flame Nebula | Emission | 1,350 ly | Orion |
| NGC 6888 | Crescent Nebula | Emission | 4,700 ly | Cygnus |

## API Reference

### NebulaService

```java
// CRUD
Nebula save(Nebula nebula);
Optional<Nebula> findById(String id);
boolean delete(String id);
void delete(Nebula nebula);

// Queries
List<Nebula> findByDataset(String datasetName);
List<Nebula> findByDatasetAndType(String datasetName, NebulaType type);
List<Nebula> findInPlotRange(String datasetName, double cx, double cy, double cz, double radius);

// Utilities
Nebula createWithDefaults(String name, NebulaType type, String datasetName,
                          double x, double y, double z, double radius);
Nebula duplicate(Nebula original, String newName);
boolean existsByName(String datasetName, String name);
long countByDataset(String datasetName);
```

### NebulaCatalogService

```java
List<NebulaCatalogEntry> getAllEntries();
List<NebulaCatalogEntry> getEntriesByType(NebulaType type);
List<NebulaCatalogEntry> getEntriesByCatalog(String sourceCatalog);
List<NebulaCatalogEntry> searchByName(String query);
Optional<NebulaCatalogEntry> getEntryById(String catalogId);
List<String> getSourceCatalogs();
int importToDataset(List<NebulaCatalogEntry> entries, String datasetName);
```

### NebulaManager

```java
void setParentGroup(Group parentGroup);
void setAdapter(InterstellarRingAdapter adapter);
void renderNebulaeInRange(String datasetName, double cx, double cy, double cz, double plotRadius);
void clearRenderers();
void updateAnimation(double timeScale);
void setVisible(boolean visible);
void setAnimationEnabled(boolean enabled);
int getActiveNebulaCount();
int getTotalParticleCount();
List<String> getActiveNebulaNames();
```

### InterstellarSpacePane (Nebula Methods)

```java
void renderNebulae(String datasetName, double cx, double cy, double cz,
                   double plotRadius, double scalingFactor);
void clearNebulae();
void toggleNebulae(boolean visible);
void toggleNebulaAnimation(boolean enabled);
void updateNebulaAnimation(double timeScale);
int getActiveNebulaCount();
```

## Performance Considerations

1. **Particle Count**: Each nebula generates thousands of particles. The system uses LOD (Level of Detail) to reduce particle count for distant nebulae:
   - < 10 ly: 100% particles
   - 10-50 ly: 50-100% particles
   - 50-100 ly: 20-50% particles
   - > 100 ly: 10% particles

2. **Mesh Refresh**: Particle meshes are refreshed every 5 frames, not every frame, to reduce GPU load.

3. **Plot Range**: Only nebulae intersecting the plot sphere are rendered. Use `NebulaRepository.findInPlotRange()` spatial query.

4. **Maximum Particles**: Clamped to 100,000 particles per nebula regardless of density settings.

## File Locations

```
com.teamgannon.trips/
├── jpa/model/
│   ├── Nebula.java              # JPA entity
│   └── NebulaType.java          # Type enum with defaults
├── jpa/repository/
│   └── NebulaRepository.java    # Database queries
├── service/
│   ├── NebulaService.java       # CRUD operations
│   └── NebulaConfigConverter.java # Entity → RingConfiguration
├── starplotting/
│   └── NebulaManager.java       # Rendering orchestration
├── particlefields/
│   ├── InterstellarRingAdapter.java # Coordinate scaling
│   ├── RingFieldRenderer.java   # Particle rendering
│   ├── DustCloudGenerator.java  # Particle generation
│   └── NoiseGenerator.java      # Filament noise
├── dialogs/nebula/
│   ├── NebulaListDialog.java    # Management dialog
│   ├── NebulaEditorDialog.java  # Create/edit dialog
│   └── catalog/
│       ├── NebulaCatalogEntry.java    # Catalog data model
│       ├── NebulaCatalogService.java  # Built-in catalogs
│       └── NebulaCatalogImportDialog.java # Import dialog
├── graphics/panes/
│   └── InterstellarSpacePane.java # Integration point
├── graphics/
│   └── PlotManager.java         # Triggers nebula rendering
└── controller/menubar/
    └── ViewMenuController.java  # Menu handlers
```
