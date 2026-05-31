package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.SolarSystemFeature;
import com.teamgannon.trips.model.FeatureDescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * v2 Phase E.1 Step 9 — pins the {@code parentBodyId} mapping added in {@link FeatureDescriptionConverter}
 * so the field reliably propagates from the JPA entity into the render-time DTO. Without this
 * mapping the properties dialog can't resolve the parent star's display name and the "Parent Star"
 * row falls back to "N/A" for every jump point.
 */
class FeatureDescriptionConverterTest {

    @Test
    @DisplayName("parentBodyId maps from SolarSystemFeature entity to FeatureDescription")
    void parentBodyIdRoundTrips() {
        SolarSystemFeature feature = new SolarSystemFeature();
        feature.setId("feature-1");
        feature.setName("Jump Point");
        feature.setFeatureType("JUMP_POINT");
        feature.setParentBodyId("star-123");

        FeatureDescription desc = FeatureDescriptionConverter.convert(feature);

        assertEquals("star-123", desc.getParentBodyId());
    }

    @Test
    @DisplayName("null parentBodyId stays null in the description")
    void nullParentBodyIdStaysNull() {
        SolarSystemFeature feature = new SolarSystemFeature();
        feature.setId("feature-1");
        feature.setName("Asteroid Belt");
        feature.setFeatureType("ASTEROID_BELT");
        feature.setParentBodyId(null);

        FeatureDescription desc = FeatureDescriptionConverter.convert(feature);

        assertNull(desc.getParentBodyId());
    }

    @Test
    @DisplayName("list conversion preserves parentBodyId across entries")
    void listConversionPreservesParentBodyIds() {
        SolarSystemFeature a = new SolarSystemFeature();
        a.setId("feat-a");
        a.setFeatureType("JUMP_POINT");
        a.setParentBodyId("star-A");

        SolarSystemFeature b = new SolarSystemFeature();
        b.setId("feat-b");
        b.setFeatureType("JUMP_POINT");
        b.setParentBodyId("star-B");

        List<FeatureDescription> descs = FeatureDescriptionConverter.convert(List.of(a, b));

        assertEquals(2, descs.size());
        assertEquals("star-A", descs.get(0).getParentBodyId());
        assertEquals("star-B", descs.get(1).getParentBodyId());
    }
}
