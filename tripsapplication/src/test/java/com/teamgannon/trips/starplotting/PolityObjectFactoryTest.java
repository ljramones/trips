package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.objects.MeshViewShapeFactory;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PolityObjectFactory.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Creating polity objects for all known polity types</li>
 *   <li>Color application</li>
 *   <li>Scale and rotation configuration</li>
 *   <li>Unknown polity handling</li>
 *   <li>MeshObjectDefinition creation</li>
 * </ul>
 */
class PolityObjectFactoryTest {

    private static boolean javaFxInitialized = false;
    private PolityObjectFactory factory;
    private MeshViewShapeFactory meshViewShapeFactory;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        meshViewShapeFactory = new MeshViewShapeFactory();
        factory = new PolityObjectFactory(meshViewShapeFactory);
    }

    // =========================================================================
    // Terran Polity Tests
    // =========================================================================

    @Nested
    @DisplayName("Terran Polity Tests")
    class TerranPolityTests {

        @Test
        @DisplayName("Creates Terran polity object (cube)")
        void createsTerranPolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.TERRAN, prefs);

                assertNotNull(mesh, "Terran polity should create a mesh");
                return null;
            });
        }

        @Test
        @DisplayName("Creates Slaasriithi polity object (same as Terran)")
        void createsSlaasriithiPolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.SLAASRIITHI, prefs);

                assertNotNull(mesh, "Slaasriithi polity should create a mesh");
                return null;
            });
        }

        @Test
        @DisplayName("Terran and Slaasriithi use same shape name")
        void terranAndSlaasriithiUseSameShape() {
            String terranShape = factory.getPolityShapeName(CivilizationDisplayPreferences.TERRAN);
            String slaasriithiShape = factory.getPolityShapeName(CivilizationDisplayPreferences.SLAASRIITHI);

            assertEquals(terranShape, slaasriithiShape);
            assertEquals(PolityObjectFactory.POLITY_TERRAN, terranShape);
        }
    }

    // =========================================================================
    // Dornani Polity Tests
    // =========================================================================

    @Nested
    @DisplayName("Dornani Polity Tests")
    class DornaniPolityTests {

        @Test
        @DisplayName("Creates Dornani polity object (tetrahedron)")
        void createsDornaniPolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.DORNANI, prefs);

                assertNotNull(mesh, "Dornani polity should create a mesh");
                return null;
            });
        }

        @Test
        @DisplayName("Creates Other1 polity object (same as Dornani)")
        void createsOther1PolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.OTHER1, prefs);

                assertNotNull(mesh, "Other1 polity should create a mesh");
                return null;
            });
        }
    }

    // =========================================================================
    // Ktor Polity Tests
    // =========================================================================

    @Nested
    @DisplayName("Ktor Polity Tests")
    class KtorPolityTests {

        @Test
        @DisplayName("Creates Ktor polity object (icosahedron)")
        void createsKtorPolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.KTOR, prefs);

                assertNotNull(mesh, "Ktor polity should create a mesh");
                return null;
            });
        }

        @Test
        @DisplayName("Creates Other3 polity object (same as Ktor)")
        void createsOther3PolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.OTHER3, prefs);

                assertNotNull(mesh, "Other3 polity should create a mesh");
                return null;
            });
        }
    }

    // =========================================================================
    // Arat Kur Polity Tests
    // =========================================================================

    @Nested
    @DisplayName("Arat Kur Polity Tests")
    class AratKurPolityTests {

        @Test
        @DisplayName("Creates Arat Kur polity object (octahedron)")
        void createsAratKurPolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.ARAKUR, prefs);

                assertNotNull(mesh, "Arat Kur polity should create a mesh");
                return null;
            });
        }

        @Test
        @DisplayName("Creates Other2 polity object (same as Arat Kur)")
        void createsOther2PolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.OTHER2, prefs);

                assertNotNull(mesh, "Other2 polity should create a mesh");
                return null;
            });
        }
    }

    // =========================================================================
    // Hkh'Rkh Polity Tests
    // =========================================================================

    @Nested
    @DisplayName("Hkh'Rkh Polity Tests")
    class HkhRkhPolityTests {

        @Test
        @DisplayName("Creates Hkh'Rkh polity object (dodecahedron)")
        void createsHkhRkhPolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.HKHRKH, prefs);

                assertNotNull(mesh, "Hkh'Rkh polity should create a mesh");
                return null;
            });
        }

        @Test
        @DisplayName("Creates Other4 polity object (same as Hkh'Rkh)")
        void createsOther4PolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.OTHER4, prefs);

                assertNotNull(mesh, "Other4 polity should create a mesh");
                return null;
            });
        }
    }

    // =========================================================================
    // Color Application Tests
    // =========================================================================

    @Nested
    @DisplayName("Color Application Tests")
    class ColorApplicationTests {

        @Test
        @DisplayName("Applies color to polity object")
        void appliesColorToPolityObject() throws Exception {
            runOnFxThread(() -> {
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.TERRAN, Color.RED);

                assertNotNull(mesh);
                PhongMaterial material = (PhongMaterial) mesh.getMaterial();
                assertNotNull(material);
                assertEquals(Color.RED, material.getDiffuseColor());
                assertEquals(Color.RED, material.getSpecularColor());
                return null;
            });
        }

        @Test
        @DisplayName("Different colors for different polities")
        void differentColorsForDifferentPolities() throws Exception {
            runOnFxThread(() -> {
                MeshView terranMesh = factory.createPolityObject(CivilizationDisplayPreferences.TERRAN, Color.BLUE);
                MeshView ktorMesh = factory.createPolityObject(CivilizationDisplayPreferences.KTOR, Color.GREEN);

                PhongMaterial terranMaterial = (PhongMaterial) terranMesh.getMaterial();
                PhongMaterial ktorMaterial = (PhongMaterial) ktorMesh.getMaterial();

                assertEquals(Color.BLUE, terranMaterial.getDiffuseColor());
                assertEquals(Color.GREEN, ktorMaterial.getDiffuseColor());
                return null;
            });
        }
    }

    // =========================================================================
    // Scale and Rotation Tests
    // =========================================================================

    @Nested
    @DisplayName("Scale and Rotation Tests")
    class ScaleAndRotationTests {

        @Test
        @DisplayName("Applies default scale to polity object")
        void appliesDefaultScale() throws Exception {
            runOnFxThread(() -> {
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.TERRAN, Color.WHITE);

                assertNotNull(mesh);
                assertEquals(1.0, mesh.getScaleX(), 0.001);
                assertEquals(1.0, mesh.getScaleY(), 0.001);
                assertEquals(1.0, mesh.getScaleZ(), 0.001);
                return null;
            });
        }

        @Test
        @DisplayName("Applies rotation to polity object")
        void appliesRotation() throws Exception {
            runOnFxThread(() -> {
                MeshView mesh = factory.createPolityObject(CivilizationDisplayPreferences.TERRAN, Color.WHITE);

                assertNotNull(mesh);
                assertEquals(90.0, mesh.getRotate(), 0.001);
                return null;
            });
        }
    }

    // =========================================================================
    // Positioned Polity Tests
    // =========================================================================

    @Nested
    @DisplayName("Positioned Polity Tests")
    class PositionedPolityTests {

        @Test
        @DisplayName("Creates positioned polity object")
        void createsPositionedPolityObject() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPositionedPolityObject(
                        CivilizationDisplayPreferences.TERRAN, prefs, 100, 200, 50);

                assertNotNull(mesh);
                assertEquals(100, mesh.getTranslateX(), 0.001);
                assertEquals(200, mesh.getTranslateY(), 0.001);
                assertEquals(50, mesh.getTranslateZ(), 0.001);
                return null;
            });
        }

        @Test
        @DisplayName("Positioned polity with negative coordinates")
        void positionedPolityWithNegativeCoords() throws Exception {
            runOnFxThread(() -> {
                CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
                MeshView mesh = factory.createPositionedPolityObject(
                        CivilizationDisplayPreferences.DORNANI, prefs, -50, -100, -25);

                assertNotNull(mesh);
                assertEquals(-50, mesh.getTranslateX(), 0.001);
                assertEquals(-100, mesh.getTranslateY(), 0.001);
                assertEquals(-25, mesh.getTranslateZ(), 0.001);
                return null;
            });
        }
    }

    // =========================================================================
    // MeshObjectDefinition Tests
    // =========================================================================

    @Nested
    @DisplayName("MeshObjectDefinition Tests")
    class MeshObjectDefinitionTests {

        @Test
        @DisplayName("Creates polity definition with correct name")
        void createsPolityDefinitionWithCorrectName() throws Exception {
            runOnFxThread(() -> {
                MeshObjectDefinition def = factory.createPolityDefinition(CivilizationDisplayPreferences.TERRAN);

                assertNotNull(def);
                assertEquals(PolityObjectFactory.POLITY_TERRAN, def.getName());
                return null;
            });
        }

        @Test
        @DisplayName("Creates polity definition with mesh object")
        void createsPolityDefinitionWithMeshObject() throws Exception {
            runOnFxThread(() -> {
                MeshObjectDefinition def = factory.createPolityDefinition(CivilizationDisplayPreferences.KTOR);

                assertNotNull(def);
                assertNotNull(def.getObject());
                assertTrue(def.getObject() instanceof MeshView);
                return null;
            });
        }

        @Test
        @DisplayName("Creates polity definition with correct scale")
        void createsPolityDefinitionWithCorrectScale() throws Exception {
            runOnFxThread(() -> {
                MeshObjectDefinition def = factory.createPolityDefinition(CivilizationDisplayPreferences.DORNANI);

                assertNotNull(def);
                assertEquals(1.0, def.getXScale(), 0.001);
                assertEquals(1.0, def.getYScale(), 0.001);
                assertEquals(1.0, def.getZScale(), 0.001);
                return null;
            });
        }

        @Test
        @DisplayName("Creates polity definition with UUID")
        void createsPolityDefinitionWithUUID() throws Exception {
            runOnFxThread(() -> {
                MeshObjectDefinition def = factory.createPolityDefinition(CivilizationDisplayPreferences.HKHRKH);

                assertNotNull(def);
                assertNotNull(def.getId());
                return null;
            });
        }
    }

    // =========================================================================
    // Unknown Polity Tests
    // =========================================================================

    @Nested
    @DisplayName("Unknown Polity Tests")
    class UnknownPolityTests {

        @Test
        @DisplayName("Returns null for unknown polity")
        void returnsNullForUnknownPolity() throws Exception {
            runOnFxThread(() -> {
                MeshView mesh = factory.createPolityObject("UnknownPolity", Color.WHITE);

                assertNull(mesh, "Unknown polity should return null");
                return null;
            });
        }

        @Test
        @DisplayName("Returns empty definition for unknown polity")
        void returnsEmptyDefinitionForUnknownPolity() throws Exception {
            runOnFxThread(() -> {
                MeshObjectDefinition def = factory.createPolityDefinition("UnknownPolity");

                assertNotNull(def);
                assertNull(def.getObject(), "Unknown polity definition should have null object");
                return null;
            });
        }

        @Test
        @DisplayName("isPolitySupported returns false for unknown polity")
        void isPolityNotSupportedForUnknown() {
            assertFalse(factory.isPolitySupported("UnknownPolity"));
        }

        @Test
        @DisplayName("getPolityShapeName returns null for unknown polity")
        void getPolityShapeNameReturnsNullForUnknown() {
            assertNull(factory.getPolityShapeName("UnknownPolity"));
        }
    }

    // =========================================================================
    // Query Method Tests
    // =========================================================================

    @Nested
    @DisplayName("Query Method Tests")
    class QueryMethodTests {

        @Test
        @DisplayName("isPolitySupported returns true for all known polities")
        void isPolitySupported() {
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.TERRAN));
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.SLAASRIITHI));
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.DORNANI));
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.OTHER1));
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.KTOR));
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.OTHER3));
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.ARAKUR));
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.OTHER2));
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.HKHRKH));
            assertTrue(factory.isPolitySupported(CivilizationDisplayPreferences.OTHER4));
        }

        @Test
        @DisplayName("getPolityShapeName returns correct names")
        void getPolityShapeNameReturnsCorrectNames() {
            assertEquals(PolityObjectFactory.POLITY_TERRAN,
                    factory.getPolityShapeName(CivilizationDisplayPreferences.TERRAN));
            assertEquals(PolityObjectFactory.POLITY_DORNANI,
                    factory.getPolityShapeName(CivilizationDisplayPreferences.DORNANI));
            assertEquals(PolityObjectFactory.POLITY_KTOR,
                    factory.getPolityShapeName(CivilizationDisplayPreferences.KTOR));
            assertEquals(PolityObjectFactory.POLITY_ARAT_KUR,
                    factory.getPolityShapeName(CivilizationDisplayPreferences.ARAKUR));
            assertEquals(PolityObjectFactory.POLITY_HKH_RKH,
                    factory.getPolityShapeName(CivilizationDisplayPreferences.HKHRKH));
        }
    }

    // =========================================================================
    // Distinct Instance Tests
    // =========================================================================

    @Nested
    @DisplayName("Distinct Instance Tests")
    class DistinctInstanceTests {

        @Test
        @DisplayName("Each call creates a distinct mesh instance")
        void eachCallCreatesDistinctInstance() throws Exception {
            runOnFxThread(() -> {
                MeshView mesh1 = factory.createPolityObject(CivilizationDisplayPreferences.TERRAN, Color.RED);
                MeshView mesh2 = factory.createPolityObject(CivilizationDisplayPreferences.TERRAN, Color.BLUE);

                assertNotNull(mesh1);
                assertNotNull(mesh2);
                assertNotSame(mesh1, mesh2, "Each call should create a distinct instance");
                return null;
            });
        }

        @Test
        @DisplayName("Multiple polity objects can be added to different parents")
        void multiplePolityObjectsCanHaveDifferentParents() throws Exception {
            runOnFxThread(() -> {
                MeshView mesh1 = factory.createPolityObject(CivilizationDisplayPreferences.TERRAN, Color.RED);
                MeshView mesh2 = factory.createPolityObject(CivilizationDisplayPreferences.TERRAN, Color.BLUE);

                javafx.scene.Group parent1 = new javafx.scene.Group();
                javafx.scene.Group parent2 = new javafx.scene.Group();

                parent1.getChildren().add(mesh1);
                parent2.getChildren().add(mesh2);

                assertTrue(parent1.getChildren().contains(mesh1));
                assertTrue(parent2.getChildren().contains(mesh2));
                assertEquals(1, parent1.getChildren().size());
                assertEquals(1, parent2.getChildren().size());
                return null;
            });
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private <T> T runOnFxThread(java.util.concurrent.Callable<T> callable) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return callable.call();
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                result.set(callable.call());
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(30, TimeUnit.SECONDS), "JavaFX operation timed out");

        if (exception.get() != null) {
            throw exception.get();
        }

        return result.get();
    }
}
