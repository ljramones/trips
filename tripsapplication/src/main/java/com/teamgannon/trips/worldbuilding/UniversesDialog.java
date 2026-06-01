package com.teamgannon.trips.worldbuilding;

import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Worldbuilding > Universes dialog — the user-visible toggle that drives F.1's activation API.
 *
 * <p>v2 Phase F.1 §6 — the user opens this dialog, sees all installed universes, and toggles
 * each one's active state via a checkbox. Each toggle calls
 * {@link UniverseDesignerService#activate(String)} / {@link UniverseDesignerService#deactivate(String)},
 * which publishes a {@link UniverseActivationChangedEvent}. The
 * {@link UniverseFilteringService} broker fans out the event to subscribed designer panels +
 * back to this dialog, which re-renders so external changes (e.g. another dialog activating a
 * universe) reflect immediately.
 *
 * <p>Hosted in a modeless {@link Stage} by {@code WorldbuildingMenuController.openUniversesDialog}.
 * Modeless so the user can have this dialog open alongside the Spaceship Modeller / Installations
 * Designer windows and see live filtering on toggle. Modality (the design doc §6.1's "dialog"
 * framing) would block other windows, defeating the cross-panel live-refresh.
 *
 * <p>Layout: list (top, with active-checkbox + name + version + lifecycle columns) → selected-
 * universe details (middle, description + author) → close button (bottom).
 *
 * <p>F.1 deliberately omits create/import/export/edit per design doc §0 — those land in later
 * F.x. F.1 ships read-only-except-for-activation.
 *
 * <p>Dispose: {@link #dispose()} unsubscribes from the broker. Forgetting causes a leak; the
 * menu controller wires {@code stage.setOnCloseRequest} to call it.
 */
@Slf4j
public class UniversesDialog extends BorderPane {

    private static final String DESCRIPTION_STEM = "Auto-seeded from existing catalog entries";

    private final UniverseDesignerService universeService;
    private final UniverseFilteringService filteringService;

    private final TableView<UniverseRow> table = new TableView<>();
    private final ObservableList<UniverseRow> rows = FXCollections.observableArrayList();

    // Details panel
    private final Label detailName = new Label("(select a universe)");
    private final Label detailVersion = new Label("—");
    private final Label detailLifecycle = new Label("—");
    private final Label detailAuthor = new Label("—");
    private final Label detailDescription = new Label("Select a universe to see its description.");

    /** Broker unsubscribe handle; null when not subscribed. */
    private Runnable filterChangeUnsubscribe;

    /** Suppresses event-driven reload when this dialog is itself driving the activation change. */
    private volatile boolean ignoreNextBrokerCallback = false;

    public UniversesDialog(UniverseDesignerService universeService,
                           UniverseFilteringService filteringService) {
        this.universeService = universeService;
        this.filteringService = filteringService;

        setPadding(new Insets(10));
        setTop(buildHeader());
        setCenter(buildCenter());
        setBottom(buildButtonBar());

        reload();

        // Subscribe so external activation changes (e.g. another instance of this dialog,
        // future scripted activation) refresh the table.
        if (filteringService != null) {
            this.filterChangeUnsubscribe = filteringService.subscribeToFilterChanges(this::handleBrokerCallback);
        }
    }

    /**
     * Dispose hook called by the menu controller's {@code stage.setOnCloseRequest}. Unsubscribes
     * from the broker; forgetting would leak this dialog through the subscriber list.
     */
    public void dispose() {
        if (filterChangeUnsubscribe != null) {
            filterChangeUnsubscribe.run();
            filterChangeUnsubscribe = null;
        }
    }

    // ============================================================
    // Layout
    // ============================================================

    private VBox buildHeader() {
        Label title = new Label("Universes");
        title.setFont(Font.font(title.getFont().getFamily(), FontWeight.BOLD, 16));
        Label subtitle = new Label("Toggle the checkbox to activate or deactivate a universe. "
                + "Real-data catalog entries remain visible regardless of activation state.");
        subtitle.setStyle("-fx-text-fill: gray;");
        subtitle.setWrapText(true);
        VBox box = new VBox(4, title, subtitle);
        box.setPadding(new Insets(0, 0, 8, 0));
        return box;
    }

    private VBox buildCenter() {
        buildTable();
        TitledPane detailsPane = buildDetailsPane();
        VBox box = new VBox(8, table, detailsPane);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private void buildTable() {
        TableColumn<UniverseRow, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(cd -> cd.getValue().active);
        activeCol.setCellFactory(col -> new CheckBoxToggleCell());
        activeCol.setPrefWidth(70);
        activeCol.setSortable(false);

        TableColumn<UniverseRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cd -> cd.getValue().name);
        nameCol.setPrefWidth(240);

        TableColumn<UniverseRow, String> versionCol = new TableColumn<>("Version");
        versionCol.setCellValueFactory(cd -> cd.getValue().version);
        versionCol.setPrefWidth(80);

        TableColumn<UniverseRow, String> lifecycleCol = new TableColumn<>("Lifecycle");
        lifecycleCol.setCellValueFactory(cd -> cd.getValue().lifecycle);
        lifecycleCol.setPrefWidth(110);

        table.getColumns().setAll(activeCol, nameCol, versionCol, lifecycleCol);
        table.setItems(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, prev, sel) -> renderDetails(sel));
    }

    private TitledPane buildDetailsPane() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(6);
        grid.setPadding(new Insets(8));

        addDetailRow(grid, 0, "Name:", detailName);
        addDetailRow(grid, 1, "Version:", detailVersion);
        addDetailRow(grid, 2, "Lifecycle:", detailLifecycle);
        addDetailRow(grid, 3, "Author:", detailAuthor);
        addDetailRow(grid, 4, "Description:", detailDescription);
        detailDescription.setWrapText(true);
        detailDescription.setMaxWidth(440);

        TitledPane pane = new TitledPane("Selected Universe", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private void addDetailRow(GridPane grid, int row, String label, Label value) {
        Label key = new Label(label);
        key.setFont(Font.font("System", FontWeight.BOLD, 12));
        grid.add(key, 0, row);
        grid.add(value, 1, row);
    }

    private HBox buildButtonBar() {
        Button closeButton = new Button("Close");
        closeButton.setDefaultButton(true);
        closeButton.setOnAction(e -> {
            // Find the hosting Stage via the scene's window and close it. The menu controller's
            // stage.setOnCloseRequest handler will call dispose() to unsubscribe.
            if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
                stage.close();
            }
        });
        HBox bar = new HBox(closeButton);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(10, 0, 0, 0));
        return bar;
    }

    // ============================================================
    // Data
    // ============================================================

    private void reload() {
        List<Universe> universes = universeService.findAll();
        // Sort: active first, then alphabetical by name. Stable sort so the user can predict
        // where freshly-activated universes appear.
        List<UniverseRow> built = universes.stream()
                .sorted((a, b) -> {
                    int activeCmp = Boolean.compare(!a.active(), !b.active()); // active first
                    if (activeCmp != 0) return activeCmp;
                    return a.name().compareToIgnoreCase(b.name());
                })
                .map(UniverseRow::new)
                .toList();

        // Preserve selection across reload if possible.
        String previouslySelectedId = table.getSelectionModel().getSelectedItem() != null
                ? table.getSelectionModel().getSelectedItem().id
                : null;

        rows.setAll(built);

        if (previouslySelectedId != null) {
            built.stream()
                    .filter(r -> previouslySelectedId.equals(r.id))
                    .findFirst()
                    .ifPresent(r -> table.getSelectionModel().select(r));
        }
    }

    private void renderDetails(UniverseRow row) {
        if (row == null) {
            detailName.setText("(select a universe)");
            detailVersion.setText("—");
            detailLifecycle.setText("—");
            detailAuthor.setText("—");
            detailDescription.setText("Select a universe to see its description.");
            return;
        }
        detailName.setText(row.universe.name());
        detailVersion.setText(row.universe.version());
        detailLifecycle.setText(row.universe.lifecycle().name());
        detailAuthor.setText(row.universe.sourceAuthor().isBlank() ? "—" : row.universe.sourceAuthor());
        detailDescription.setText(row.universe.description().isBlank() ? "—" : row.universe.description());
    }

    /**
     * Called from the {@link UniverseFilteringService} broker when activation state changes
     * (in this dialog or elsewhere). Reloads the table to reflect the current state.
     *
     * <p>The {@link #ignoreNextBrokerCallback} flag short-circuits the self-induced reload that
     * would otherwise follow every checkbox toggle (since activate/deactivate publishes an event
     * the broker delivers back to this dialog). The flag is set just before the service call
     * and cleared at the end of this callback; one event in, one consumed.
     */
    private void handleBrokerCallback() {
        if (ignoreNextBrokerCallback) {
            ignoreNextBrokerCallback = false;
            return;
        }
        reload();
    }

    /**
     * Toggle the active state of a universe by id. Called from the checkbox cell's listener.
     * Sets {@link #ignoreNextBrokerCallback} before invoking the service to suppress the
     * self-induced reload — the checkbox already reflects the new state.
     */
    private void toggleActivation(String universeId, boolean newActive) {
        ignoreNextBrokerCallback = true;
        try {
            if (newActive) {
                universeService.activate(universeId);
            } else {
                universeService.deactivate(universeId);
            }
        } catch (Exception ex) {
            log.error("Failed to toggle universe '{}' active state to {}", universeId, newActive, ex);
            ignoreNextBrokerCallback = false; // ensure the next genuine event still fires reload
            // Roll back the checkbox UI on failure so the user sees the actual state.
            reload();
        }
    }

    // ============================================================
    // Row model + cell
    // ============================================================

    /** Display-model for one row in the universe table. */
    static class UniverseRow {
        final String id;
        final Universe universe;
        final SimpleStringProperty name;
        final SimpleStringProperty version;
        final SimpleStringProperty lifecycle;
        final SimpleBooleanProperty active;

        UniverseRow(Universe u) {
            this.id = u.id();
            this.universe = u;
            this.name = new SimpleStringProperty(u.name());
            this.version = new SimpleStringProperty(u.version());
            this.lifecycle = new SimpleStringProperty(u.lifecycle().name());
            this.active = new SimpleBooleanProperty(u.active());
        }
    }

    /**
     * Checkbox cell that toggles the universe's active state via
     * {@link #toggleActivation(String, boolean)}. Bound to the row's {@code active} property so
     * external reloads update the checkbox visually.
     */
    private class CheckBoxToggleCell extends TableCell<UniverseRow, Boolean> {
        private final CheckBox checkBox = new CheckBox();

        CheckBoxToggleCell() {
            setAlignment(Pos.CENTER);
            checkBox.setOnAction(e -> {
                UniverseRow row = getTableRow().getItem();
                if (row != null) {
                    toggleActivation(row.id, checkBox.isSelected());
                }
            });
        }

        @Override
        protected void updateItem(Boolean active, boolean empty) {
            super.updateItem(active, empty);
            if (empty || active == null) {
                setGraphic(null);
            } else {
                checkBox.setSelected(active);
                setGraphic(checkBox);
            }
        }
    }

    // ============================================================
    // Test seams (package-private)
    // ============================================================

    /** Visible for {@link UniversesDialogTest}. Returns the current row list. */
    ObservableList<UniverseRow> rowsForTest() {
        return rows;
    }

    /** Visible for {@link UniversesDialogTest}. Drives the toggle path without UI events. */
    void toggleActivationForTest(String universeId, boolean newActive) {
        toggleActivation(universeId, newActive);
    }
}
