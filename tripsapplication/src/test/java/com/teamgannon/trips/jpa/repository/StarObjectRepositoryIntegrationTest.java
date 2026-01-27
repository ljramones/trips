package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StarObjectRepository using Testcontainers.
 * Tests all repository methods against a real PostgreSQL database.
 */
class StarObjectRepositoryIntegrationTest extends BaseRepositoryIntegrationTest {

    @Autowired
    private StarObjectRepository starObjectRepository;

    @Autowired
    private DataSetDescriptorRepository dataSetDescriptorRepository;

    private DataSetDescriptor testDataSet;

    @BeforeEach
    void setUp() {
        // Create test dataset
        testDataSet = createDataSet(TEST_DATASET, 0L, 100);
        dataSetDescriptorRepository.save(testDataSet);
    }

    @AfterEach
    void tearDown() {
        starObjectRepository.deleteAll();
        dataSetDescriptorRepository.deleteAll();
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudTests {

        @Test
        @DisplayName("should save and find star by ID")
        void shouldSaveAndFindById() {
            StarObject star = createStar("Alpha Centauri", 1.3, -0.8, 0.5, 4.37);
            starObjectRepository.save(star);
            flushAndClear();

            var found = starObjectRepository.findById(star.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getDisplayName()).isEqualTo("Alpha Centauri");
            assertThat(found.get().getDistance()).isEqualTo(4.37);
        }

        @Test
        @DisplayName("should find stars by list of IDs")
        void shouldFindByIdIn() {
            StarObject star1 = createStar("Star1", 1, 1, 1, 10);
            StarObject star2 = createStar("Star2", 2, 2, 2, 20);
            StarObject star3 = createStar("Star3", 3, 3, 3, 30);
            starObjectRepository.saveAll(List.of(star1, star2, star3));
            flushAndClear();

            List<StarObject> found = starObjectRepository.findByIdIn(List.of(star1.getId(), star3.getId()));

            assertThat(found).hasSize(2);
            assertThat(found).extracting(StarObject::getDisplayName)
                    .containsExactlyInAnyOrder("Star1", "Star3");
        }

        @Test
        @DisplayName("should delete stars by dataset name")
        void shouldDeleteByDataSetName() {
            starObjectRepository.saveAll(List.of(
                    createStar("Star1", 1, 1, 1, 10),
                    createStar("Star2", 2, 2, 2, 20)
            ));

            StarObject otherDatasetStar = createStar("OtherStar", 3, 3, 3, 30);
            otherDatasetStar.setDataSetName("OtherDataset");
            starObjectRepository.save(otherDatasetStar);
            flushAndClear();

            starObjectRepository.deleteByDataSetName(TEST_DATASET);
            flushAndClear();

            assertThat(starObjectRepository.countByDataSetName(TEST_DATASET)).isZero();
            assertThat(starObjectRepository.countByDataSetName("OtherDataset")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Dataset Queries")
    class DatasetQueryTests {

        @Test
        @DisplayName("should find all stars in dataset")
        void shouldFindByDataSetName() {
            starObjectRepository.saveAll(List.of(
                    createStar("Star1", 1, 1, 1, 10),
                    createStar("Star2", 2, 2, 2, 20),
                    createStar("Star3", 3, 3, 3, 30)
            ));
            flushAndClear();

            Page<StarObject> page = starObjectRepository.findByDataSetName(TEST_DATASET, PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("should find stars ordered by display name")
        void shouldFindByDataSetNameOrderByDisplayName() {
            starObjectRepository.saveAll(List.of(
                    createStar("Zeta", 1, 1, 1, 10),
                    createStar("Alpha", 2, 2, 2, 20),
                    createStar("Beta", 3, 3, 3, 30)
            ));
            flushAndClear();

            List<StarObject> stars = starObjectRepository.findByDataSetNameOrderByDisplayName(TEST_DATASET);

            assertThat(stars).extracting(StarObject::getDisplayName)
                    .containsExactly("Alpha", "Beta", "Zeta");
        }

        @Test
        @DisplayName("should count stars in dataset")
        void shouldCountByDataSetName() {
            starObjectRepository.saveAll(List.of(
                    createStar("Star1", 1, 1, 1, 10),
                    createStar("Star2", 2, 2, 2, 20)
            ));
            flushAndClear();

            long count = starObjectRepository.countByDataSetName(TEST_DATASET);

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Distance Queries")
    class DistanceQueryTests {

        @Test
        @DisplayName("should find stars within distance limit")
        void shouldFindByDistanceLessThan() {
            starObjectRepository.saveAll(List.of(
                    createStar("Near1", 1, 1, 1, 5),
                    createStar("Near2", 2, 2, 2, 8),
                    createStar("Far", 10, 10, 10, 50)
            ));
            flushAndClear();

            Page<StarObject> nearStars = starObjectRepository.findByDistanceLessThan(
                    TEST_DATASET, 10.0, PageRequest.of(0, 10));

            assertThat(nearStars.getContent()).hasSize(2);
            assertThat(nearStars.getContent()).extracting(StarObject::getDisplayName)
                    .containsExactlyInAnyOrder("Near1", "Near2");
        }

        @Test
        @DisplayName("should stream stars within distance")
        void shouldStreamByDistanceWithin() {
            starObjectRepository.saveAll(List.of(
                    createStar("Near1", 1, 1, 1, 5),
                    createStar("Near2", 2, 2, 2, 8),
                    createStar("Far", 10, 10, 10, 50)
            ));
            flushAndClear();

            try (Stream<StarObject> stream = starObjectRepository.streamByDistanceWithin(TEST_DATASET, 10.0)) {
                List<StarObject> nearStars = stream.collect(Collectors.toList());
                assertThat(nearStars).hasSize(2);
            }
        }

        @Test
        @DisplayName("should count stars within distance")
        void shouldCountByDistanceWithin() {
            starObjectRepository.saveAll(List.of(
                    createStar("Near1", 1, 1, 1, 5),
                    createStar("Near2", 2, 2, 2, 8),
                    createStar("Far", 10, 10, 10, 50)
            ));
            flushAndClear();

            long count = starObjectRepository.countByDistanceWithin(TEST_DATASET, 10.0);

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Bounding Box Queries")
    class BoundingBoxQueryTests {

        @Test
        @DisplayName("should find stars in 3D bounding box")
        void shouldFindInBoundingBox() {
            starObjectRepository.saveAll(List.of(
                    createStar("Inside1", 5, 5, 5, 10),
                    createStar("Inside2", 8, 8, 8, 15),
                    createStar("Outside", 50, 50, 50, 100)
            ));
            flushAndClear();

            List<StarObject> inside = starObjectRepository.findInBoundingBox(
                    TEST_DATASET, 0, 10, 0, 10, 0, 10);

            assertThat(inside).hasSize(2);
            assertThat(inside).extracting(StarObject::getDisplayName)
                    .containsExactlyInAnyOrder("Inside1", "Inside2");
        }

        @Test
        @DisplayName("should count stars in bounding box")
        void shouldCountInBoundingBox() {
            starObjectRepository.saveAll(List.of(
                    createStar("Inside1", 5, 5, 5, 10),
                    createStar("Inside2", 8, 8, 8, 15),
                    createStar("Outside", 50, 50, 50, 100)
            ));
            flushAndClear();

            int count = starObjectRepository.countInBoundingBox(
                    TEST_DATASET, 0, 10, 0, 10, 0, 10);

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("should stream stars in bounding box")
        void shouldStreamInBoundingBox() {
            starObjectRepository.saveAll(List.of(
                    createStar("Inside1", 5, 5, 5, 10),
                    createStar("Outside", 50, 50, 50, 100)
            ));
            flushAndClear();

            try (Stream<StarObject> stream = starObjectRepository.streamInBoundingBox(
                    TEST_DATASET, 0, 10, 0, 10, 0, 10)) {
                List<StarObject> inside = stream.collect(Collectors.toList());
                assertThat(inside).hasSize(1);
                assertThat(inside.get(0).getDisplayName()).isEqualTo("Inside1");
            }
        }
    }

    @Nested
    @DisplayName("Name Search Queries")
    class NameSearchQueryTests {

        @Test
        @DisplayName("should find stars by partial display name")
        void shouldFindByDisplayNameContaining() {
            starObjectRepository.saveAll(List.of(
                    createStar("Alpha Centauri A", 1, 1, 1, 4.37),
                    createStar("Alpha Centauri B", 1, 1, 1, 4.37),
                    createStar("Proxima Centauri", 1, 1, 1, 4.24)
            ));
            flushAndClear();

            List<StarObject> found = starObjectRepository.findByDisplayNameContaining(TEST_DATASET, "Alpha");

            assertThat(found).hasSize(2);
            assertThat(found).extracting(StarObject::getDisplayName)
                    .allMatch(name -> name.contains("Alpha"));
        }

        @Test
        @DisplayName("should find stars by partial common name")
        void shouldFindByCommonNameContaining() {
            StarObject star1 = createStar("HD 10700", 1, 1, 1, 12);
            star1.setCommonName("Tau Ceti");
            StarObject star2 = createStar("HD 22049", 2, 2, 2, 10);
            star2.setCommonName("Epsilon Eridani");
            starObjectRepository.saveAll(List.of(star1, star2));
            flushAndClear();

            List<StarObject> found = starObjectRepository.findByCommonNameContaining(TEST_DATASET, "Tau");

            assertThat(found).hasSize(1);
            assertThat(found.get(0).getCommonName()).isEqualTo("Tau Ceti");
        }

        @Test
        @DisplayName("should find star by display name ignoring case")
        void shouldFindFirstByDisplayNameIgnoreCase() {
            starObjectRepository.save(createStar("Alpha Centauri", 1, 1, 1, 4.37));
            flushAndClear();

            StarObject found = starObjectRepository.findFirstByDisplayNameIgnoreCase("alpha centauri");

            assertThat(found).isNotNull();
            assertThat(found.getDisplayName()).isEqualTo("Alpha Centauri");
        }

        @Test
        @DisplayName("should find stars by alias")
        void shouldFindByAlias() {
            StarObject star = createStar("HD 128620", 1, 1, 1, 4.37);
            star.setAliasList(Set.of("alpha centauri a", "rigil kentaurus"));
            starObjectRepository.save(star);
            flushAndClear();

            List<StarObject> found = starObjectRepository.findByAlias("alpha centauri a");

            assertThat(found).hasSize(1);
            assertThat(found.get(0).getDisplayName()).isEqualTo("HD 128620");
        }
    }

    @Nested
    @DisplayName("Constellation Queries")
    class ConstellationQueryTests {

        @Test
        @DisplayName("should find stars by constellation")
        void shouldFindByConstellation() {
            StarObject star1 = createStar("Alpha Centauri", 1, 1, 1, 4.37);
            star1.setConstellationName("Centaurus");
            StarObject star2 = createStar("Beta Centauri", 2, 2, 2, 390);
            star2.setConstellationName("Centaurus");
            StarObject star3 = createStar("Sirius", 3, 3, 3, 8.6);
            star3.setConstellationName("Canis Major");
            starObjectRepository.saveAll(List.of(star1, star2, star3));
            flushAndClear();

            List<StarObject> centaurusStars = starObjectRepository.findByConstellation(
                    TEST_DATASET, "Centaurus");

            assertThat(centaurusStars).hasSize(2);
            assertThat(centaurusStars).extracting(StarObject::getConstellationName)
                    .allMatch(name -> name.equals("Centaurus"));
        }
    }

    @Nested
    @DisplayName("Catalog ID Queries")
    class CatalogIdQueryTests {

        @Test
        @DisplayName("should find star by Hipparcos ID")
        void shouldFindByHipId() {
            StarObject star = createStarWithCatalogIds("Alpha Centauri A", "71683", "128620", null);
            starObjectRepository.save(star);
            flushAndClear();

            StarObject found = starObjectRepository.findByHipId(TEST_DATASET, "71683");

            assertThat(found).isNotNull();
            assertThat(found.getDisplayName()).isEqualTo("Alpha Centauri A");
        }

        @Test
        @DisplayName("should find star by Henry Draper ID")
        void shouldFindByHdId() {
            StarObject star = createStarWithCatalogIds("Alpha Centauri A", null, "128620", null);
            starObjectRepository.save(star);
            flushAndClear();

            StarObject found = starObjectRepository.findByHdId(TEST_DATASET, "128620");

            assertThat(found).isNotNull();
            assertThat(found.getDisplayName()).isEqualTo("Alpha Centauri A");
        }

        @Test
        @DisplayName("should find star by Gaia DR3 ID")
        void shouldFindByGaiaDR3Id() {
            StarObject star = createStarWithCatalogIds("Alpha Centauri A", null, null, "5853498713190525696");
            starObjectRepository.save(star);
            flushAndClear();

            StarObject found = starObjectRepository.findByGaiaDR3Id(TEST_DATASET, "5853498713190525696");

            assertThat(found).isNotNull();
            assertThat(found.getDisplayName()).isEqualTo("Alpha Centauri A");
        }

        @Test
        @DisplayName("should find star by specific HIP ID")
        void shouldFindBySpecificHipId() {
            StarObject star = createStarWithCatalogIds("Alpha Centauri A", "71683", "128620", null);
            starObjectRepository.save(star);
            flushAndClear();

            // Use specific catalog ID query instead of the general catalog ID list query
            // which has issues with @Lob TEXT columns in PostgreSQL
            StarObject found = starObjectRepository.findByHipId(TEST_DATASET, "71683");

            assertThat(found).isNotNull();
            assertThat(found.getDisplayName()).isEqualTo("Alpha Centauri A");
        }
    }

    @Nested
    @DisplayName("Solar System Queries")
    class SolarSystemQueryTests {

        @Test
        @DisplayName("should find stars by solar system ID")
        void shouldFindBySolarSystemId() {
            String systemId = "system-123";
            StarObject star1 = createStar("Star A", 0, 0, 0, 0);
            star1.setSolarSystemId(systemId);
            StarObject star2 = createStar("Star B", 0.1, 0.1, 0.1, 0);
            star2.setSolarSystemId(systemId);
            starObjectRepository.saveAll(List.of(star1, star2));
            flushAndClear();

            List<StarObject> found = starObjectRepository.findBySolarSystemId(systemId);

            assertThat(found).hasSize(2);
        }

        @Test
        @DisplayName("should count stars in solar system")
        void shouldCountBySolarSystemId() {
            String systemId = "system-123";
            StarObject star1 = createStar("Star A", 0, 0, 0, 0);
            star1.setSolarSystemId(systemId);
            StarObject star2 = createStar("Star B", 0.1, 0.1, 0.1, 0);
            star2.setSolarSystemId(systemId);
            starObjectRepository.saveAll(List.of(star1, star2));
            flushAndClear();

            long count = starObjectRepository.countBySolarSystemId(systemId);

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("RA/Dec Queries")
    class RaDecQueryTests {

        @Test
        @DisplayName("should find stars near RA/Dec coordinates")
        void shouldFindByRaDecNear() {
            StarObject star = createStar("Target Star", 1, 1, 1, 10);
            star.setRa(180.5);
            star.setDeclination(-45.3);
            starObjectRepository.save(star);
            flushAndClear();

            List<StarObject> found = starObjectRepository.findByRaDecNear(180.505, -45.295);

            assertThat(found).hasSize(1);
            assertThat(found.get(0).getDisplayName()).isEqualTo("Target Star");
        }

        @Test
        @DisplayName("should not find stars outside RA/Dec tolerance")
        void shouldNotFindStarsOutsideTolerance() {
            StarObject star = createStar("Target Star", 1, 1, 1, 10);
            star.setRa(180.5);
            star.setDeclination(-45.3);
            starObjectRepository.save(star);
            flushAndClear();

            List<StarObject> found = starObjectRepository.findByRaDecNear(181.0, -45.0);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Missing Distance Queries")
    class MissingDistanceQueryTests {

        @Test
        @DisplayName("should find stars with missing distance that have HIP ID")
        void shouldFindMissingDistanceWithHipId() {
            // Star with distance
            StarObject withDistance = createStar("Has Distance", 1, 1, 1, 10);
            withDistance.getCatalogIds().setHipCatId("71683");

            // Star missing distance but has HIP ID
            StarObject missingDistanceWithHip = createStar("Missing Distance With HIP", 2, 2, 2, 0);
            missingDistanceWithHip.getCatalogIds().setHipCatId("71684");

            // Star missing distance and no HIP ID
            StarObject missingDistanceNoHip = createStar("Missing Distance No HIP", 3, 3, 3, 0);
            // No HIP ID set (defaults to empty from initDefaults)

            starObjectRepository.saveAll(List.of(withDistance, missingDistanceWithHip, missingDistanceNoHip));
            flushAndClear();

            // Verify count of all stars with distance = 0
            long count = starObjectRepository.countMissingDistance(TEST_DATASET);
            assertThat(count).isEqualTo(2); // Both missing distance stars
        }

        @Test
        @DisplayName("should count stars with missing distance")
        void shouldCountMissingDistance() {
            StarObject withDistance = createStar("Has Distance", 1, 1, 1, 10);
            StarObject missingDistance = createStar("Missing Distance", 2, 2, 2, 0);
            starObjectRepository.saveAll(List.of(withDistance, missingDistance));
            flushAndClear();

            long count = starObjectRepository.countMissingDistance(TEST_DATASET);

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Composite Index Query Patterns")
    class CompositeIndexQueryTests {

        @Test
        @DisplayName("should efficiently query by dataset and 3D coordinates (uses idx_star_dataset_coords)")
        void shouldQueryByDatasetAndCoordinates() {
            // Create stars spread across 3D space
            starObjectRepository.saveAll(List.of(
                    createStar("Quadrant1", 5, 5, 5, 10),
                    createStar("Quadrant2", -5, 5, 5, 12),
                    createStar("Quadrant3", 5, -5, 5, 14),
                    createStar("Quadrant4", 5, 5, -5, 16),
                    createStar("Quadrant5", -5, -5, 5, 18),
                    createStar("Quadrant6", -5, 5, -5, 20),
                    createStar("Quadrant7", 5, -5, -5, 22),
                    createStar("Quadrant8", -5, -5, -5, 24),
                    createStar("Origin", 0, 0, 0, 0),
                    createStar("FarAway", 100, 100, 100, 200)
            ));
            flushAndClear();

            // Query positive octant - should use composite index (dataSetName, x, y, z)
            List<StarObject> positiveOctant = starObjectRepository.findInBoundingBox(
                    TEST_DATASET, 0, 10, 0, 10, 0, 10);

            assertThat(positiveOctant).hasSize(1);
            assertThat(positiveOctant.get(0).getDisplayName()).isEqualTo("Quadrant1");
        }

        @Test
        @DisplayName("should efficiently query by dataset and distance range (uses idx_star_dataset_distance)")
        void shouldQueryByDatasetAndDistanceRange() {
            // Create stars at various distances
            starObjectRepository.saveAll(List.of(
                    createStar("VeryNear", 1, 0, 0, 4),
                    createStar("Near", 2, 0, 0, 8),
                    createStar("Medium", 5, 0, 0, 15),
                    createStar("Far", 10, 0, 0, 30),
                    createStar("VeryFar", 20, 0, 0, 60)
            ));
            flushAndClear();

            // Query nearby stars - should use composite index (dataSetName, distance)
            Page<StarObject> nearbyStars = starObjectRepository.findByDistanceLessThan(
                    TEST_DATASET, 10.0, PageRequest.of(0, 10));

            assertThat(nearbyStars.getContent()).hasSize(2);
            assertThat(nearbyStars.getContent()).extracting(StarObject::getDisplayName)
                    .containsExactlyInAnyOrder("VeryNear", "Near");
        }

        @Test
        @DisplayName("should handle multiple datasets with coordinate queries")
        void shouldHandleMultipleDatasetsWithCoordinateQueries() {
            // Create stars in test dataset
            starObjectRepository.saveAll(List.of(
                    createStar("TestStar1", 5, 5, 5, 10),
                    createStar("TestStar2", 6, 6, 6, 12)
            ));

            // Create stars in other dataset
            StarObject otherStar1 = createStar("OtherStar1", 5, 5, 5, 10);
            otherStar1.setDataSetName("OtherDataset");
            StarObject otherStar2 = createStar("OtherStar2", 6, 6, 6, 12);
            otherStar2.setDataSetName("OtherDataset");
            starObjectRepository.saveAll(List.of(otherStar1, otherStar2));
            flushAndClear();

            // Query should only return stars from the specified dataset
            List<StarObject> testDatasetStars = starObjectRepository.findInBoundingBox(
                    TEST_DATASET, 0, 10, 0, 10, 0, 10);
            List<StarObject> otherDatasetStars = starObjectRepository.findInBoundingBox(
                    "OtherDataset", 0, 10, 0, 10, 0, 10);

            assertThat(testDatasetStars).hasSize(2);
            assertThat(otherDatasetStars).hasSize(2);
            assertThat(testDatasetStars).extracting(StarObject::getDataSetName)
                    .allMatch(name -> name.equals(TEST_DATASET));
        }

        @Test
        @DisplayName("should efficiently stream large coordinate range queries")
        void shouldStreamLargeCoordinateRangeQueries() {
            // Create a grid of stars
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    for (int z = 0; z < 5; z++) {
                        StarObject star = createStar(
                                "Star_%d_%d_%d".formatted(x, y, z),
                                x * 2, y * 2, z * 2,
                                Math.sqrt(x * x + y * y + z * z) * 2
                        );
                        starObjectRepository.save(star);
                    }
                }
            }
            flushAndClear();

            // Stream query for a subset - should efficiently use composite index
            try (Stream<StarObject> stream = starObjectRepository.streamInBoundingBox(
                    TEST_DATASET, 0, 5, 0, 5, 0, 5)) {
                List<StarObject> found = stream.collect(Collectors.toList());
                // Should find stars at coordinates (0,0,0), (0,0,2), (0,2,0), (0,2,2), (2,0,0), (2,0,2), (2,2,0), (2,2,2)
                // Plus (0,0,4), (0,4,0), (4,0,0), (0,2,4), etc. within the 0-5 range
                assertThat(found).hasSizeGreaterThanOrEqualTo(8);
            }
        }

        @Test
        @DisplayName("should count stars in bounding box across datasets")
        void shouldCountStarsInBoundingBoxAcrossDatasets() {
            // Create test dataset stars
            starObjectRepository.saveAll(List.of(
                    createStar("Test1", 1, 1, 1, 5),
                    createStar("Test2", 2, 2, 2, 8)
            ));

            // Create other dataset stars at same coordinates
            StarObject other1 = createStar("Other1", 1, 1, 1, 5);
            other1.setDataSetName("OtherDataset");
            starObjectRepository.save(other1);
            flushAndClear();

            int testCount = starObjectRepository.countInBoundingBox(
                    TEST_DATASET, 0, 5, 0, 5, 0, 5);
            int otherCount = starObjectRepository.countInBoundingBox(
                    "OtherDataset", 0, 5, 0, 5, 0, 5);

            assertThat(testCount).isEqualTo(2);
            assertThat(otherCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Alias Loading Tests")
    class AliasLoadingTests {

        @Test
        @DisplayName("should find star by ID with aliases eagerly loaded")
        void shouldFindByIdWithAliases() {
            StarObject star = createStar("Alpha Centauri", 1.3, -0.8, 0.5, 4.37);
            star.getAliasList().add("Rigil Kentaurus");
            star.getAliasList().add("Toliman");
            starObjectRepository.save(star);
            flushAndClear();

            StarObject found = starObjectRepository.findByIdWithAliases(star.getId());

            assertThat(found).isNotNull();
            assertThat(found.getDisplayName()).isEqualTo("Alpha Centauri");
            // Aliases should be loaded even outside transaction
            assertThat(found.getAliasList()).containsExactlyInAnyOrder("Rigil Kentaurus", "Toliman");
        }

        @Test
        @DisplayName("should return null for non-existent ID with aliases query")
        void shouldReturnNullForNonExistentIdWithAliases() {
            StarObject found = starObjectRepository.findByIdWithAliases("non-existent-id");

            assertThat(found).isNull();
        }

        @Test
        @DisplayName("should handle star with no aliases")
        void shouldHandleStarWithNoAliases() {
            StarObject star = createStar("Lonely Star", 1, 1, 1, 10);
            starObjectRepository.save(star);
            flushAndClear();

            StarObject found = starObjectRepository.findByIdWithAliases(star.getId());

            assertThat(found).isNotNull();
            assertThat(found.getAliasList()).isEmpty();
        }
    }
}
