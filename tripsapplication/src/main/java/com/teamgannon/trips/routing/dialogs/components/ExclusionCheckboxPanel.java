package com.teamgannon.trips.routing.dialogs.components;

import com.teamgannon.trips.routing.RoutingConstants;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Reusable panel for displaying exclusion checkboxes.
 * <p>
 * Eliminates duplicated checkbox creation code across routing dialogs.
 * Supports both spectral class exclusions and polity exclusions.
 */
public class ExclusionCheckboxPanel extends VBox {

    /**
     * Standard spectral classes used in route finding.
     */
    public static final List<String> SPECTRAL_CLASSES = List.of(
            "O", "B", "A", "F", "G", "K", "M", "W", "L", "T", "Y", "C", "S"
    );

    /**
     * Standard polities used in the application (Terran Republic universe).
     */
    public static final List<String> POLITIES = List.of(
            "Terran", "Dornani", "Ktor", "Arat Kur", "Hkh'Rkh", "Slaasriithi",
            "Other 1", "Other 2", "Other 3", "Other 4"
    );

    private final Map<String, CheckBox> checkboxMap = new LinkedHashMap<>();
    private final int columnsPerRow;

    /**
     * Creates an exclusion checkbox panel with the given items.
     *
     * @param title         the title shown above the checkboxes
     * @param items         the list of items to create checkboxes for
     * @param columnsPerRow number of columns to arrange checkboxes in
     */
    public ExclusionCheckboxPanel(String title, List<String> items, int columnsPerRow) {
        this.columnsPerRow = columnsPerRow;

        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setFont(RoutingConstants.createDialogFont());
        this.getChildren().add(titleLabel);
        this.getChildren().add(new Separator());

        // Create checkboxes arranged in columns
        HBox hBox = new HBox();
        this.getChildren().add(hBox);

        List<VBox> columns = new ArrayList<>();
        for (int i = 0; i < columnsPerRow; i++) {
            VBox column = new VBox();
            columns.add(column);
            hBox.getChildren().add(column);
        }

        // Distribute checkboxes across columns
        int itemsPerColumn = (int) Math.ceil((double) items.size() / columnsPerRow);
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            CheckBox checkBox = new CheckBox(item);
            checkBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
            checkboxMap.put(item, checkBox);

            int columnIndex = i / itemsPerColumn;
            if (columnIndex < columns.size()) {
                columns.get(columnIndex).getChildren().add(checkBox);
            }
        }
    }

    /**
     * Creates a spectral class exclusion panel with default layout.
     *
     * @return a new panel configured for spectral classes
     */
    public static ExclusionCheckboxPanel createSpectralClassPanel() {
        return new ExclusionCheckboxPanel(
                "Select stars to exclude in our route finding",
                SPECTRAL_CLASSES,
                3
        );
    }

    /**
     * Creates a polity exclusion panel with default layout.
     *
     * @return a new panel configured for polities
     */
    public static ExclusionCheckboxPanel createPolityPanel() {
        return new ExclusionCheckboxPanel(
                "Select polities to exclude in our route finding",
                POLITIES,
                2
        );
    }

    /**
     * Gets the set of selected exclusion values.
     *
     * @return set of selected item names
     */
    public @NotNull Set<String> getSelectedExclusions() {
        Set<String> exclusions = new HashSet<>();
        for (Map.Entry<String, CheckBox> entry : checkboxMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                exclusions.add(entry.getKey());
            }
        }
        return exclusions;
    }

    /**
     * Checks if a specific item is selected.
     *
     * @param item the item name to check
     * @return true if the checkbox is selected, false otherwise
     */
    public boolean isSelected(String item) {
        CheckBox checkBox = checkboxMap.get(item);
        return checkBox != null && checkBox.isSelected();
    }

    /**
     * Sets the selection state of a specific item.
     *
     * @param item     the item name
     * @param selected the selection state
     */
    public void setSelected(String item, boolean selected) {
        CheckBox checkBox = checkboxMap.get(item);
        if (checkBox != null) {
            checkBox.setSelected(selected);
        }
    }

    /**
     * Clears all selections.
     */
    public void clearAll() {
        checkboxMap.values().forEach(cb -> cb.setSelected(false));
    }

    /**
     * Selects all checkboxes.
     */
    public void selectAll() {
        checkboxMap.values().forEach(cb -> cb.setSelected(true));
    }

    /**
     * Gets the checkbox for a specific item.
     *
     * @param item the item name
     * @return the checkbox, or null if not found
     */
    public CheckBox getCheckbox(String item) {
        return checkboxMap.get(item);
    }
}
