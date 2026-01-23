package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.nightsky.model.VisibleStarResult;
import com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts between star data formats for planetary sky visualization.
 * <p>
 * Handles:
 * <ul>
 *   <li>VisibleStarResult to StarDisplayRecord conversion</li>
 *   <li>VisibleStarResult to BrightStarEntry conversion</li>
 *   <li>Star name resolution</li>
 * </ul>
 */
@Slf4j
public class StarDataConverter {

    /**
     * Convert visible star results to star display records.
     */
    public List<StarDisplayRecord> toStarDisplayRecords(List<VisibleStarResult> results) {
        List<StarDisplayRecord> records = new ArrayList<>();
        for (VisibleStarResult result : results) {
            StarObject star = result.getStar();
            if (star == null) {
                continue;
            }
            StarDisplayRecord record = new StarDisplayRecord();
            record.setRecordId(star.getId());
            record.setStarName(resolveName(star));
            record.setMagnitude(result.getMagnitude());
            record.setDistance(star.getDistance());
            record.setSpectralClass(star.getSpectralClass());
            record.setX(star.getX());
            record.setY(star.getY());
            record.setZ(star.getZ());
            records.add(record);
        }
        return records;
    }

    /**
     * Convert visible star results to brightest star entries.
     */
    public List<PlanetarySkyRenderer.BrightStarEntry> toBrightestEntries(List<VisibleStarResult> results) {
        List<PlanetarySkyRenderer.BrightStarEntry> entries = new ArrayList<>();
        for (VisibleStarResult result : results) {
            StarObject star = result.getStar();
            if (star == null) {
                continue;
            }
            StarDisplayRecord record = new StarDisplayRecord();
            record.setRecordId(star.getId());
            record.setStarName(resolveName(star));
            record.setMagnitude(result.getMagnitude());
            record.setDistance(star.getDistance());
            record.setSpectralClass(star.getSpectralClass());
            record.setX(star.getX());
            record.setY(star.getY());
            record.setZ(star.getZ());
            entries.add(new PlanetarySkyRenderer.BrightStarEntry(
                    record.getStarName(),
                    result.getDistanceLy(),
                    result.getMagnitude(),
                    result.getAzimuthDeg(),
                    result.getAltitudeDeg(),
                    record
            ));
        }
        return entries;
    }

    /**
     * Resolve the best display name for a star.
     */
    public String resolveName(StarObject star) {
        if (star.getCommonName() != null && !star.getCommonName().trim().isEmpty()) {
            return star.getCommonName().trim();
        }
        if (star.getDisplayName() != null && !star.getDisplayName().trim().isEmpty()) {
            return star.getDisplayName().trim();
        }
        if (star.getSystemName() != null && !star.getSystemName().trim().isEmpty()) {
            return star.getSystemName().trim();
        }
        if (star.getId() != null && !star.getId().trim().isEmpty()) {
            return star.getId().trim();
        }
        return "Unknown";
    }
}
