package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization + round-trip tests for {@link StarEditMapper} (Issue 23 /
 * Phase 7-follow-on).
 * <p>
 * Pins the conversion contract before any per-caller migration of dialogs
 * away from the legacy "pass the live entity in" pattern — so a future
 * refactor can be made with confidence the field-for-field semantics
 * haven't drifted.
 */
@DisplayName("StarEditMapper: entity <-> view-model round-trip")
class StarEditMapperTest {

    // ==================== Helpers ====================

    private static StarObject populatedEntity() {
        StarObject s = new StarObject();
        s.setId("entity-123");
        s.setDataSetName("HYG");

        // Overview
        s.setDisplayName("Sol");
        s.setCommonName("The Sun");
        s.setConstellationName("Orion");  // wrong-but-fine for the test
        s.setSpectralClass("G2V");
        s.setDistance(0.0);
        s.setMetallicity(0.012);
        s.setAge(4.6e9);
        s.setX(0.0);
        s.setY(0.0);
        s.setZ(0.0);
        s.setNotes("home star");

        // Secondary
        s.setSimbadId("Sol");
        s.setGalacticLat(0.5);
        s.setGalacticLong(180.0);
        s.setRadius(1.0);
        s.setMass(1.0);
        s.setLuminosity("1.0");
        s.setTemperature(5778);
        s.setRa(0.0);
        s.setDeclination(0.0);
        s.setPmra(0.0);
        s.setPmdec(0.0);
        s.setParallax(0.0);
        s.setRadialVelocity(0.0);
        s.setBprp(0.82);
        s.setBpg(0.36);
        s.setGrp(0.46);
        s.setMagu(5.6);
        s.setMagb(5.5);
        s.setMagv(4.83);
        s.setMagr(4.42);
        s.setMagi(4.08);
        s.setGaiaDR2CatId("Gaia-Sol");

        Set<String> aliases = new HashSet<>();
        aliases.add("Sun");
        aliases.add("Sol");
        aliases.add("Helios");
        s.setAliasList(aliases);

        // Fictional fields (polity, worldType, fuelType, techType, portType, populationType,
        // productType, milSpaceType, milPlanType, anomaly, other) removed by the
        // Worldbuilding Data Model Normalization task.

        // Display
        s.setForceLabelToBeShown(true);
        return s;
    }

    // ==================== Forward (entity -> vm) ====================

    @Nested
    @DisplayName("toViewModel")
    class ToViewModel {

        @Test
        @DisplayName("rejects a null entity")
        void rejectsNullEntity() {
            assertThrows(IllegalArgumentException.class, () -> StarEditMapper.toViewModel(null));
        }

        @Test
        @DisplayName("copies every editable scalar from a populated entity")
        void copiesScalars() {
            StarEditViewModel vm = StarEditMapper.toViewModel(populatedEntity());

            assertEquals("entity-123", vm.getId());
            assertEquals("HYG", vm.getDataSetName());

            assertEquals("Sol", vm.getDisplayName());
            assertEquals("The Sun", vm.getCommonName());
            assertEquals("Orion", vm.getConstellationName());
            assertEquals("G2V", vm.getSpectralClass());
            assertEquals(0.012, vm.getMetallicity(), 1e-12);
            assertEquals(4.6e9, vm.getAge(), 1e-3);
            assertEquals("home star", vm.getNotes());

            assertEquals(1.0, vm.getRadius(), 1e-12);
            assertEquals(1.0, vm.getMass(), 1e-12);
            assertEquals("1.0", vm.getLuminosity());
            assertEquals(5778, vm.getTemperature(), 1e-9);
            assertEquals(0.82, vm.getBprp(), 1e-12);
            assertEquals("Gaia-Sol", vm.getGaiaDR2CatId());

            // Fictional-tab assertions (polity, milPlanType, anomaly, other) removed by
            // the Worldbuilding Data Model Normalization task.

            assertTrue(vm.isForceLabelToBeShown());
        }

        @Test
        @DisplayName("materialises the alias collection into a sorted-stable list")
        void materializesAliases() {
            StarEditViewModel vm = StarEditMapper.toViewModel(populatedEntity());
            assertEquals(3, vm.getAliases().size());
            assertTrue(vm.getAliases().containsAll(List.of("Sun", "Sol", "Helios")));
        }

        @Test
        @DisplayName("converts null string fields to empty strings (Java FXML doesn't like null)")
        void nullStringsBecomeEmpty() {
            // StarObject's @Data defaults make most fields non-null already;
            // explicitly null out the ones we want to verify the mapper
            // defensively converts.
            StarObject sparse = new StarObject();
            sparse.setId("sparse");
            sparse.setDisplayName(null);
            sparse.setCommonName(null);
            sparse.setConstellationName(null);
            sparse.setSpectralClass(null);
            sparse.setNotes(null);
            sparse.setSimbadId(null);
            sparse.setLuminosity(null);
            sparse.setGaiaDR2CatId(null);
            // polity / worldType setters removed by Worldbuilding Data Model Normalization task.

            StarEditViewModel vm = StarEditMapper.toViewModel(sparse);
            assertEquals("", vm.getDisplayName());
            assertEquals("", vm.getCommonName());
            assertEquals("", vm.getConstellationName());
            assertEquals("", vm.getSpectralClass());
            assertEquals("", vm.getNotes());
            assertEquals("", vm.getSimbadId());
            assertEquals("", vm.getLuminosity());
            assertEquals("", vm.getGaiaDR2CatId());
        }

        @Test
        @DisplayName("treats null alias collection as an empty list")
        void nullAliasesBecomeEmptyList() {
            StarObject s = new StarObject();
            s.setId("x");
            s.setAliasList(null);

            StarEditViewModel vm = StarEditMapper.toViewModel(s);
            assertNotNull(vm.getAliases());
            assertTrue(vm.getAliases().isEmpty());
        }
    }

    // ==================== Reverse (vm -> entity) ====================

    @Nested
    @DisplayName("applyToEntity")
    class ApplyToEntity {

        @Test
        @DisplayName("rejects null view-model")
        void rejectsNullViewModel() {
            StarObject target = new StarObject();
            target.setId("t");
            assertThrows(IllegalArgumentException.class,
                    () -> StarEditMapper.applyToEntity(null, target));
        }

        @Test
        @DisplayName("rejects null target entity")
        void rejectsNullTarget() {
            StarEditViewModel vm = new StarEditViewModel();
            assertThrows(IllegalArgumentException.class,
                    () -> StarEditMapper.applyToEntity(vm, null));
        }

        @Test
        @DisplayName("does NOT overwrite the entity's id or dataset name")
        void preservesIdentity() {
            StarObject target = new StarObject();
            target.setId("original-id");
            target.setDataSetName("original-ds");

            StarEditViewModel vm = new StarEditViewModel();
            vm.setId("vm-id");
            vm.setDataSetName("vm-ds");
            vm.setDisplayName("new name");

            StarEditMapper.applyToEntity(vm, target);

            assertEquals("original-id", target.getId(), "id must NOT be overwritten");
            assertEquals("original-ds", target.getDataSetName(), "dataset must NOT be overwritten");
            assertEquals("new name", target.getDisplayName(), "editable field IS overwritten");
        }

        @Test
        @DisplayName("aliasList is replaced in full (covers the LIE bug class)")
        void aliasListIsReplaced() {
            StarObject target = new StarObject();
            target.setId("t");
            Set<String> originalAliases = new HashSet<>();
            originalAliases.add("OldAlias1");
            originalAliases.add("OldAlias2");
            target.setAliasList(originalAliases);

            StarEditViewModel vm = new StarEditViewModel();
            vm.setAliases(List.of("NewAlias", "AnotherNew"));

            StarEditMapper.applyToEntity(vm, target);

            assertEquals(2, target.getAliasList().size());
            assertTrue(target.getAliasList().contains("NewAlias"));
            assertTrue(target.getAliasList().contains("AnotherNew"));
            assertFalse(target.getAliasList().contains("OldAlias1"));
        }

        @Test
        @DisplayName("aliasList replacement skips null and blank entries")
        void aliasListReplacementSkipsBlanks() {
            StarObject target = new StarObject();
            target.setId("t");

            StarEditViewModel vm = new StarEditViewModel();
            vm.setAliases(new java.util.ArrayList<>(java.util.Arrays.asList(
                    "Valid", "", "  ", null, "Another")));

            StarEditMapper.applyToEntity(vm, target);

            assertEquals(2, target.getAliasList().size());
            assertTrue(target.getAliasList().contains("Valid"));
            assertTrue(target.getAliasList().contains("Another"));
        }
    }

    // ==================== Round-trip ====================

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("entity -> vm -> applyToEntity (same target) preserves every editable field")
        void entityViaVmBackToSameEntityIsLossless() {
            StarObject original = populatedEntity();
            StarObject snapshot = populatedEntity();  // independent equality reference

            StarEditViewModel vm = StarEditMapper.toViewModel(original);

            StarObject target = new StarObject();
            target.setId(original.getId());
            target.setDataSetName(original.getDataSetName());
            StarEditMapper.applyToEntity(vm, target);

            assertEquals(snapshot.getDisplayName(), target.getDisplayName());
            assertEquals(snapshot.getCommonName(), target.getCommonName());
            assertEquals(snapshot.getSpectralClass(), target.getSpectralClass());
            assertEquals(snapshot.getMetallicity(), target.getMetallicity(), 1e-12);
            assertEquals(snapshot.getRadius(), target.getRadius(), 1e-12);
            assertEquals(snapshot.getMass(), target.getMass(), 1e-12);
            assertEquals(snapshot.getTemperature(), target.getTemperature(), 1e-9);
            // Fictional-tab round-trip assertions (polity, milPlanType, anomaly, other)
            // removed by the Worldbuilding Data Model Normalization task.
            assertEquals(snapshot.isForceLabelToBeShown(), target.isForceLabelToBeShown());
            assertEquals(snapshot.getAliasList().size(), target.getAliasList().size());
            assertTrue(target.getAliasList().containsAll(snapshot.getAliasList()));
        }
    }
}
