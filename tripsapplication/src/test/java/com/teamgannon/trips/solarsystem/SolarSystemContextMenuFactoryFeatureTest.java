package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.model.FeatureDescription;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase E.1 Step 9 — covers {@link SolarSystemContextMenuFactory#createFeatureContextMenu}.
 *
 * <p>Pins the menu shape for JUMP_POINT (title + separator + Properties item) and confirms
 * that non-JUMP_POINT feature types currently only get the title (no Properties dialog wired
 * for ASTEROID_BELT / JUMP_GATE / etc. until those per-type dialogs land in later phases).
 */
class SolarSystemContextMenuFactoryFeatureTest {

    private static boolean javaFxInitialized = false;
    private SolarSystemContextMenuFactory factory;

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
        factory = new SolarSystemContextMenuFactory();
    }

    private FeatureDescription jumpPointFixture() {
        FeatureDescription jp = new FeatureDescription();
        jp.setId("jp-1");
        jp.setName("Jump Point");
        jp.setFeatureType("JUMP_POINT");
        jp.setOrbitalRadiusAU(35.0);
        jp.setOrbitalAngleDeg(60.0);
        return jp;
    }

    @Test
    @DisplayName("JUMP_POINT menu has title + separator + Properties item (3 items total)")
    void jumpPointMenuShape() {
        ContextMenu menu = factory.createFeatureContextMenu(jumpPointFixture(), "Sol");
        List<MenuItem> items = menu.getItems();
        assertEquals(3, items.size());
        assertEquals("Jump Point", items.get(0).getText());
        assertTrue(items.get(0).isDisable(), "title row is disabled (decorative)");
        assertTrue(items.get(1) instanceof SeparatorMenuItem, "second row is the separator");
        assertEquals("Properties...", items.get(2).getText());
        assertNotNull(items.get(2).getOnAction(),
                "Properties item must have an action handler that opens the dialog");
    }

    @Test
    @DisplayName("JUMP_POINT title uses the feature name; falls back to type when name is null")
    void jumpPointTitleFallsBackToTypeWhenNameMissing() {
        FeatureDescription jp = jumpPointFixture();
        jp.setName(null);
        ContextMenu menu = factory.createFeatureContextMenu(jp, "Sol");
        assertEquals("JUMP_POINT", menu.getItems().get(0).getText(),
                "missing name -> show the feature type so the menu stays informative");
    }

    @Test
    @DisplayName("non-JUMP_POINT feature gets title only (no Properties item until per-type dialog lands)")
    void nonJumpPointMenuHasNoPropertiesItem() {
        FeatureDescription belt = new FeatureDescription();
        belt.setName("Main Belt");
        belt.setFeatureType("ASTEROID_BELT");
        ContextMenu menu = factory.createFeatureContextMenu(belt, "Sol");
        List<MenuItem> items = menu.getItems();
        assertEquals(2, items.size(), "title + separator only");
        assertEquals("Main Belt", items.get(0).getText());
        assertTrue(items.get(1) instanceof SeparatorMenuItem);
        boolean hasProperties = items.stream().anyMatch(i -> "Properties...".equals(i.getText()));
        assertFalse(hasProperties,
                "ASTEROID_BELT should not surface a Properties item until its per-type dialog ships");
    }

    @Test
    @DisplayName("JUMP_GATE also gets title-only — confirms JUMP_POINT is the only special-case in E.1")
    void jumpGateMenuIsTitleOnly() {
        FeatureDescription gate = new FeatureDescription();
        gate.setName("Sol Gate");
        gate.setFeatureType("JUMP_GATE");
        ContextMenu menu = factory.createFeatureContextMenu(gate, "Sol");
        boolean hasProperties = menu.getItems().stream().anyMatch(i -> "Properties...".equals(i.getText()));
        assertFalse(hasProperties,
                "JUMP_GATE will need its own per-type dialog (Phase E.2); currently no Properties item");
    }
}
