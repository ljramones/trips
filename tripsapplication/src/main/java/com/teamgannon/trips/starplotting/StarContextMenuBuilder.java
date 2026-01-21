package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Builder for star context menus.
 * <p>
 * Creates context menus for stars with configurable actions. The builder pattern
 * allows for flexible menu construction with only the needed items.
 * <p>
 * Menu sections:
 * <ul>
 *   <li>Title - Star name and polity</li>
 *   <li>Star Actions - Highlight, Properties, Recenter, Edit, Delete</li>
 *   <li>Routing - Automated and Manual routing options</li>
 *   <li>Reports - Distance report</li>
 *   <li>Solar System - Enter system, Generate system</li>
 * </ul>
 */
@Slf4j
public class StarContextMenuBuilder {

    // =========================================================================
    // Constants
    // =========================================================================

    /**
     * Font size for context menu titles.
     */
    private static final int TITLE_FONT_SIZE = 20;

    /**
     * Font size for section headers.
     */
    private static final int HEADER_FONT_SIZE = 15;

    /**
     * CSS style for title items.
     */
    private static final String TITLE_STYLE =
            "-fx-text-fill: darkblue; -fx-font-size:" + TITLE_FONT_SIZE + "; -fx-font-weight: bold";

    /**
     * CSS style for section header items.
     */
    private static final String HEADER_STYLE =
            "-fx-font-size:" + HEADER_FONT_SIZE + "; -fx-font-weight: bold";

    // =========================================================================
    // State
    // =========================================================================

    private final ContextMenu contextMenu;
    private final Node starNode;
    private final StarDisplayRecord record;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Create a new context menu builder for a star.
     *
     * @param starNode the star node
     * @param record   the star display record
     */
    public StarContextMenuBuilder(@NotNull Node starNode, @NotNull StarDisplayRecord record) {
        this.contextMenu = new ContextMenu();
        this.starNode = starNode;
        this.record = record;
    }

    // =========================================================================
    // Title Section
    // =========================================================================

    /**
     * Add the title section with star name and polity.
     *
     * @return this builder
     */
    public StarContextMenuBuilder withTitle() {
        String polity = record.getPolity();
        if ("NA".equals(polity)) {
            polity = "Non-aligned";
        }
        String title = record.getStarName() + " (" + polity + ")";
        return withTitle(title);
    }

    /**
     * Add a custom title.
     *
     * @param title the title text
     * @return this builder
     */
    public StarContextMenuBuilder withTitle(String title) {
        MenuItem titleItem = new MenuItem(title);
        titleItem.setStyle(TITLE_STYLE);
        titleItem.setDisable(true);
        contextMenu.getItems().add(titleItem);
        contextMenu.getItems().add(new SeparatorMenuItem());
        return this;
    }

    // =========================================================================
    // Star Action Items
    // =========================================================================

    /**
     * Add the highlight star menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withHighlightAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Highlight star");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the properties menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withPropertiesAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Properties");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the recenter menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withRecenterAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Recenter on this star");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the edit properties menu item.
     *
     * @param action the action to perform when clicked (receives record, node for update)
     * @return this builder
     */
    public StarContextMenuBuilder withEditAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Edit star");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the delete star menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withDeleteAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Delete star");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    // =========================================================================
    // Routing Section
    // =========================================================================

    /**
     * Add the routing section header.
     *
     * @return this builder
     */
    public StarContextMenuBuilder withRoutingHeader() {
        contextMenu.getItems().add(new SeparatorMenuItem());
        MenuItem header = new MenuItem("Routing");
        header.setStyle(HEADER_STYLE);
        header.setDisable(true);
        contextMenu.getItems().add(header);
        return this;
    }

    /**
     * Add the automated routing menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withAutomatedRoutingAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Run route finder/generator");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the manual routing menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withManualRoutingAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Build route on screen by clicking stars");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the start route menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withStartRouteAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Start Route");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the continue route menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withContinueRouteAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Continue Route");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the finish route menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withFinishRouteAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Finish Route");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the remove last route link menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withRemoveRouteAction(@NotNull Runnable action) {
        MenuItem item = new MenuItem("Remove last link from route");
        item.setOnAction(e -> action.run());
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the reset route menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withResetRouteAction(@NotNull Runnable action) {
        MenuItem item = new MenuItem("Route: Start over");
        item.setOnAction(e -> action.run());
        contextMenu.getItems().add(item);
        return this;
    }

    // =========================================================================
    // Report Section
    // =========================================================================

    /**
     * Add the distance report menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withDistanceReportAction(@NotNull Consumer<StarDisplayRecord> action) {
        contextMenu.getItems().add(new SeparatorMenuItem());
        MenuItem item = new MenuItem("Generate distance report from this star");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    // =========================================================================
    // Solar System Section
    // =========================================================================

    /**
     * Add the enter system menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withEnterSystemAction(@NotNull Consumer<StarDisplayRecord> action) {
        contextMenu.getItems().add(new SeparatorMenuItem());
        MenuItem item = new MenuItem("Enter System");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the generate solar system menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withGenerateSolarSystemAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Generate Simulated Solar System from this star");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add the edit notes menu item.
     *
     * @param action the action to perform when clicked
     * @return this builder
     */
    public StarContextMenuBuilder withEditNotesAction(@NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem("Edit notes on this star");
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    /**
     * Add a separator.
     *
     * @return this builder
     */
    public StarContextMenuBuilder withSeparator() {
        contextMenu.getItems().add(new SeparatorMenuItem());
        return this;
    }

    /**
     * Add a custom menu item.
     *
     * @param label  the menu item label
     * @param action the action to perform
     * @return this builder
     */
    public StarContextMenuBuilder withCustomAction(@NotNull String label, @NotNull Consumer<StarDisplayRecord> action) {
        MenuItem item = new MenuItem(label);
        item.setOnAction(e -> action.accept(record));
        contextMenu.getItems().add(item);
        return this;
    }

    /**
     * Add a section header.
     *
     * @param headerText the header text
     * @return this builder
     */
    public StarContextMenuBuilder withSectionHeader(@NotNull String headerText) {
        MenuItem header = new MenuItem(headerText);
        header.setStyle(HEADER_STYLE);
        header.setDisable(true);
        contextMenu.getItems().add(header);
        return this;
    }

    // =========================================================================
    // Build
    // =========================================================================

    /**
     * Build the context menu.
     *
     * @return the constructed ContextMenu
     */
    public ContextMenu build() {
        return contextMenu;
    }

    /**
     * Get the star node this menu is for.
     *
     * @return the star node
     */
    public Node getStarNode() {
        return starNode;
    }

    /**
     * Get the star record this menu is for.
     *
     * @return the star display record
     */
    public StarDisplayRecord getRecord() {
        return record;
    }

    /**
     * Get the number of items in the menu.
     *
     * @return item count
     */
    public int getItemCount() {
        return contextMenu.getItems().size();
    }
}
