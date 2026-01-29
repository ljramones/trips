/**
 * Particle Field Rendering System.
 *
 * <p>This package provides a flexible system for rendering distributed particle structures
 * in 3D space. It supports various astronomical and sci-fi structures:
 *
 * <h2>Structure Types</h2>
 * <ul>
 *   <li><b>Planetary Rings</b> - Saturn-like and Uranus-like ring systems</li>
 *   <li><b>Asteroid Belts</b> - Main belt, Kuiper belt structures</li>
 *   <li><b>Debris Disks</b> - Protoplanetary disks, collision remnants</li>
 *   <li><b>Dust Clouds</b> - Emission nebulae, dark nebulae, reflection nebulae</li>
 *   <li><b>Accretion Disks</b> - Black hole and neutron star accretion</li>
 * </ul>
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link com.teamgannon.trips.particlefields.RingFieldRenderer} - Main renderer for particle fields</li>
 *   <li>{@link com.teamgannon.trips.particlefields.RingConfiguration} - Configuration record for ring parameters</li>
 *   <li>{@link com.teamgannon.trips.particlefields.RingFieldFactory} - Factory with presets for common structures</li>
 *   <li>{@link com.teamgannon.trips.particlefields.RingFieldWindow} - Standalone visualization window</li>
 * </ul>
 *
 * <h2>Scale Adapters</h2>
 * <ul>
 *   <li>{@link com.teamgannon.trips.particlefields.SolarSystemRingAdapter} - Converts AU to screen coordinates</li>
 *   <li>{@link com.teamgannon.trips.particlefields.InterstellarRingAdapter} - Converts light-years to screen</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create a Saturn-like ring
 * RingConfiguration config = RingFieldFactory.saturnRing();
 * RingFieldRenderer renderer = new RingFieldRenderer(config, new Random(42));
 * parentGroup.getChildren().add(renderer.getGroup());
 *
 * // In animation loop:
 * renderer.update(timeScale);
 * renderer.refreshMeshes();  // Periodically, not every frame
 * }</pre>
 *
 * @see com.teamgannon.trips.solarsystem.rendering.SolarSystemRenderer
 */
package com.teamgannon.trips.particlefields;
