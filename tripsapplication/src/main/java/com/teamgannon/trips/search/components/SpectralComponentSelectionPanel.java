package com.teamgannon.trips.search.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * Panel for selecting spectral class components for advanced stellar queries.
 * Allows filtering by:
 * - Spectral class letter (O, B, A, F, G, K, M, L, T, Y, etc.)
 * - Subtype number (0-9)
 * - Luminosity class (I, II, III, IV, V, VI, VII)
 *
 * Example: To find "F class stars with subtype 0-5 and luminosity V or VI":
 * - Select "F" in class letters
 * - Check subtypes 0, 1, 2, 3, 4, 5
 * - Check luminosity V and VI
 */
@Slf4j
public class SpectralComponentSelectionPanel extends VBox {

    // Activation checkbox
    private final CheckBox enabledCheckBox = new CheckBox("Enable Component Filter");

    // Spectral class letters (main sequence and common)
    private final CheckBox classO = new CheckBox("O");
    private final CheckBox classB = new CheckBox("B");
    private final CheckBox classA = new CheckBox("A");
    private final CheckBox classF = new CheckBox("F");
    private final CheckBox classG = new CheckBox("G");
    private final CheckBox classK = new CheckBox("K");
    private final CheckBox classM = new CheckBox("M");
    private final CheckBox classL = new CheckBox("L");
    private final CheckBox classT = new CheckBox("T");
    private final CheckBox classY = new CheckBox("Y");

    // Subtype checkboxes (0-9)
    private final CheckBox subtype0 = new CheckBox("0");
    private final CheckBox subtype1 = new CheckBox("1");
    private final CheckBox subtype2 = new CheckBox("2");
    private final CheckBox subtype3 = new CheckBox("3");
    private final CheckBox subtype4 = new CheckBox("4");
    private final CheckBox subtype5 = new CheckBox("5");
    private final CheckBox subtype6 = new CheckBox("6");
    private final CheckBox subtype7 = new CheckBox("7");
    private final CheckBox subtype8 = new CheckBox("8");
    private final CheckBox subtype9 = new CheckBox("9");

    // Luminosity class checkboxes
    private final CheckBox lumI = new CheckBox("I (Supergiants)");
    private final CheckBox lumII = new CheckBox("II (Bright Giants)");
    private final CheckBox lumIII = new CheckBox("III (Giants)");
    private final CheckBox lumIV = new CheckBox("IV (Subgiants)");
    private final CheckBox lumV = new CheckBox("V (Main Sequence)");
    private final CheckBox lumVI = new CheckBox("VI (Subdwarfs)");
    private final CheckBox lumVII = new CheckBox("VII (White Dwarfs)");

    // Select all checkboxes for convenience
    private final CheckBox selectAllClasses = new CheckBox("All Classes");
    private final CheckBox selectAllSubtypes = new CheckBox("All Subtypes");
    private final CheckBox selectAllLuminosity = new CheckBox("All Luminosity");

    // Preview label
    private final Label previewLabel = new Label("Pattern: (none)");

    // Content pane that shows/hides based on enabled state
    private final VBox contentPane = new VBox();

    @Getter
    private boolean enabled = false;

    public SpectralComponentSelectionPanel() {
        this.setSpacing(5);
        this.setPadding(new Insets(5));

        // Enable checkbox at top
        enabledCheckBox.setOnAction(e -> {
            enabled = enabledCheckBox.isSelected();
            contentPane.setVisible(enabled);
            contentPane.setManaged(enabled);
            updatePreview();
        });

        // Build content sections
        contentPane.setSpacing(10);
        contentPane.setVisible(false);
        contentPane.setManaged(false);

        // Spectral Class section
        TitledPane classPane = createClassSelectionPane();

        // Subtype section
        TitledPane subtypePane = createSubtypeSelectionPane();

        // Luminosity class section
        TitledPane luminosityPane = createLuminositySelectionPane();

        // Preview section
        HBox previewBox = new HBox(5);
        previewBox.setAlignment(Pos.CENTER_LEFT);
        previewLabel.setStyle("-fx-font-style: italic;");
        previewBox.getChildren().addAll(new Label("Preview:"), previewLabel);

        contentPane.getChildren().addAll(classPane, subtypePane, luminosityPane, previewBox);

        this.getChildren().addAll(enabledCheckBox, contentPane);

        // Add listeners to update preview
        addPreviewListeners();
    }

    private TitledPane createClassSelectionPane() {
        VBox classBox = new VBox(5);

        // Select all row
        HBox selectAllRow = new HBox(10);
        selectAllRow.setAlignment(Pos.CENTER_LEFT);
        selectAllClasses.setOnAction(e -> {
            boolean selected = selectAllClasses.isSelected();
            classO.setSelected(selected);
            classB.setSelected(selected);
            classA.setSelected(selected);
            classF.setSelected(selected);
            classG.setSelected(selected);
            classK.setSelected(selected);
            classM.setSelected(selected);
            classL.setSelected(selected);
            classT.setSelected(selected);
            classY.setSelected(selected);
            updatePreview();
        });
        selectAllRow.getChildren().add(selectAllClasses);

        // Main sequence classes (O-M)
        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.getChildren().addAll(
                new Label("Main Seq:"),
                classO, classB, classA, classF, classG, classK, classM
        );

        // Extended classes (L, T, Y - brown dwarfs)
        HBox extendedRow = new HBox(10);
        extendedRow.setAlignment(Pos.CENTER_LEFT);
        extendedRow.getChildren().addAll(
                new Label("Brown Dwarfs:"),
                classL, classT, classY
        );

        classBox.getChildren().addAll(selectAllRow, mainRow, extendedRow);

        TitledPane pane = new TitledPane("Spectral Class Letter", classBox);
        pane.setExpanded(false);
        pane.setCollapsible(true);
        return pane;
    }

    private TitledPane createSubtypeSelectionPane() {
        VBox subtypeBox = new VBox(5);

        // Select all row
        HBox selectAllRow = new HBox(10);
        selectAllRow.setAlignment(Pos.CENTER_LEFT);
        selectAllSubtypes.setOnAction(e -> {
            boolean selected = selectAllSubtypes.isSelected();
            subtype0.setSelected(selected);
            subtype1.setSelected(selected);
            subtype2.setSelected(selected);
            subtype3.setSelected(selected);
            subtype4.setSelected(selected);
            subtype5.setSelected(selected);
            subtype6.setSelected(selected);
            subtype7.setSelected(selected);
            subtype8.setSelected(selected);
            subtype9.setSelected(selected);
            updatePreview();
        });
        selectAllRow.getChildren().add(selectAllSubtypes);

        // Subtypes in two rows
        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().addAll(
                new Label("Early (Hot):"),
                subtype0, subtype1, subtype2, subtype3, subtype4
        );

        HBox row2 = new HBox(10);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.getChildren().addAll(
                new Label("Late (Cool):"),
                subtype5, subtype6, subtype7, subtype8, subtype9
        );

        subtypeBox.getChildren().addAll(selectAllRow, row1, row2);

        TitledPane pane = new TitledPane("Subtype (0-9)", subtypeBox);
        pane.setExpanded(false);
        pane.setCollapsible(true);
        return pane;
    }

    private TitledPane createLuminositySelectionPane() {
        VBox lumBox = new VBox(5);

        // Select all row
        HBox selectAllRow = new HBox(10);
        selectAllRow.setAlignment(Pos.CENTER_LEFT);
        selectAllLuminosity.setOnAction(e -> {
            boolean selected = selectAllLuminosity.isSelected();
            lumI.setSelected(selected);
            lumII.setSelected(selected);
            lumIII.setSelected(selected);
            lumIV.setSelected(selected);
            lumV.setSelected(selected);
            lumVI.setSelected(selected);
            lumVII.setSelected(selected);
            updatePreview();
        });
        selectAllRow.getChildren().add(selectAllLuminosity);

        // Luminosity classes in rows
        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().addAll(lumI, lumII, lumIII);

        HBox row2 = new HBox(10);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.getChildren().addAll(lumIV, lumV, lumVI, lumVII);

        lumBox.getChildren().addAll(selectAllRow, row1, row2);

        TitledPane pane = new TitledPane("Luminosity Class", lumBox);
        pane.setExpanded(false);
        pane.setCollapsible(true);
        return pane;
    }

    private void addPreviewListeners() {
        // Add listener to all checkboxes
        CheckBox[] allCheckBoxes = {
                classO, classB, classA, classF, classG, classK, classM, classL, classT, classY,
                subtype0, subtype1, subtype2, subtype3, subtype4, subtype5, subtype6, subtype7, subtype8, subtype9,
                lumI, lumII, lumIII, lumIV, lumV, lumVI, lumVII
        };

        for (CheckBox cb : allCheckBoxes) {
            cb.setOnAction(e -> updatePreview());
        }
    }

    private void updatePreview() {
        if (!enabled) {
            previewLabel.setText("(disabled)");
            return;
        }

        Set<String> classes = getSelectedClasses();
        Set<String> subtypes = getSelectedSubtypes();
        Set<String> luminosities = getSelectedLuminosityClasses();

        if (classes.isEmpty() && subtypes.isEmpty() && luminosities.isEmpty()) {
            previewLabel.setText("(no selection)");
            return;
        }

        StringBuilder preview = new StringBuilder();

        if (!classes.isEmpty()) {
            preview.append("Class: ").append(String.join(",", classes));
        }

        if (!subtypes.isEmpty()) {
            if (preview.length() > 0) preview.append(" + ");
            preview.append("Subtype: ").append(String.join(",", subtypes));
        }

        if (!luminosities.isEmpty()) {
            if (preview.length() > 0) preview.append(" + ");
            preview.append("Lum: ").append(String.join(",", luminosities));
        }

        previewLabel.setText(preview.toString());
    }

    /**
     * Get selected spectral class letters.
     */
    public Set<String> getSelectedClasses() {
        Set<String> selected = new HashSet<>();
        if (classO.isSelected()) selected.add("O");
        if (classB.isSelected()) selected.add("B");
        if (classA.isSelected()) selected.add("A");
        if (classF.isSelected()) selected.add("F");
        if (classG.isSelected()) selected.add("G");
        if (classK.isSelected()) selected.add("K");
        if (classM.isSelected()) selected.add("M");
        if (classL.isSelected()) selected.add("L");
        if (classT.isSelected()) selected.add("T");
        if (classY.isSelected()) selected.add("Y");
        return selected;
    }

    /**
     * Get selected subtypes.
     */
    public Set<String> getSelectedSubtypes() {
        Set<String> selected = new HashSet<>();
        if (subtype0.isSelected()) selected.add("0");
        if (subtype1.isSelected()) selected.add("1");
        if (subtype2.isSelected()) selected.add("2");
        if (subtype3.isSelected()) selected.add("3");
        if (subtype4.isSelected()) selected.add("4");
        if (subtype5.isSelected()) selected.add("5");
        if (subtype6.isSelected()) selected.add("6");
        if (subtype7.isSelected()) selected.add("7");
        if (subtype8.isSelected()) selected.add("8");
        if (subtype9.isSelected()) selected.add("9");
        return selected;
    }

    /**
     * Get selected luminosity classes.
     * Returns the Roman numeral representation.
     */
    public Set<String> getSelectedLuminosityClasses() {
        Set<String> selected = new HashSet<>();
        if (lumI.isSelected()) selected.add("I");
        if (lumII.isSelected()) selected.add("II");
        if (lumIII.isSelected()) selected.add("III");
        if (lumIV.isSelected()) selected.add("IV");
        if (lumV.isSelected()) selected.add("V");
        if (lumVI.isSelected()) selected.add("VI");
        if (lumVII.isSelected()) selected.add("VII");
        return selected;
    }

    /**
     * Check if component filtering is enabled and has selections.
     */
    public boolean hasSelections() {
        return enabled && (!getSelectedClasses().isEmpty()
                || !getSelectedSubtypes().isEmpty()
                || !getSelectedLuminosityClasses().isEmpty());
    }

    /**
     * Reset all selections.
     */
    public void reset() {
        enabledCheckBox.setSelected(false);
        enabled = false;
        contentPane.setVisible(false);
        contentPane.setManaged(false);

        // Reset all checkboxes
        CheckBox[] allCheckBoxes = {
                selectAllClasses, selectAllSubtypes, selectAllLuminosity,
                classO, classB, classA, classF, classG, classK, classM, classL, classT, classY,
                subtype0, subtype1, subtype2, subtype3, subtype4, subtype5, subtype6, subtype7, subtype8, subtype9,
                lumI, lumII, lumIII, lumIV, lumV, lumVI, lumVII
        };

        for (CheckBox cb : allCheckBoxes) {
            cb.setSelected(false);
        }

        updatePreview();
    }
}
