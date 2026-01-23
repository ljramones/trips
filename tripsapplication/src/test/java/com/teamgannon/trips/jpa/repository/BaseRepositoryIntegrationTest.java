package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * Base class for JPA repository integration tests using Testcontainers with PostgreSQL.
 * Provides common test data creation utilities and shared container configuration.
 * <p>
 * These tests require Docker to be running. To skip integration tests when Docker
 * is not available, run Maven with:
 * <pre>
 * ./mvnw-java17.sh test -DexcludedGroups=integration
 * </pre>
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Tag("integration")
public abstract class BaseRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("tripstest")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    protected EntityManager entityManager;

    protected static final String TEST_DATASET = "TestDataset";

    @BeforeEach
    void baseSetUp() {
        // Subclasses can override for additional setup
    }

    // ========== Star Creation Utilities ==========

    /**
     * Create a basic StarObject with required fields.
     */
    protected StarObject createStar(String displayName, double x, double y, double z, double distance) {
        StarObject star = new StarObject();
        star.setId(UUID.randomUUID().toString());
        star.setDisplayName(displayName);
        star.setDataSetName(TEST_DATASET);
        star.setX(x);
        star.setY(y);
        star.setZ(z);
        star.setDistance(distance);
        star.setRealStar(true);
        star.setCommonName("");
        star.setConstellationName("");
        star.setSpectralClass("G2V");
        star.setOrthoSpectralClass("G2V");
        star.setNotes("");
        star.setSource("");

        // Initialize embedded objects
        star.getCatalogIds().initDefaults();
        star.getWorldBuilding().initDefaults();

        return star;
    }

    /**
     * Create a star with catalog IDs.
     */
    protected StarObject createStarWithCatalogIds(String displayName, String hipId, String hdId, String gaiaId) {
        StarObject star = createStar(displayName, 0, 0, 0, 10);
        star.getCatalogIds().setHipCatId(hipId != null ? hipId : "");
        star.getCatalogIds().setHdCatId(hdId != null ? hdId : "");
        star.getCatalogIds().setGaiaDR3CatId(gaiaId != null ? gaiaId : "");
        if (hipId != null || hdId != null || gaiaId != null) {
            StringBuilder catalogList = new StringBuilder();
            if (hipId != null) catalogList.append("HIP ").append(hipId);
            if (hdId != null) {
                if (catalogList.length() > 0) catalogList.append(", ");
                catalogList.append("HD ").append(hdId);
            }
            if (gaiaId != null) {
                if (catalogList.length() > 0) catalogList.append(", ");
                catalogList.append("Gaia DR3 ").append(gaiaId);
            }
            star.getCatalogIds().setCatalogIdList(catalogList.toString());
        }
        return star;
    }

    /**
     * Create a star with world-building data.
     */
    protected StarObject createStarWithWorldBuilding(String displayName, String polity, String worldType) {
        StarObject star = createStar(displayName, 0, 0, 0, 10);
        star.getWorldBuilding().setPolity(polity != null ? polity : "NA");
        star.getWorldBuilding().setWorldType(worldType != null ? worldType : "NA");
        return star;
    }

    /**
     * Create Sol at origin.
     */
    protected StarObject createSol() {
        StarObject sol = createStar("Sol", 0, 0, 0, 0);
        sol.setCommonName("Sol");
        sol.setSpectralClass("G2V");
        sol.setOrthoSpectralClass("G2V");
        return sol;
    }

    // ========== DataSet Creation Utilities ==========

    /**
     * Create a basic DataSetDescriptor.
     */
    protected DataSetDescriptor createDataSet(String name, Long starCount, double distanceRange) {
        DataSetDescriptor descriptor = new DataSetDescriptor();
        descriptor.setDataSetName(name);
        descriptor.setNumberStars(starCount);
        descriptor.setDistanceRange(distanceRange);
        descriptor.setFileCreator("Test");
        descriptor.setFileNotes("Test dataset");
        descriptor.setDatasetType("Test");
        return descriptor;
    }

    // ========== SolarSystem Creation Utilities ==========

    /**
     * Create a basic SolarSystem.
     */
    protected SolarSystem createSolarSystem(String name, String primaryStarId) {
        SolarSystem system = new SolarSystem();
        system.setId(UUID.randomUUID().toString());
        system.setSystemName(name);
        system.setPrimaryStarId(primaryStarId);
        system.setStarCount(1);
        system.setPlanetCount(0);
        system.setHasHabitableZonePlanets(false);
        return system;
    }

    // ========== ExoPlanet Creation Utilities ==========

    /**
     * Create a basic ExoPlanet.
     */
    protected ExoPlanet createExoPlanet(String name, String starName, String solarSystemId, String hostStarId) {
        ExoPlanet planet = new ExoPlanet();
        planet.setId(UUID.randomUUID().toString());
        planet.setName(name);
        planet.setStarName(starName);
        planet.setSolarSystemId(solarSystemId);
        planet.setHostStarId(hostStarId);
        planet.setPlanetStatus("Confirmed");
        planet.setSemiMajorAxis(1.0);
        planet.setMass(1.0);
        planet.setRadius(1.0);
        planet.setIsMoon(false);
        return planet;
    }

    /**
     * Create a moon.
     */
    protected ExoPlanet createMoon(String name, String parentPlanetId, String solarSystemId) {
        ExoPlanet moon = new ExoPlanet();
        moon.setId(UUID.randomUUID().toString());
        moon.setName(name);
        moon.setParentPlanetId(parentPlanetId);
        moon.setSolarSystemId(solarSystemId);
        moon.setPlanetStatus("Confirmed");
        moon.setIsMoon(true);
        return moon;
    }

    // ========== Utility Methods ==========

    /**
     * Flush and clear the entity manager to ensure queries hit the database.
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
