package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for StarRenderer.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StarRendererTest {

    @Mock
    private StarLODManager lodManager;

    @Mock
    private StarLabelManager labelManager;

    @Mock
    private InterstellarScaleManager scaleManager;

    @Mock
    private SpecialStarMeshManager meshManager;

    @Mock
    private PolityObjectFactory polityObjectFactory;

    @Mock
    private StarClickHandler clickHandler;

    private StarRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new StarRenderer(
                lodManager,
                labelManager,
                scaleManager,
                meshManager,
                polityObjectFactory,
                clickHandler
        );
    }

    @Nested
    @DisplayName("Material caching tests")
    class MaterialCachingTests {

        @Test
        @DisplayName("should return same material for same color")
        void shouldReturnSameMaterialForSameColor() {
            Color color = Color.RED;

            PhongMaterial material1 = renderer.getCachedMaterial(color);
            PhongMaterial material2 = renderer.getCachedMaterial(color);

            assertSame(material1, material2, "Should return cached material for same color");
        }

        @Test
        @DisplayName("should return different materials for different colors")
        void shouldReturnDifferentMaterialsForDifferentColors() {
            PhongMaterial redMaterial = renderer.getCachedMaterial(Color.RED);
            PhongMaterial blueMaterial = renderer.getCachedMaterial(Color.BLUE);

            assertNotSame(redMaterial, blueMaterial, "Should return different materials for different colors");
        }

        @Test
        @DisplayName("should set diffuse and specular color on material")
        void shouldSetDiffuseAndSpecularColor() {
            Color color = Color.YELLOW;

            PhongMaterial material = renderer.getCachedMaterial(color);

            assertEquals(color, material.getDiffuseColor());
            assertEquals(color, material.getSpecularColor());
        }

        @Test
        @DisplayName("should track material cache size")
        void shouldTrackMaterialCacheSize() {
            assertEquals(0, renderer.getMaterialCacheSize());

            renderer.getCachedMaterial(Color.RED);
            assertEquals(1, renderer.getMaterialCacheSize());

            renderer.getCachedMaterial(Color.BLUE);
            assertEquals(2, renderer.getMaterialCacheSize());

            // Same color shouldn't increase cache size
            renderer.getCachedMaterial(Color.RED);
            assertEquals(2, renderer.getMaterialCacheSize());
        }

        @Test
        @DisplayName("should clear material cache")
        void shouldClearMaterialCache() {
            renderer.getCachedMaterial(Color.RED);
            renderer.getCachedMaterial(Color.BLUE);
            assertEquals(2, renderer.getMaterialCacheSize());

            renderer.clearCaches();

            assertEquals(0, renderer.getMaterialCacheSize());
        }
    }

    @Nested
    @DisplayName("Node batching tests")
    class NodeBatchingTests {

        @Test
        @DisplayName("should start with empty pending nodes")
        void shouldStartWithEmptyPendingNodes() {
            assertTrue(renderer.getPendingStarNodes().isEmpty());
            assertTrue(renderer.getPendingPolityNodes().isEmpty());
        }

        @Test
        @DisplayName("should add pending star node")
        void shouldAddPendingStarNode() {
            Sphere sphere = new Sphere(5);

            renderer.addPendingStarNode(sphere);

            assertEquals(1, renderer.getPendingStarNodes().size());
            assertSame(sphere, renderer.getPendingStarNodes().get(0));
        }

        @Test
        @DisplayName("should clear pending nodes")
        void shouldClearPendingNodes() {
            renderer.addPendingStarNode(new Sphere(5));
            renderer.addPendingStarNode(new Sphere(3));

            renderer.clearPendingNodes();

            assertTrue(renderer.getPendingStarNodes().isEmpty());
            assertTrue(renderer.getPendingPolityNodes().isEmpty());
        }

        @Test
        @DisplayName("should clear all caches including pending nodes")
        void shouldClearAllCachesIncludingPendingNodes() {
            renderer.getCachedMaterial(Color.RED);
            renderer.addPendingStarNode(new Sphere(5));

            renderer.clearCaches();

            assertEquals(0, renderer.getMaterialCacheSize());
            assertTrue(renderer.getPendingStarNodes().isEmpty());
        }
    }

    @Nested
    @DisplayName("Star creation tests")
    class StarCreationTests {

        @Test
        @DisplayName("should create center star using mesh manager")
        void shouldCreateCenterStarUsingMeshManager() {
            StarDisplayRecord record = createMockRecord("CenterStar", 0, 0, 0);
            when(meshManager.createCentralStar()).thenReturn(new Sphere(10));
            when(scaleManager.getStarSizeMultiplier()).thenReturn(1.0);

            Node star = renderer.createStar(
                    record,
                    true,  // isCenter
                    new ColorPalette(),
                    new StarDisplayPreferences(),
                    new CivilizationDisplayPreferences(),
                    false,
                    false
            );

            assertNotNull(star);
            verify(meshManager).createCentralStar();
        }

        @Test
        @DisplayName("should create non-center star using LOD manager")
        void shouldCreateNonCenterStarUsingLodManager() {
            StarDisplayRecord record = createMockRecord("RegularStar", 10, 20, 30);
            Sphere expectedStar = new Sphere(5);
            when(scaleManager.getStarSizeMultiplier()).thenReturn(1.0);
            when(lodManager.determineLODLevel(any(), eq(false))).thenReturn(StarLODManager.LODLevel.MEDIUM);
            when(lodManager.createStarWithLOD(any(), anyDouble(), any(), any())).thenReturn(expectedStar);

            Node star = renderer.createStar(
                    record,
                    false,  // not center
                    new ColorPalette(),
                    new StarDisplayPreferences(),
                    new CivilizationDisplayPreferences(),
                    false,
                    false
            );

            assertNotNull(star);
            verify(lodManager).createStarWithLOD(any(), anyDouble(), any(), any());
        }

        @Test
        @DisplayName("should position star at coordinates")
        void shouldPositionStarAtCoordinates() {
            StarDisplayRecord record = createMockRecord("Star", 100, 200, 300);
            Sphere sphere = new Sphere(5);
            when(scaleManager.getStarSizeMultiplier()).thenReturn(1.0);
            when(lodManager.determineLODLevel(any(), eq(false))).thenReturn(StarLODManager.LODLevel.MEDIUM);
            when(lodManager.createStarWithLOD(any(), anyDouble(), any(), any())).thenReturn(sphere);

            Node star = renderer.createStar(
                    record,
                    false,
                    new ColorPalette(),
                    new StarDisplayPreferences(),
                    new CivilizationDisplayPreferences(),
                    false,
                    false
            );

            assertEquals(100, star.getTranslateX(), 0.001);
            assertEquals(200, star.getTranslateY(), 0.001);
            assertEquals(300, star.getTranslateZ(), 0.001);
        }

        @Test
        @DisplayName("should set star ID and user data")
        void shouldSetStarIdAndUserData() {
            StarDisplayRecord record = createMockRecord("TestStar", 0, 0, 0);
            when(scaleManager.getStarSizeMultiplier()).thenReturn(1.0);
            when(lodManager.determineLODLevel(any(), eq(false))).thenReturn(StarLODManager.LODLevel.MEDIUM);
            when(lodManager.createStarWithLOD(any(), anyDouble(), any(), any())).thenReturn(new Sphere(5));

            Node star = renderer.createStar(
                    record,
                    false,
                    new ColorPalette(),
                    new StarDisplayPreferences(),
                    new CivilizationDisplayPreferences(),
                    false,
                    false
            );

            assertEquals("regularStar", star.getId());
            assertSame(record, star.getUserData());
        }

        @Test
        @DisplayName("should setup lazy context menu")
        void shouldSetupLazyContextMenu() {
            StarDisplayRecord record = createMockRecord("Star", 0, 0, 0);
            when(scaleManager.getStarSizeMultiplier()).thenReturn(1.0);
            when(lodManager.determineLODLevel(any(), eq(false))).thenReturn(StarLODManager.LODLevel.MEDIUM);
            when(lodManager.createStarWithLOD(any(), anyDouble(), any(), any())).thenReturn(new Sphere(5));

            renderer.createStar(
                    record,
                    false,
                    new ColorPalette(),
                    new StarDisplayPreferences(),
                    new CivilizationDisplayPreferences(),
                    false,
                    false
            );

            verify(clickHandler).setupLazyContextMenu(eq(record), any(Node.class));
        }

        @Test
        @DisplayName("should call label manager when labels enabled")
        void shouldCallLabelManagerWhenLabelsEnabled() {
            StarDisplayRecord record = createMockRecord("LabeledStar", 0, 0, 0);
            when(scaleManager.getStarSizeMultiplier()).thenReturn(1.0);
            when(lodManager.determineLODLevel(any(), eq(false))).thenReturn(StarLODManager.LODLevel.MEDIUM);
            when(lodManager.createStarWithLOD(any(), anyDouble(), any(), any())).thenReturn(new Sphere(5));
            // Return null since we can't create Label without JavaFX toolkit
            when(labelManager.addLabel(any(), any(), any())).thenReturn(null);

            renderer.createStar(
                    record,
                    false,
                    new ColorPalette(),
                    new StarDisplayPreferences(),
                    new CivilizationDisplayPreferences(),
                    true,  // labels on
                    false
            );

            verify(labelManager).addLabel(any(Node.class), eq(record), any(ColorPalette.class));
        }

        @Test
        @DisplayName("should not add label when labels disabled")
        void shouldNotAddLabelWhenLabelsDisabled() {
            StarDisplayRecord record = createMockRecord("NoLabelStar", 0, 0, 0);
            when(scaleManager.getStarSizeMultiplier()).thenReturn(1.0);
            when(lodManager.determineLODLevel(any(), eq(false))).thenReturn(StarLODManager.LODLevel.MEDIUM);
            when(lodManager.createStarWithLOD(any(), anyDouble(), any(), any())).thenReturn(new Sphere(5));

            renderer.createStar(
                    record,
                    false,
                    new ColorPalette(),
                    new StarDisplayPreferences(),
                    new CivilizationDisplayPreferences(),
                    false,  // labels off
                    false
            );

            verify(labelManager, never()).addLabel(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Label registrar tests")
    class LabelRegistrarTests {

        @Test
        @DisplayName("should call label registrar when set and label created")
        void shouldCallLabelRegistrarWhenSetAndLabelCreated() {
            AtomicReference<String> capturedId = new AtomicReference<>();
            AtomicReference<Label> capturedLabel = new AtomicReference<>();
            renderer.setLabelRegistrar((id, label) -> {
                capturedId.set(id);
                capturedLabel.set(label);
            });

            StarDisplayRecord record = createMockRecord("Star", 0, 0, 0);
            when(record.getRecordId()).thenReturn("test-id-123");
            when(scaleManager.getStarSizeMultiplier()).thenReturn(1.0);
            when(lodManager.determineLODLevel(any(), eq(false))).thenReturn(StarLODManager.LODLevel.MEDIUM);
            when(lodManager.createStarWithLOD(any(), anyDouble(), any(), any())).thenReturn(new Sphere(5));
            // Return null since we can't create Label without JavaFX toolkit
            // The registrar will be called with null, which is acceptable for this test
            when(labelManager.addLabel(any(), any(), any())).thenReturn(null);

            renderer.createStar(
                    record,
                    false,
                    new ColorPalette(),
                    new StarDisplayPreferences(),
                    new CivilizationDisplayPreferences(),
                    true,
                    false
            );

            assertEquals("test-id-123", capturedId.get());
            // Label is null because we can't create Label without JavaFX toolkit
            assertNull(capturedLabel.get());
        }
    }

    private StarDisplayRecord createMockRecord(String name, double x, double y, double z) {
        StarDisplayRecord record = mock(StarDisplayRecord.class, withSettings().lenient());
        lenient().when(record.getStarName()).thenReturn(name);
        lenient().when(record.getRecordId()).thenReturn("id-" + name);
        lenient().when(record.getCoordinates()).thenReturn(new Point3D(x, y, z));
        lenient().when(record.getStarColor()).thenReturn(Color.YELLOW);
        lenient().when(record.getRadius()).thenReturn(5.0);
        lenient().when(record.getPolity()).thenReturn("NA");
        return record;
    }
}
