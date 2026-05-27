package com.teamgannon.trips.search.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Shared base for the search-side selection panels under this package
 * (CategorySelectionPanel, DataSetPanel, DistanceSelectionPanel, etc.).
 * <p>
 * Provides:
 * <ul>
 *   <li>{@link #LABEL_PREF_WIDTH} + {@link #applyLabelStyle(Label)} / {@link #createLabel(String)} —
 *       consistent label styling across all panels.</li>
 *   <li>{@link #loadFxml(String)} — single-call FXML loader that wires
 *       {@code this} as both root and controller, replacing the 12-line
 *       boilerplate that used to live in every panel's constructor
 *       (Issue 55 / Phase 7.13).</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * Subclass constructors collapse from a ~12-line FXML-load block to a
 * one-line super call:
 * <pre>
 *   public CategorySelectionPanel() {
 *       loadFxml("CategorySelectionPanel.fxml");
 *   }
 * </pre>
 */
public class BasePane extends GridPane {

    protected final static float LABEL_PREF_WIDTH = 150;

    public GridPane getPane() {
        return this;
    }

    protected @NotNull Label createLabel(String textName) {
        Label label = new Label(textName);
        label.setPrefWidth(LABEL_PREF_WIDTH);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        return label;
    }

    protected void applyLabelStyle(@NotNull Label label) {
        label.setPrefWidth(LABEL_PREF_WIDTH);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
    }

    /**
     * Load an FXML resource alongside this class, wiring {@code this} as
     * both the root and the controller. Subclasses call this from their
     * no-arg constructor: {@code loadFxml("MyPanel.fxml")}.
     *
     * @param fxmlFileName FXML filename, resolved relative to the subclass's
     *                     classpath location (i.e. same package as the .class).
     * @throws IllegalStateException if the resource is missing or malformed
     */
    protected final void loadFxml(String fxmlFileName) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load " + fxmlFileName, ex);
        }
    }
}
