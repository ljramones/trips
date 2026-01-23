package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DataSetDescriptorRepository using Testcontainers.
 */
class DataSetDescriptorRepositoryIntegrationTest extends BaseRepositoryIntegrationTest {

    @Autowired
    private DataSetDescriptorRepository dataSetDescriptorRepository;

    @AfterEach
    void tearDown() {
        dataSetDescriptorRepository.deleteAll();
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudTests {

        @Test
        @DisplayName("should save and find dataset by name")
        void shouldSaveAndFindByName() {
            DataSetDescriptor descriptor = createDataSet("HYG Database", 120000L, 100);
            descriptor.setFileCreator("astronexus.com");
            descriptor.setFileNotes("Stellar database combining Hipparcos, Yale, and Gliese");
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("HYG Database");

            assertThat(found).isNotNull();
            assertThat(found.getNumberStars()).isEqualTo(120000L);
            assertThat(found.getDistanceRange()).isEqualTo(100);
            assertThat(found.getFileCreator()).isEqualTo("astronexus.com");
        }

        @Test
        @DisplayName("should return null for non-existent dataset")
        void shouldReturnNullForNonExistent() {
            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("NonExistent");

            assertThat(found).isNull();
        }

        @Test
        @DisplayName("should delete dataset")
        void shouldDeleteDataset() {
            DataSetDescriptor descriptor = createDataSet("ToDelete", 1000L, 50);
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            dataSetDescriptorRepository.delete(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("ToDelete");
            assertThat(found).isNull();
        }
    }

    @Nested
    @DisplayName("Listing and Counting")
    class ListingTests {

        @Test
        @DisplayName("should find all datasets ordered by name")
        void shouldFindAllOrderedByName() {
            dataSetDescriptorRepository.saveAll(List.of(
                    createDataSet("Dataset C", 3000L, 60),
                    createDataSet("Dataset A", 1000L, 20),
                    createDataSet("Dataset B", 2000L, 40)
            ));
            flushAndClear();

            List<DataSetDescriptor> datasets = dataSetDescriptorRepository.findAllByOrderByDataSetNameAsc();

            assertThat(datasets).hasSize(3);
            assertThat(datasets).extracting(DataSetDescriptor::getDataSetName)
                    .containsExactly("Dataset A", "Dataset B", "Dataset C");
        }

        @Test
        @DisplayName("should find all datasets")
        void shouldFindAll() {
            dataSetDescriptorRepository.saveAll(List.of(
                    createDataSet("Dataset A", 1000L, 20),
                    createDataSet("Dataset B", 2000L, 40)
            ));
            flushAndClear();

            Iterable<DataSetDescriptor> all = dataSetDescriptorRepository.findAll();

            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("should count total datasets")
        void shouldCount() {
            dataSetDescriptorRepository.saveAll(List.of(
                    createDataSet("Dataset A", 1000L, 20),
                    createDataSet("Dataset B", 2000L, 40),
                    createDataSet("Dataset C", 3000L, 60)
            ));
            flushAndClear();

            long count = dataSetDescriptorRepository.count();

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Dataset Attributes")
    class DatasetAttributeTests {

        @Test
        @DisplayName("should save and retrieve star count")
        void shouldSaveStarCount() {
            DataSetDescriptor descriptor = createDataSet("Large Dataset", 500000L, 200);
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("Large Dataset");

            assertThat(found.getNumberStars()).isEqualTo(500000L);
        }

        @Test
        @DisplayName("should save and retrieve distance range")
        void shouldSaveDistanceRange() {
            DataSetDescriptor descriptor = createDataSet("Extended Dataset", 1000L, 500);
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("Extended Dataset");

            assertThat(found.getDistanceRange()).isEqualTo(500);
        }

        @Test
        @DisplayName("should save and retrieve dataset type")
        void shouldSaveDatasetType() {
            DataSetDescriptor descriptor = createDataSet("Gaia Dataset", 50000L, 100);
            descriptor.setDatasetType("Gaia DR3");
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("Gaia Dataset");

            assertThat(found.getDatasetType()).isEqualTo("Gaia DR3");
        }

        @Test
        @DisplayName("should save and retrieve file notes")
        void shouldSaveFileNotes() {
            DataSetDescriptor descriptor = createDataSet("Annotated Dataset", 1000L, 50);
            descriptor.setFileNotes("Custom star data for fiction writing");
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("Annotated Dataset");

            assertThat(found.getFileNotes()).isEqualTo("Custom star data for fiction writing");
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("should update star count")
        void shouldUpdateStarCount() {
            DataSetDescriptor descriptor = createDataSet("Updatable", 1000L, 50);
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("Updatable");
            found.setNumberStars(2000L);
            dataSetDescriptorRepository.save(found);
            flushAndClear();

            DataSetDescriptor updated = dataSetDescriptorRepository.findByDataSetName("Updatable");
            assertThat(updated.getNumberStars()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("should update distance range")
        void shouldUpdateDistanceRange() {
            DataSetDescriptor descriptor = createDataSet("Expandable", 1000L, 50);
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("Expandable");
            found.setDistanceRange(100);
            dataSetDescriptorRepository.save(found);
            flushAndClear();

            DataSetDescriptor updated = dataSetDescriptorRepository.findByDataSetName("Expandable");
            assertThat(updated.getDistanceRange()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty dataset")
        void shouldHandleEmptyDataset() {
            DataSetDescriptor descriptor = createDataSet("Empty Dataset", 0L, 0);
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("Empty Dataset");

            assertThat(found.getNumberStars()).isZero();
            assertThat(found.getDistanceRange()).isZero();
        }

        @Test
        @DisplayName("should handle special characters in name")
        void shouldHandleSpecialCharactersInName() {
            DataSetDescriptor descriptor = createDataSet("Dataset (v2.0) - Extended", 1000L, 50);
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("Dataset (v2.0) - Extended");

            assertThat(found).isNotNull();
        }

        @Test
        @DisplayName("should handle notes up to default varchar limit")
        void shouldHandleNotes() {
            DataSetDescriptor descriptor = createDataSet("Notes Dataset", 1000L, 50);
            String notes = "A".repeat(200);  // VARCHAR(255) default limit
            descriptor.setFileNotes(notes);
            dataSetDescriptorRepository.save(descriptor);
            flushAndClear();

            DataSetDescriptor found = dataSetDescriptorRepository.findByDataSetName("Notes Dataset");

            assertThat(found.getFileNotes()).hasSize(200);
        }
    }
}
