package com.teamgannon.trips.nightsky.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request parameters for night sky computation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NightSkyRequest {
    /** Planet/ExoPlanet ID to observe from */
    private String planetId;

    /** Host star record ID */
    private String hostStarId;

    /** Observation time in UTC */
    private Instant instantUtc;

    /** Observer's latitude in radians */
    private double latRad;

    /** Observer's longitude in radians */
    private double lonRad;

    /** Search radius in light years (how far to look for stars) */
    private double radiusLy;

    /** Maximum apparent magnitude to include */
    private float maxMagnitude;

    /** Maximum number of stars to return */
    private int maxStars;

    /** Level of detail preset */
    private LevelOfDetail lod;

    /** Atmospheric model for extinction */
    private AtmosphereModel atmosphereModel;

    /** Dataset name to query stars from */
    private String datasetName;

    public static NightSkyRequest defaultRequest(String planetId, String hostStarId) {
        return NightSkyRequest.builder()
                .planetId(planetId)
                .hostStarId(hostStarId)
                .instantUtc(Instant.now())
                .latRad(0.0)
                .lonRad(0.0)
                .radiusLy(100.0)
                .maxMagnitude(6.5f)
                .maxStars(10000)
                .lod(LevelOfDetail.HIGH)
                .atmosphereModel(AtmosphereModel.earthLike())
                .build();
    }
}
