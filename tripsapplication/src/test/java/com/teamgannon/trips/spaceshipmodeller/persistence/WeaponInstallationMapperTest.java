package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Emplacement;
import com.terranrepublic.assets.InstallationType;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponInstallation;
import com.terranrepublic.assets.WeaponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive round-trip coverage for {@link WeaponInstallationMapper}.
 *
 * <p>Same discipline as {@code StationDesignMapperTest}: parameterised coverage for every
 * {@link InstallationType}, every {@link Emplacement}, every {@link TechLevel}, every
 * {@link OperationalState}, and the non-default cases the Phase A0 round-trip-loss bug
 * pattern would have masked.
 */
class WeaponInstallationMapperTest {

    private final WeaponInstallationMapper mapper = new WeaponInstallationMapper();

    private static WeaponInstallation with(InstallationType type,
                                           Emplacement emplacement,
                                           TechLevel techLevel,
                                           OperationalState state,
                                           boolean concealed,
                                           boolean mobile) {
        Instant now = Instant.parse("2025-04-01T12:00:00Z");
        return new WeaponInstallation(
                UUID.randomUUID().toString(),
                "Coverage-" + type.name() + "-" + emplacement.name(),
                "CV-1",
                type,
                emplacement,
                "Coverage Source",
                "Coverage Faction",
                concealed,
                "Coverage description (LOB exercise)",
                1_000,
                100,
                mobile,
                10,
                List.of(),
                techLevel,
                "coverage",
                state,
                now,
                now);
    }

    private static WeaponInstallation withArmaments(List<Armament> armaments) {
        Instant now = Instant.parse("2025-04-02T12:00:00Z");
        return new WeaponInstallation(
                UUID.randomUUID().toString(),
                "Armed Coverage Battery",
                "ACB-1",
                InstallationType.DEFENCE_BATTERY,
                Emplacement.ORBITAL_FIXED,
                "Coverage Source",
                "Coverage Faction",
                false,
                "non-empty armaments to exercise the LOB column",
                500,
                50,
                false,
                4,
                armaments,
                TechLevel.NEAR_FUTURE,
                "coverage",
                OperationalState.OPERATIONAL,
                now,
                now);
    }

    // ------------------------------------------------------------------
    // All-fields round-trip — the existing-pattern test
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Catalog.SAPL (canonical seed) round-trips intact")
    void saplRoundTrips() {
        WeaponInstallation original = (WeaponInstallation) Catalog.SAPL;
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(original));
        assertEquals(original, back, "SAPL is a seed source for V8; round-trip-loss here would corrupt seed data");
    }

    @Test
    @DisplayName("Catalog.SHEVA_GUN (canonical seed) round-trips intact")
    void shevaGunRoundTrips() {
        WeaponInstallation original = (WeaponInstallation) Catalog.SHEVA_GUN;
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(original));
        assertEquals(original, back);
    }

    // ------------------------------------------------------------------
    // Armaments LOB column
    // ------------------------------------------------------------------

    @Test
    @DisplayName("armaments are serialised into the JSON LOB column")
    void armamentsSerialisedToLob() {
        WeaponInstallation d = withArmaments(List.of(
                new Armament("Coverage beam", WeaponType.LASER, 4, 100, 1000, "test", "")));
        WeaponInstallationEntity entity = mapper.toEntity(d);
        assertNotNull(entity.getArmamentsJson());
        assertTrue(entity.getArmamentsJson().contains("Coverage beam"));
    }

    @Test
    @DisplayName("empty armaments round-trip to an empty list, not null")
    void emptyArmamentsRoundTripToEmptyList() {
        WeaponInstallation d = with(InstallationType.BEAM_ARRAY, Emplacement.SOLAR_ORBIT,
                TechLevel.ADVANCED, OperationalState.OPERATIONAL, false, false);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(d));
        assertTrue(back.armaments().isEmpty());
    }

    @Test
    @DisplayName("null armaments LOB on the entity surfaces as an empty list (legacy-row safety)")
    void nullLobSurfacesEmptyList() {
        WeaponInstallationEntity entity = mapper.toEntity(withArmaments(List.of()));
        entity.setArmamentsJson(null);
        WeaponInstallation back = mapper.toDomain(entity);
        assertTrue(back.armaments().isEmpty(), "null LOB should not NPE");
    }

    // ------------------------------------------------------------------
    // Phase A0 round-trip-loss-class coverage
    // ------------------------------------------------------------------

    @Test
    @DisplayName("concealed=true survives the round-trip")
    void concealedRoundTrips() {
        WeaponInstallation d = with(InstallationType.MISSILE_FIELD, Emplacement.GROUND_FIXED,
                TechLevel.NEAR_FUTURE, OperationalState.OPERATIONAL, true, false);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(d));
        assertTrue(back.concealed());
    }

    @Test
    @DisplayName("mobile=true survives the round-trip")
    void mobileRoundTrips() {
        WeaponInstallation d = with(InstallationType.SUPER_CANNON, Emplacement.GROUND_MOBILE,
                TechLevel.NEAR_FUTURE, OperationalState.OPERATIONAL, false, true);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(d));
        assertTrue(back.mobile());
    }

    @Test
    @DisplayName("mobile=false survives the round-trip (default-collision case)")
    void notMobileRoundTrips() {
        WeaponInstallation d = with(InstallationType.SUPER_CANNON, Emplacement.GROUND_FIXED,
                TechLevel.NEAR_FUTURE, OperationalState.OPERATIONAL, false, false);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(d));
        assertFalse(back.mobile());
    }

    // ------------------------------------------------------------------
    // Enum constant coverage
    // ------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(InstallationType.class)
    @DisplayName("every InstallationType constant round-trips through the mapper")
    void everyInstallationTypeRoundTrips(InstallationType type) {
        WeaponInstallation d = with(type, Emplacement.GROUND_FIXED,
                TechLevel.UNKNOWN, OperationalState.OPERATIONAL, false, false);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(type, back.installationType());
    }

    @ParameterizedTest
    @EnumSource(Emplacement.class)
    @DisplayName("every Emplacement constant round-trips through the mapper")
    void everyEmplacementRoundTrips(Emplacement emplacement) {
        WeaponInstallation d = with(InstallationType.DEFENCE_BATTERY, emplacement,
                TechLevel.UNKNOWN, OperationalState.OPERATIONAL, false, false);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(emplacement, back.emplacement());
    }

    @ParameterizedTest
    @EnumSource(TechLevel.class)
    @DisplayName("every TechLevel constant round-trips through the mapper")
    void everyTechLevelRoundTrips(TechLevel techLevel) {
        WeaponInstallation d = with(InstallationType.DEFENCE_BATTERY, Emplacement.GROUND_FIXED,
                techLevel, OperationalState.OPERATIONAL, false, false);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(techLevel, back.techLevel());
    }

    @ParameterizedTest
    @EnumSource(OperationalState.class)
    @DisplayName("every OperationalState constant round-trips through the mapper")
    void everyOperationalStateRoundTrips(OperationalState state) {
        WeaponInstallation d = with(InstallationType.DEFENCE_BATTERY, Emplacement.GROUND_FIXED,
                TechLevel.UNKNOWN, state, false, false);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(state, back.operationalState());
    }
}
