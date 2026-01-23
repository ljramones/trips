package com.teamgannon.trips.nightsky.service;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.nightsky.model.LevelOfDetail;
import com.teamgannon.trips.nightsky.model.StarRenderRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fast candidate-star retrieval with LOD and spatial binning.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StarQueryService {

    private final StarObjectRepository starObjectRepository;

    /**
     * Query stars within a spatial bounding box centered on observer.
     */
    @Transactional(readOnly = true)
    public List<StarRenderRow> queryStarsInRadius(
            double centerX, double centerY, double centerZ,
            double radiusLy,
            String datasetName,
            LevelOfDetail lod) {

        // Calculate bounding box
        double minX = centerX - radiusLy;
        double maxX = centerX + radiusLy;
        double minY = centerY - radiusLy;
        double maxY = centerY + radiusLy;
        double minZ = centerZ - radiusLy;
        double maxZ = centerZ + radiusLy;

        log.debug("Querying stars in box: ({},{},{}) to ({},{},{})",
                minX, minY, minZ, maxX, maxY, maxZ);

        // Use stream-based query for large datasets
        try (Stream<StarObject> starStream = starObjectRepository
                .streamInBoundingBox(
                        datasetName, minX, maxX, minY, maxY, minZ, maxZ)) {

            return starStream
                    .filter(star -> {
                        // Apply spherical distance filter
                        double dx = star.getX() - centerX;
                        double dy = star.getY() - centerY;
                        double dz = star.getZ() - centerZ;
                        return Math.sqrt(dx * dx + dy * dy + dz * dz) <= radiusLy;
                    })
                    .filter(star -> {
                        // Apply magnitude filter from LOD using V-band magnitude
                        double mag = star.getMagv();
                        return mag <= lod.getMagnitudeLimit();
                    })
                    .limit(lod.getMaxStars())
                    .map(this::toStarRenderRow)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Get host star data for the "sun" rendering.
     */
    @Transactional(readOnly = true)
    public StarRenderRow getHostStar(String hostStarId) {
        return starObjectRepository.findById(hostStarId)
                .map(this::toStarRenderRow)
                .orElse(null);
    }

    /**
     * Convert StarObject to lightweight StarRenderRow.
     */
    private StarRenderRow toStarRenderRow(StarObject star) {
        // Use V-band magnitude (magv) as the absolute magnitude
        float absMag = (float) star.getMagv();
        if (absMag == 0.0f) {
            absMag = 10.0f; // Default for unknown magnitude
        }

        // Get temperature, default to G-type star temperature if not set
        float teff = star.getTemperature() > 0 ? (float) star.getTemperature() : 5500.0f;

        return StarRenderRow.builder()
                .starId(star.getId() != null ? star.getId().hashCode() : 0)
                .xLy(star.getX())
                .yLy(star.getY())
                .zLy(star.getZ())
                .absMag(absMag)
                .bpRpOrTeff(teff)
                .spectralClass(star.getSpectralClass())
                .starName(star.getDisplayName())
                .build();
    }
}
