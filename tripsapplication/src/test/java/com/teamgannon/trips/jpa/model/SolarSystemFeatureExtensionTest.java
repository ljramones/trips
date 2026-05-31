package com.teamgannon.trips.jpa.model;

import com.terranrepublic.assets.CatalogedKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase E.1 Step 4 — pins the four new {@link SolarSystemFeature} columns added by V14
 * (parentBodyId, catalogReferenceId, catalogReferenceKind, networkId) and the new
 * {@link SolarSystemFeature.FeatureType#JUMP_POINT} constant.
 *
 * <p>This is a unit test of the entity's getter/setter contract — it doesn't exercise JPA
 * persistence. Integration coverage (V14 migration applied + entity↔schema validation) is
 * provided by {@code FlywayBaselineSmokeTest}.
 */
class SolarSystemFeatureExtensionTest {

    // ----------------------------------------------------------- four new columns

    @Test
    @DisplayName("parentBodyId is nullable and round-trips through getter/setter")
    void parentBodyIdRoundTrips() {
        SolarSystemFeature f = new SolarSystemFeature();
        assertNull(f.getParentBodyId(), "default is null (Divergence C resolution: V14 adds with DEFAULT NULL)");
        f.setParentBodyId("star-sol-id");
        assertEquals("star-sol-id", f.getParentBodyId());
    }

    @Test
    @DisplayName("catalogReferenceId is nullable and round-trips through getter/setter")
    void catalogReferenceIdRoundTrips() {
        SolarSystemFeature f = new SolarSystemFeature();
        assertNull(f.getCatalogReferenceId());
        f.setCatalogReferenceId("catalog-troy");
        assertEquals("catalog-troy", f.getCatalogReferenceId());
    }

    @Test
    @DisplayName("catalogReferenceKind uses the CatalogedKind enum (per Divergence B resolution)")
    void catalogReferenceKindUsesCatalogedKind() {
        SolarSystemFeature f = new SolarSystemFeature();
        assertNull(f.getCatalogReferenceKind());
        f.setCatalogReferenceKind(CatalogedKind.MEGASTRUCTURE);
        assertEquals(CatalogedKind.MEGASTRUCTURE, f.getCatalogReferenceKind());
        // TRANSPORT_NODE was the value Divergence B's case (β) added beyond AssetKind's coverage
        f.setCatalogReferenceKind(CatalogedKind.TRANSPORT_NODE);
        assertEquals(CatalogedKind.TRANSPORT_NODE, f.getCatalogReferenceKind());
    }

    @Test
    @DisplayName("networkId is nullable and round-trips through getter/setter")
    void networkIdRoundTrips() {
        SolarSystemFeature f = new SolarSystemFeature();
        assertNull(f.getNetworkId());
        f.setNetworkId("catalog-network-aldenata-civilian");
        assertEquals("catalog-network-aldenata-civilian", f.getNetworkId());
    }

    @Test
    @DisplayName("all four new columns can coexist on a single feature row (catalog-referenced JUMP_GATE example)")
    void newColumnsCoexistOnSameRow() {
        // A JUMP_GATE feature in some system, referencing the catalog TransportNode for the gate
        // AND belonging to a specific GateNetwork — the realistic shape Phase E.2 will populate.
        SolarSystemFeature gate = new SolarSystemFeature();
        gate.setSolarSystemId("solar-sol");
        gate.setFeatureType(SolarSystemFeature.FeatureType.JUMP_GATE);
        gate.setParentBodyId("star-sol");
        gate.setCatalogReferenceId("catalog-transport-aldenata-sol-gate");
        gate.setCatalogReferenceKind(CatalogedKind.TRANSPORT_NODE);
        gate.setNetworkId("catalog-network-aldenata-civilian");

        assertEquals("star-sol", gate.getParentBodyId());
        assertEquals("catalog-transport-aldenata-sol-gate", gate.getCatalogReferenceId());
        assertEquals(CatalogedKind.TRANSPORT_NODE, gate.getCatalogReferenceKind());
        assertEquals("catalog-network-aldenata-civilian", gate.getNetworkId());
    }

    // ----------------------------------------------------------- JUMP_POINT featureType

    @Test
    @DisplayName("JUMP_POINT constant exists with exact value \"JUMP_POINT\" (v2 Phase E.1 §6.3)")
    void jumpPointConstantExistsWithExactValue() {
        assertEquals("JUMP_POINT", SolarSystemFeature.FeatureType.JUMP_POINT);
    }

    @Test
    @DisplayName("JUMP_POINT is recognised as a point-type feature by isPointType()")
    void jumpPointIsPointType() {
        SolarSystemFeature f = new SolarSystemFeature();
        f.setFeatureType(SolarSystemFeature.FeatureType.JUMP_POINT);
        assertTrue(f.isPointType(),
                "JUMP_POINT must be a point-type feature so the renderer dispatches correctly");
    }

    @Test
    @DisplayName("JUMP_POINT is not a belt-type feature")
    void jumpPointIsNotBeltType() {
        SolarSystemFeature f = new SolarSystemFeature();
        f.setFeatureType(SolarSystemFeature.FeatureType.JUMP_POINT);
        assertEquals(false, f.isBeltType());
    }
}
