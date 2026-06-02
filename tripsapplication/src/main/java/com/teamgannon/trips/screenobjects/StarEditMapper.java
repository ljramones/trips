package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bidirectional mapper between {@link StarObject} (JPA entity) and
 * {@link StarEditViewModel} (the dialog's DTO) — Issue 23 / Phase 7-follow-on.
 *
 * <h2>Why a mapper, not the entity directly</h2>
 * <p>
 * The dialog used to bind form fields straight to the entity. That meant
 * touching {@code starObject.getAliasList()} on the FX thread threw
 * {@link org.hibernate.LazyInitializationException} the moment the dialog
 * opened outside the loading transaction. The fix in code was a defensive
 * {@code Hibernate.isInitialized(...)} check that hid — not solved — the
 * problem.
 * <p>
 * With this mapper the dialog never sees a live entity. The
 * {@link #toViewModel(StarObject)} call runs inside a transactional service
 * (so the alias collection gets {@link Hibernate#initialize materialised}
 * safely), and {@link #applyToEntity(StarEditViewModel, StarObject)} writes
 * the dialog's edits back to a freshly-loaded entity on the way to save.
 *
 * <h2>Stateless</h2>
 * Pure static methods — no Spring context, no DB calls, no side effects.
 * Unit-testable without TestFX or a Spring slice.
 */
public final class StarEditMapper {

    private StarEditMapper() {
    }

    /**
     * Build a fully-materialised view-model snapshot of {@code entity}.
     * <p>
     * Call this from within an active transaction (or with an attached
     * session) so the alias collection can be eagerly initialised. The
     * resulting view-model is detached from Hibernate and safe to hand
     * to the FX thread.
     */
    public static StarEditViewModel toViewModel(StarObject entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Cannot build a view-model from a null StarObject");
        }
        StarEditViewModel vm = new StarEditViewModel();

        // Identity
        vm.setId(entity.getId());
        vm.setDataSetName(entity.getDataSetName());

        // Overview
        vm.setDisplayName(nullToEmpty(entity.getDisplayName()));
        vm.setCommonName(nullToEmpty(entity.getCommonName()));
        vm.setConstellationName(nullToEmpty(entity.getConstellationName()));
        vm.setSpectralClass(nullToEmpty(entity.getSpectralClass()));
        vm.setDistance(entity.getDistance());
        vm.setMetallicity(entity.getMetallicity());
        vm.setAge(entity.getAge());
        vm.setX(entity.getX());
        vm.setY(entity.getY());
        vm.setZ(entity.getZ());
        vm.setNotes(nullToEmpty(entity.getNotes()));

        // Secondary / scientific
        vm.setSimbadId(nullToEmpty(entity.getSimbadId()));
        vm.setGalacticLat(entity.getGalacticLat());
        vm.setGalacticLong(entity.getGalacticLong());
        vm.setRadius(entity.getRadius());
        vm.setMass(entity.getMass());
        vm.setLuminosity(nullToEmpty(entity.getLuminosity()));
        vm.setTemperature(entity.getTemperature());
        vm.setRa(entity.getRa());
        vm.setDeclination(entity.getDeclination());
        vm.setPmra(entity.getPmra());
        vm.setPmdec(entity.getPmdec());
        vm.setParallax(entity.getParallax());
        vm.setRadialVelocity(entity.getRadialVelocity());
        vm.setBprp(entity.getBprp());
        vm.setBpg(entity.getBpg());
        vm.setGrp(entity.getGrp());
        vm.setMagu(entity.getMagu());
        vm.setMagb(entity.getMagb());
        vm.setMagv(entity.getMagv());
        vm.setMagr(entity.getMagr());
        vm.setMagi(entity.getMagi());
        vm.setGaiaDR2CatId(nullToEmpty(entity.getGaiaDR2CatId()));

        // Aliases — materialise inside the caller's transaction so the
        // view-model is detached-safe.
        Set<String> aliasList = entity.getAliasList();
        if (aliasList != null) {
            Hibernate.initialize(aliasList);
            vm.setAliases(new ArrayList<>(aliasList));
        } else {
            vm.setAliases(new ArrayList<>());
        }

        // Fictional / sci-fi worldbuilding fields removed by normalization task.

        // Display
        vm.setForceLabelToBeShown(entity.isForceLabelToBeShown());

        return vm;
    }

    /**
     * Apply the view-model's user-edited fields back onto a target entity.
     * Identity fields (id, dataSetName) are intentionally NOT written — they
     * stay whatever the entity already has. Aliases replace the entity's
     * existing collection in full.
     * <p>
     * Call this from within the save-side transaction.
     */
    public static void applyToEntity(StarEditViewModel vm, StarObject target) {
        if (vm == null) {
            throw new IllegalArgumentException("Cannot apply a null view-model");
        }
        if (target == null) {
            throw new IllegalArgumentException("Cannot apply to a null target entity");
        }

        // Overview
        target.setDisplayName(vm.getDisplayName());
        target.setCommonName(vm.getCommonName());
        target.setConstellationName(vm.getConstellationName());
        target.setSpectralClass(vm.getSpectralClass());
        target.setDistance(vm.getDistance());
        target.setMetallicity(vm.getMetallicity());
        target.setAge(vm.getAge());
        target.setX(vm.getX());
        target.setY(vm.getY());
        target.setZ(vm.getZ());
        target.setNotes(vm.getNotes());

        // Secondary
        target.setSimbadId(vm.getSimbadId());
        target.setGalacticLat(vm.getGalacticLat());
        target.setGalacticLong(vm.getGalacticLong());
        target.setRadius(vm.getRadius());
        target.setMass(vm.getMass());
        target.setLuminosity(vm.getLuminosity());
        target.setTemperature(vm.getTemperature());
        target.setRa(vm.getRa());
        target.setDeclination(vm.getDeclination());
        target.setPmra(vm.getPmra());
        target.setPmdec(vm.getPmdec());
        target.setParallax(vm.getParallax());
        target.setRadialVelocity(vm.getRadialVelocity());
        target.setBprp(vm.getBprp());
        target.setBpg(vm.getBpg());
        target.setGrp(vm.getGrp());
        target.setMagu(vm.getMagu());
        target.setMagb(vm.getMagb());
        target.setMagv(vm.getMagv());
        target.setMagr(vm.getMagr());
        target.setMagi(vm.getMagi());
        target.setGaiaDR2CatId(vm.getGaiaDR2CatId());

        // Aliases — replace in full. We deliberately set a new HashSet to
        // dodge the LIE risk on the entity's existing (possibly-uninitialised)
        // collection.
        target.setAliasList(toAliasSet(vm.getAliases()));

        // Fictional worldbuilding fields removed by normalization task.

        // Display
        target.setForceLabelToBeShown(vm.isForceLabelToBeShown());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static Set<String> toAliasSet(Collection<String> aliases) {
        if (aliases == null || aliases.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> out = new HashSet<>();
        for (String a : aliases) {
            if (a != null && !a.isBlank()) {
                out.add(a);
            }
        }
        return out;
    }
}
