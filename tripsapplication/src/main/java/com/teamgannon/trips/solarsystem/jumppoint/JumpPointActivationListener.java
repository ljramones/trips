package com.teamgannon.trips.solarsystem.jumppoint;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.SolarSystemFeature;
import com.teamgannon.trips.jpa.repository.SolarSystemFeatureRepository;
import com.teamgannon.trips.model.SolarSystemDescription;
import com.teamgannon.trips.solarsystem.SolarSystemActivatedEvent;
import javafx.geometry.Point3D;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Listens for {@link SolarSystemActivatedEvent} and ensures each star in the activated system
 * has its deterministic jump-point feature persisted.
 *
 * <p>v2 Phase E.1 §7.3 — the first behavioral change in the running app for E.1: activating a
 * system (via the "Enter System..." or "Generate Simulated Solar System..." context-menu paths)
 * now triggers per-star jump-point computation + persistence. Idempotent at the row level via
 * {@code findByParentBodyIdAndFeatureType}: repeat activations of the same system find the
 * existing feature and skip computation.
 *
 * <p>Threading: the event is published synchronously from
 * {@code SolarSystemSpacePane.render()} on the JavaFX Application Thread. This listener does
 * JPA writes only — no scene-graph mutation — so it doesn't need {@code FxThread.runOnFxThread}
 * wrapping. JPA writes on the FX thread are acceptable for the 1-3-stars-per-system scale of
 * E.1; if future work makes the activation work more expensive (E.2's catalog-reference
 * population, etc.), this may need to move to a background task.
 *
 * <p>Graceful failure: any throwable from the computer or repository is caught and logged.
 * Activation succeeds even if jump-point persistence fails — the user can still navigate the
 * system; the missing feature regenerates on the next activation (deterministic seed yields the
 * same coordinates).
 */
@Slf4j
@Component
public class JumpPointActivationListener {

    private final SolarSystemFeatureRepository featureRepository;
    private final JumpPointComputer jumpPointComputer;

    public JumpPointActivationListener(SolarSystemFeatureRepository featureRepository,
                                       JumpPointComputer jumpPointComputer) {
        this.featureRepository = featureRepository;
        this.jumpPointComputer = jumpPointComputer;
    }

    /**
     * Per-event entry point. Iterates the system's stars (primary + companions) and ensures each
     * has a JUMP_POINT feature persisted. {@code @Transactional} so the per-star save() calls
     * share an ambient transaction; rollback on exception is the standard JPA contract.
     */
    @EventListener
    @Transactional
    public void onSystemActivated(SolarSystemActivatedEvent event) {
        SolarSystemDescription system = event.system();
        if (system == null || system.getSolarSystemId() == null) {
            return;
        }
        try {
            int inserted = 0;
            for (StarDisplayRecord star : allStars(system)) {
                if (ensureJumpPointForStar(system, star)) {
                    inserted++;
                }
            }
            if (inserted > 0) {
                log.info("Jump-point activation: inserted {} new JUMP_POINT feature(s) for system {}",
                        inserted, system.getSolarSystemId());
            }
        } catch (Exception e) {
            log.error("Jump-point activation failed for system {}; user can still navigate the "
                    + "system, and re-activation will retry persistence (deterministic seed)",
                    system.getSolarSystemId(), e);
        }
    }

    /**
     * Ensures a single star's JUMP_POINT feature exists. Returns {@code true} if a new feature
     * was inserted; {@code false} if the feature already existed or the star is jump-inaccessible.
     */
    private boolean ensureJumpPointForStar(SolarSystemDescription system, StarDisplayRecord star) {
        if (star == null || star.getRecordId() == null) {
            return false;
        }
        // Idempotency check: skip if already computed.
        List<SolarSystemFeature> existing = featureRepository.findByParentBodyIdAndFeatureType(
                star.getRecordId(), SolarSystemFeature.FeatureType.JUMP_POINT);
        if (!existing.isEmpty()) {
            return false;
        }
        // Compute deterministically.
        Optional<Point3D> position = jumpPointComputer.compute(star, system);
        if (position.isEmpty()) {
            // Star is jump-inaccessible (100-iteration cap exhausted). Absence-of-feature is
            // the state per §3.1; no feature created.
            log.debug("Star {} is jump-inaccessible (no clear position in 100 iterations); "
                    + "no JUMP_POINT feature created", star.getRecordId());
            return false;
        }
        // Persist as a new SolarSystemFeature.
        SolarSystemFeature feature = buildJumpPointFeature(system, star, position.get());
        featureRepository.save(feature);
        return true;
    }

    /**
     * Builds the {@link SolarSystemFeature} row for a jump point. Cartesian → spherical
     * coordinates for the entity's column layout: {@code orbitalRadiusAU} is the projection
     * onto the xy-plane, {@code orbitalAngleDeg} is the azimuthal angle in degrees, and
     * {@code orbitalHeightAU} is the z-component. Matches the convention used by
     * {@code SolarSystemRenderer.renderPointFeature} for inverse conversion.
     */
    private SolarSystemFeature buildJumpPointFeature(SolarSystemDescription system,
                                                     StarDisplayRecord star,
                                                     Point3D position) {
        SolarSystemFeature f = new SolarSystemFeature();
        f.setSolarSystemId(system.getSolarSystemId());
        f.setParentBodyId(star.getRecordId());
        f.setFeatureType(SolarSystemFeature.FeatureType.JUMP_POINT);
        f.setFeatureCategory(SolarSystemFeature.FeatureCategory.NATURAL);
        f.setName(buildJumpPointName(star, system));

        // Cartesian → spherical-ish (radius in xy, angle in xy, height = z).
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();
        double orbitalRadiusAU = Math.sqrt(x * x + y * y);
        double orbitalAngleDeg = Math.toDegrees(Math.atan2(y, x));
        if (orbitalAngleDeg < 0) {
            orbitalAngleDeg += 360.0;
        }
        f.setOrbitalRadiusAU(orbitalRadiusAU);
        f.setOrbitalAngleDeg(orbitalAngleDeg);
        f.setOrbitalHeightAU(z);
        return f;
    }

    /**
     * Human-readable name for a jump-point feature. Single-star systems get just "Jump Point";
     * multi-star systems get "{star name} Jump Point" so the per-star scope is visible in the
     * UI.
     */
    private String buildJumpPointName(StarDisplayRecord star, SolarSystemDescription system) {
        if (system.isMultiStarSystem() && star.getStarName() != null && !star.getStarName().isBlank()) {
            return star.getStarName() + " Jump Point";
        }
        return "Jump Point";
    }

    /**
     * Combined primary + companion stars in the activated system. Each star gets its own
     * per-star jump-point feature per §3.4.
     */
    private List<StarDisplayRecord> allStars(SolarSystemDescription system) {
        List<StarDisplayRecord> stars = new ArrayList<>();
        if (system.getStarDisplayRecord() != null) {
            stars.add(system.getStarDisplayRecord());
        }
        if (system.getCompanionStars() != null) {
            stars.addAll(system.getCompanionStars());
        }
        return stars;
    }
}
