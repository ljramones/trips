package com.teamgannon.trips.dialogs.solarsystem;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SubScene;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Save a snapshot of a {@link SubScene} as a PNG via a user-driven file chooser.
 * <p>
 * Extracted from {@code ProceduralPlanetViewerDialog} in Phase 4.2 of the
 * codebase-review remediation. Stateless utility — the caller manages any
 * preconditions (e.g. pausing auto-rotation before the snapshot).
 */
@Slf4j
public final class PlanetScreenshotExporter {

    private PlanetScreenshotExporter() {
    }

    /**
     * Open a save dialog and write the {@link SubScene}'s current frame as PNG.
     *
     * @param subScene      the rendered subscene to snapshot (must already be on the FX thread)
     * @param suggestedName initial filename in the chooser (e.g. {@code "Earth_terrain.png"})
     * @param owner         parent window for the modal chooser (may be {@code null})
     * @return the saved file, or empty if the user cancelled or the write failed
     */
    public static Optional<File> saveSnapshot(SubScene subScene, String suggestedName, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Planet Screenshot");
        fileChooser.setInitialFileName(suggestedName);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        File file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return Optional.empty();
        }
        try {
            WritableImage image = subScene.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            log.info("Saved screenshot to: {}", file.getAbsolutePath());
            return Optional.of(file);
        } catch (IOException e) {
            log.error("Failed to save screenshot to {}: {}", file.getAbsolutePath(), e.getMessage(), e);
            return Optional.empty();
        }
    }
}
