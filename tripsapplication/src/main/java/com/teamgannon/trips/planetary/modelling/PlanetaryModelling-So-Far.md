# TRIPS Planetary Modelling TODO
**Last updated:** January 17, 2026  
**Current milestone:** Accrete → Tectonics bridge complete & stable (full integration, stagnant-lid mode, hotspots, scaling)

## Done ✅ (Major Wins)
- Modernized Accrete physics (Kothari radius, Kopparapu HZ, validation fixes, albedo cooling, etc.)
- Full Java port of Victor Gordan-style Goldberg polyhedron + plate tectonics
- Accrete → Procedural Planet bridge:
    - TectonicBias record with physics-based mapping
    - oceanicPlateRatio, height/rift multipliers, hotspot probability, active/stagnant-lid mode
    - Consumed in BoundaryDetector, ElevationCalculator, hotspots
- PlanetGenerator helpers: createBiasedConfig(), generateFromAccrete()
- 3D JavaFX solar system viewer + detailed planet UI
- Planetarium-grade night sky
- Real astro data connectors (Gaia, Vizier, SIMBAD, NASA Exoplanet Archive)

## Immediate / Safety Net (Next 1–2 hours)
- [ ] Add validation guard in StarSystem constructor (throw on mass/luminosity/temp ≤ 0)
- [ ] Add UI warning dialog when generating with invalid star data
- [ ] Optional: fallback defaults + logging for zeroed stars (Sun-like rescue mode)

## High Priority Next Features (This Weekend / High Impact)
1. **Erosion + Weathering + Rivers + Coastal Smoothing**
    - Lightweight downhill sediment flow on polygon mesh
    - Rainfall from climate zones → river carving → valley smoothing
    - Coastal erosion for realistic fractal shorelines
    - Expected effort: 1–3 days | Visual realism boost: ★★★★★

2. **Tidally-Locked World Visuals & Climate**
    - Dayside/nightside temperature split visualization
    - Heat transport bias (thick atm → more uniform temp)
    - Possible night-side city lights or aurora glow
    - Effort: 1–2 days | Demo impact: ★★★★★

3. **Bulk Stellar Data Cleanup Script**
    - Scan & report/fix zeroed parameters (mass, L, T) using SIMBAD/Gaia lookups
    - Effort: 2–5 days | Unlocks 1000s of real systems

## Medium-Term / Nice-to-Have (Next 1–2 Sprints)
- Moon system improvements (tidal heating → volcanism, capture logic)
- Basic planetary migration stub (Type I/II, Grand Tack-like)
- Biome painting (from climate + elevation + water)
- Export pipeline (glTF/OBJ + JSON save/load)
- Performance: higher-res meshes (COLOSSAL size without slowdown)

## Low Priority / Future
- Full erosion cycles (multi-pass weathering)
- Simple life indicators / biosignatures
- River network routing + deltas
- Atmospheric scattering shaders in JavaFX

**Current Focus Recommendation:**  
**Start with erosion/weathering.**  
It’s the single biggest realism upgrade left after tectonics — turns abstract mountains/rifts into believable Earth/Mars/Venus landscapes.  
Once that’s in, tidally-locked visuals will make the habitable candidates pop.

Feel free to comment, reorder, or add your own items — this is a living doc!