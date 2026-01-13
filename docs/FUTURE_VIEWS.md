# Future View Types for TRIPS

This document outlines the pluggable view architecture and planned future view types.

---

# CORE CONCEPT: One View = One 3D Pane + One Side Pane

**Each view type is a COMPLETE, INDEPENDENT pair:**

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           INTERSTELLAR VIEW                             │
├─────────────────────────────────────┬───────────────────────────────────┤
│                                     │                                   │
│     InterstellarSpacePane           │     InterstellarSidePane          │
│     (3D star field)                 │     (6 TitledPanes)               │
│                                     │                                   │
│     - Stars as points               │     1. DataSets Available         │
│     - Routes between stars          │     2. Objects in View            │
│     - Grid overlay                  │     3. Planetary Systems          │
│                                     │     4. Stellar Object Properties  │
│                                     │     5. Link Control               │
│                                     │     6. Star Routing               │
│                                     │                                   │
└─────────────────────────────────────┴───────────────────────────────────┘

                              ↓ "Enter System" on star ↓

┌─────────────────────────────────────────────────────────────────────────┐
│                           SOLAR SYSTEM VIEW                             │
├─────────────────────────────────────┬───────────────────────────────────┤
│                                     │                                   │
│     SolarSystemSpacePane            │     SolarSystemSidePane           │
│     (3D orbital view)               │     (4 TitledPanes)               │
│                                     │                                   │
│     - Central star                  │     1. System Overview            │
│     - Planet orbits                 │     2. Planets & Moons            │
│     - Habitable zone ring           │     3. Selected Object            │
│                                     │     4. Display & Controls         │
│                                     │                                   │
└─────────────────────────────────────┴───────────────────────────────────┘
```

**When you switch views, BOTH panes switch together. The side pane is NOT shared - each view has its own dedicated side pane.**

---

## How the Switching Works

The `RightPanelController` contains a `StackPane` that holds multiple **complete, separate side panes** stacked on top of each other. Only ONE is visible at a time.

```
RightPanelController.contextStackPane (StackPane)
│
├── [BEHIND] SolarSystemSidePane   ← Complete side pane for solar system
│             └── Accordion with 4 TitledPanes
│
└── [FRONT]  InterstellarSidePane  ← Complete side pane for interstellar
              └── Accordion with 6 TitledPanes
```

**When user clicks "Enter System" on a star:**
1. `ContextSelectorEvent(SOLARSYSTEM, star)` is fired
2. `LeftDisplayController.showSolarSystem(star)` → switches 3D pane
3. `RightPanelCoordinator.switchToSolarSystem(star)` → switches side pane
   - `SolarSystemSidePane.setSystem(system)` ← loads star's system data
   - `SolarSystemSidePane.toFront()` ← brings solar system side pane to front

**When user clicks "Jump Back":**
1. `ContextSelectorEvent(INTERSTELLAR)` is fired
2. `LeftDisplayController.showInterstellar()` → switches 3D pane
3. `RightPanelCoordinator.switchToInterstellar()` → switches side pane
   - `InterstellarSidePane.toFront()` ← brings interstellar side pane to front

---

# THE TWO IMPLEMENTED SIDE PANES

---

## Side Pane #1: Interstellar Side Pane

**When visible:** Interstellar (star neighborhood) view is active

**Class:** Existing `Accordion` (`propertiesAccordion`) in `RightPanelController`

**Contains 6 TitledPanes:**

| # | TitledPane | Purpose |
|---|------------|---------|
| 1 | DataSets Available | Select which dataset to display |
| 2 | Objects in View | List of stars currently plotted |
| 3 | Planetary Systems | Stars known to have planets |
| 4 | Stellar Object Properties | Details of selected star |
| 5 | Link Control | Transit/jump filtering |
| 6 | Star Routing | Route planning and display |

---

## Side Pane #2: Solar System Side Pane

**When visible:** Solar System (orbital) view is active

**Class:** `SolarSystemSidePane` (extends VBox, contains Accordion)

**Contains 4 TitledPanes:**

| # | TitledPane | Purpose |
|---|------------|---------|
| 1 | System Overview | Star info, habitable zone, planet/moon counts |
| 2 | Planets & Moons | List of planets with context menu |
| 3 | Selected Object | Properties of clicked planet or star |
| 4 | Display & Controls | Animation, scale, visibility toggles |

**Context:** When switching to this side pane, it receives the `SolarSystemDescription` for the star system being viewed, so all content is specific to that system.

---

# FUTURE SIDE PANES

---

## Side Pane #3: Galactic Side Pane (Future)

**When visible:** Galactic (galaxy-wide) view is active

**Class:** `GalacticSidePane`

**Contains 4 TitledPanes:**

| # | TitledPane | Purpose |
|---|------------|---------|
| 1 | Galactic Position | Dataset position in galaxy, distance from center, galactic arm |
| 2 | Dataset Coverage | Volume covered, star count, % of galaxy explored |
| 3 | Region Statistics | Spectral class distribution, notable objects, clusters |
| 4 | Navigation Controls | Galactic coordinate input, jump to regions, zoom |

---

## Side Pane #4: Planetary Side Pane (Future)

**When visible:** Planetary (night sky from surface) view is active

**Class:** `PlanetarySidePane`

**Contains 5 TitledPanes:**

| # | TitledPane | Purpose |
|---|------------|---------|
| 1 | Viewing Location | Planet name, surface position, local time, viewing direction |
| 2 | Sky Overview | Visible star count, host star position, sibling planets, moons |
| 3 | Brightest Stars | Top 20 brightest from this location, click to highlight |
| 4 | Constellation Guide | Earth constellations visible, distortion level, toggle lines |
| 5 | View Controls | Time slider, direction, magnitude limit, FOV, atmosphere toggle |

---

# FUTURE 3D PANES

---

## 3D Pane #3: Galactic View (Future)

**Pairs with:** Side Pane #3 (GalacticSidePane)

**Class:** `GalacticSpacePlane` (placeholder exists)

**Concept:** Zoomed-out view showing Milky Way galaxy structure with dataset stars plotted in galactic positions.

**Visualization:**
- Spiral arm structure as background
- Current dataset as bright points (color by spectral class)
- "You are here" Sol indicator
- Logarithmic scaling (galaxy ~100,000 ly, typical dataset <1000 ly)

**Technical:** `AstrographicTransformer` already supports galactic coordinates.

---

## 3D Pane #4: Planetary View (Future)

**Pairs with:** Side Pane #4 (PlanetarySidePane)

**Class:** `PlanetarySkyPane`

**Concept:** Night sky from a planet's surface, showing how stars appear from that world.

**Visualization:**
- Sky dome projection
- Stars repositioned for planet's 3D location
- Host star as sun (day/night effect)
- Magnitude adjusted: m_new = m_old + 5*log10(d_new/d_old)
- Constellation distortion analysis

**Technical:** Orekit library for orbital calculations, planet elements in `PlanetDescription`.

---

# COMPLETE VIEW HIERARCHY

Each level = One 3D Pane + One Side Pane (switching together)

```
┌──────────────────────────────────────────────────────────────────┐
│  GALACTIC VIEW (future)                                          │
│  3D: GalacticSpacePlane    Side: GalacticSidePane               │
└──────────────────────────────────────────────────────────────────┘
                              │
                              │  Click on region
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  INTERSTELLAR VIEW (implemented) ← Default                       │
│  3D: InterstellarSpacePane    Side: InterstellarSidePane        │
└──────────────────────────────────────────────────────────────────┘
                              │
                              │  "Enter System" on star
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  SOLAR SYSTEM VIEW (implemented)                                 │
│  3D: SolarSystemSpacePane    Side: SolarSystemSidePane          │
└──────────────────────────────────────────────────────────────────┘
                              │
                              │  "Land on Planet"
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  PLANETARY VIEW (future)                                         │
│  3D: PlanetarySkyPane    Side: PlanetarySidePane                │
└──────────────────────────────────────────────────────────────────┘
```

---

# SUMMARY TABLE

| View | 3D Pane | Side Pane | Status |
|------|---------|-----------|--------|
| Galactic | `GalacticSpacePlane` | `GalacticSidePane` (4 panes) | Future |
| **Interstellar** | `InterstellarSpacePane` | `InterstellarSidePane` (6 panes) | **Implemented** |
| **Solar System** | `SolarSystemSpacePane` | `SolarSystemSidePane` (4 panes) | **Implemented** |
| Planetary | `PlanetarySkyPane` | `PlanetarySidePane` (5 panes) | Future |

---

## ContextSelectionType Enum

```java
public enum ContextSelectionType {
    INTERSTELLAR,  // implemented - 3D pane + side pane
    SOLARSYSTEM,   // implemented - 3D pane + side pane
    GALACTIC,      // future - 3D pane + side pane
    PLANETARY      // future - 3D pane + side pane
}
```

**Key Point:** Each enum value triggers switching BOTH the 3D pane (left) AND the side pane (right) together as a matched pair.
