package com.teamgannon.trips.worldbuilding;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.terranrepublic.assets.Alias;
import com.terranrepublic.assets.AliasTargetKind;
import com.terranrepublic.assets.Universe;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Worldbuilding > Aliases dialog — the user-visible surface for managing alias entries.
 *
 * <p>Phase F.2 §6.3 — third worldbuilding-platform dialog after Universes (F.1 Step 7) and
 * the Installations Designer. Modeless BorderPane hosted by
 * {@code WorldbuildingMenuController.openAliasesDialog}; mirrors UniversesDialog's lifecycle
 * shape (broker subscription + {@link #ignoreNextBrokerCallback} self-induced suppression +
 * {@link #dispose()} unsubscribe).
 *
 * <p>Layout:
 * <ul>
 *   <li><b>Top</b> — filter row: Universe ComboBox (lists all universes, default "All active");
 *       Target kind ComboBox (All / Star / Exoplanet)</li>
 *   <li><b>Center</b> — TableView of aliases with columns Universe / Kind / Target name /
 *       Alias / Description</li>
 *   <li><b>Bottom</b> — Create..., Edit..., Delete..., Close buttons</li>
 * </ul>
 *
 * <p>F.2 ships full CRUD: Create + Edit + Delete (unlike F.1's read-only-except-for-activation
 * Universes dialog). Save flows through {@link AliasDesignerService#save}, surfacing the
 * two-layer uniqueness pattern's friendly {@link IllegalStateException} message on duplicate
 * collisions. Delete asks for confirmation with the alias text shown.
 *
 * <p>Target name resolution: aliases store {@code (kind, targetId)} — the table needs the
 * resolved display name. The dialog injects {@link StarObjectRepository} +
 * {@link ExoPlanetRepository} and resolves per-row inline (small alias counts in practice;
 * a bulk-resolver helper isn't worth the indirection at F.2 scale).
 */
@Slf4j
public class AliasesDialog extends BorderPane {

    private static final String ALL_UNIVERSES = "(All active universes)";
    private static final String ALL_KINDS = "All";

    private final AliasDesignerService aliasService;
    private final UniverseDesignerService universeService;
    private final UniverseFilteringService filteringService;
    private final StarObjectRepository starRepository;
    private final ExoPlanetRepository exoPlanetRepository;

    private final ComboBox<String> universeFilter = new ComboBox<>();
    private final ComboBox<String> kindFilter = new ComboBox<>();
    private final TableView<AliasRow> table = new TableView<>();
    private final ObservableList<AliasRow> rows = FXCollections.observableArrayList();

    private final Button createButton = new Button("Create...");
    private final Button editButton = new Button("Edit...");
    private final Button deleteButton = new Button("Delete...");
    private final Button closeButton = new Button("Close");

    /** id → universe name cache so filter dropdown + row rendering share lookups. */
    private final Map<String, String> universeNamesById = new HashMap<>();

    @Nullable
    private Runnable filterChangeUnsubscribe;

    /** Suppresses event-driven reload during self-induced changes (save/delete from this dialog). */
    private volatile boolean ignoreNextBrokerCallback = false;

    public AliasesDialog(AliasDesignerService aliasService,
                         UniverseDesignerService universeService,
                         UniverseFilteringService filteringService,
                         StarObjectRepository starRepository,
                         ExoPlanetRepository exoPlanetRepository) {
        this.aliasService = aliasService;
        this.universeService = universeService;
        this.filteringService = filteringService;
        this.starRepository = starRepository;
        this.exoPlanetRepository = exoPlanetRepository;

        setPadding(new Insets(10));
        setTop(buildHeader());
        setCenter(buildCenter());
        setBottom(buildButtonBar());

        reload();

        if (filteringService != null) {
            this.filterChangeUnsubscribe =
                    filteringService.subscribeToFilterChanges(this::handleBrokerCallback);
        }
    }

    /**
     * Dispose hook called by the menu controller's {@code stage.setOnCloseRequest}. Unsubscribes
     * from the broker. Forgetting leaks this dialog through the subscriber list.
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
        Label title = new Label("Aliases");
        title.setFont(Font.font(title.getFont().getFamily(), FontWeight.BOLD, 16));
        Label subtitle = new Label("Manage fictional names (aliases) attached to real stars and "
                + "exoplanets within worldbuilding universes.");
        subtitle.setStyle("-fx-text-fill: gray;");
        subtitle.setWrapText(true);

        HBox filters = new HBox(8);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(4, 0, 0, 0));

        Label universeFilterLabel = new Label("Universe:");
        universeFilter.setPrefWidth(220);
        universeFilter.valueProperty().addListener((obs, old, sel) -> applyFilters());

        Label kindFilterLabel = new Label("Target kind:");
        kindFilter.getItems().setAll(ALL_KINDS, "Star", "Exoplanet");
        kindFilter.setValue(ALL_KINDS);
        kindFilter.valueProperty().addListener((obs, old, sel) -> applyFilters());

        filters.getChildren().addAll(universeFilterLabel, universeFilter, kindFilterLabel, kindFilter);

        VBox box = new VBox(4, title, subtitle, filters);
        box.setPadding(new Insets(0, 0, 8, 0));
        return box;
    }

    private VBox buildCenter() {
        buildTable();
        VBox box = new VBox(8, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private void buildTable() {
        TableColumn<AliasRow, String> universeCol = new TableColumn<>("Universe");
        universeCol.setCellValueFactory(cd -> cd.getValue().universeName);
        universeCol.setPrefWidth(160);

        TableColumn<AliasRow, String> kindCol = new TableColumn<>("Kind");
        kindCol.setCellValueFactory(cd -> cd.getValue().kindLabel);
        kindCol.setPrefWidth(90);

        TableColumn<AliasRow, String> targetCol = new TableColumn<>("Target");
        targetCol.setCellValueFactory(cd -> cd.getValue().targetName);
        targetCol.setPrefWidth(180);

        TableColumn<AliasRow, String> aliasCol = new TableColumn<>("Alias");
        aliasCol.setCellValueFactory(cd -> cd.getValue().aliasText);
        aliasCol.setPrefWidth(180);

        TableColumn<AliasRow, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(cd -> cd.getValue().descriptionExcerpt);
        descriptionCol.setPrefWidth(240);

        table.getColumns().setAll(universeCol, kindCol, targetCol, aliasCol, descriptionCol);
        table.setItems(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().selectedItemProperty().addListener((obs, prev, sel) ->
                updateButtonStates(sel != null));
    }

    private HBox buildButtonBar() {
        createButton.setOnAction(e -> openCreateDialog());
        editButton.setOnAction(e -> openEditDialog());
        deleteButton.setOnAction(e -> confirmAndDelete());
        closeButton.setDefaultButton(true);
        closeButton.setOnAction(e -> {
            if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
                stage.close();
            }
        });
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        HBox bar = new HBox(8, createButton, editButton, deleteButton, closeButton);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(10, 0, 0, 0));
        return bar;
    }

    private void updateButtonStates(boolean rowSelected) {
        editButton.setDisable(!rowSelected);
        deleteButton.setDisable(!rowSelected);
    }

    // ============================================================
    // Data
    // ============================================================

    private void reload() {
        // Repopulate universe-name cache from all universes (not just active) — the filter
        // dropdown shows them all so users can browse aliases for inactive universes too.
        universeNamesById.clear();
        List<Universe> universes = universeService.findAll();
        for (Universe u : universes) {
            universeNamesById.put(u.id(), u.name());
        }
        // Preserve current universe-filter selection if still valid.
        String previousUniverseFilter = universeFilter.getValue();
        universeFilter.getItems().setAll(ALL_UNIVERSES);
        universes.stream()
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .forEach(u -> universeFilter.getItems().add(u.name()));
        if (previousUniverseFilter != null && universeFilter.getItems().contains(previousUniverseFilter)) {
            universeFilter.setValue(previousUniverseFilter);
        } else {
            universeFilter.setValue(ALL_UNIVERSES);
        }
        applyFilters();
    }

    /** Rebuilds the table content per the current filter selections. */
    private void applyFilters() {
        List<Alias> all = aliasService.findAll();
        String selectedUniverseName = universeFilter.getValue();
        String selectedKind = kindFilter.getValue();

        List<AliasRow> built = all.stream()
                .filter(a -> matchesUniverseFilter(a, selectedUniverseName))
                .filter(a -> matchesKindFilter(a, selectedKind))
                .map(this::toRow)
                .sorted((a, b) -> {
                    int u = a.universeName.get().compareToIgnoreCase(b.universeName.get());
                    if (u != 0) return u;
                    return a.aliasText.get().compareToIgnoreCase(b.aliasText.get());
                })
                .toList();
        rows.setAll(built);
    }

    private boolean matchesUniverseFilter(Alias alias, String selected) {
        if (selected == null || ALL_UNIVERSES.equals(selected)) {
            // "All active universes" → only show aliases whose universe is currently active.
            return filteringService == null
                    || filteringService.getActiveUniverseIds().contains(alias.universeId());
        }
        // Specific universe name selected → exact match by name (via id→name cache).
        String aliasUniverseName = universeNamesById.getOrDefault(alias.universeId(), "");
        return selected.equals(aliasUniverseName);
    }

    private boolean matchesKindFilter(Alias alias, String selectedKind) {
        if (selectedKind == null || ALL_KINDS.equals(selectedKind)) {
            return true;
        }
        return ("Star".equals(selectedKind) && alias.targetKind() == AliasTargetKind.STAR)
                || ("Exoplanet".equals(selectedKind) && alias.targetKind() == AliasTargetKind.EXOPLANET);
    }

    private AliasRow toRow(Alias alias) {
        String universeName = universeNamesById.getOrDefault(alias.universeId(), "(unknown)");
        String kindLabel = alias.targetKind() == AliasTargetKind.STAR ? "Star" : "Exoplanet";
        String targetName = resolveTargetName(alias.targetKind(), alias.targetId());
        String descriptionExcerpt = alias.description() == null || alias.description().isEmpty()
                ? ""
                : (alias.description().length() > 80
                ? alias.description().substring(0, 77) + "..."
                : alias.description());
        return new AliasRow(alias, universeName, kindLabel, targetName, descriptionExcerpt);
    }

    private String resolveTargetName(AliasTargetKind kind, String targetId) {
        if (kind == AliasTargetKind.STAR) {
            return starRepository.findById(targetId)
                    .map(StarObject::getDisplayName)
                    .orElse("(missing star: " + targetId + ")");
        }
        return exoPlanetRepository.findById(targetId)
                .map(ExoPlanet::getName)
                .orElse("(missing exoplanet: " + targetId + ")");
    }

    // ============================================================
    // Create / Edit / Delete actions
    // ============================================================

    private void openCreateDialog() {
        AliasEditorDialog dialog = new AliasEditorDialog(
                aliasService, universeService, starRepository, exoPlanetRepository, null);
        Optional<Alias> result = dialog.showAndWait();
        result.ifPresent(saved -> {
            ignoreNextBrokerCallback = true;
            log.info("Created alias '{}' (universe={}, target={}/{}, id={})",
                    saved.aliasText(), saved.universeId(),
                    saved.targetKind(), saved.targetId(), saved.id());
            applyFilters();
        });
    }

    private void openEditDialog() {
        AliasRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        AliasEditorDialog dialog = new AliasEditorDialog(
                aliasService, universeService, starRepository, exoPlanetRepository, selected.alias);
        Optional<Alias> result = dialog.showAndWait();
        result.ifPresent(saved -> {
            ignoreNextBrokerCallback = true;
            log.info("Edited alias id={} text='{}'", saved.id(), saved.aliasText());
            applyFilters();
        });
    }

    private void confirmAndDelete() {
        AliasRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Alias");
        alert.setHeaderText("Delete this alias?");
        alert.setContentText(String.format(
                "Alias: %s%nUniverse: %s%nTarget: %s (%s)%n%nThis action cannot be undone.",
                selected.aliasText.get(), selected.universeName.get(),
                selected.targetName.get(), selected.kindLabel.get()));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ignoreNextBrokerCallback = true;
            try {
                aliasService.deleteById(selected.alias.id());
                applyFilters();
            } catch (Exception ex) {
                log.error("Failed to delete alias {}", selected.alias.id(), ex);
                showError("Delete failed: " + ex.getMessage());
                ignoreNextBrokerCallback = false;
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Alias error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ============================================================
    // Broker callback
    // ============================================================

    private void handleBrokerCallback() {
        if (ignoreNextBrokerCallback) {
            ignoreNextBrokerCallback = false;
            return;
        }
        // External universe activation change → recompute filters since "All active universes"
        // visibility may have shifted.
        applyFilters();
    }

    // ============================================================
    // Row model + test seams
    // ============================================================

    static class AliasRow {
        final Alias alias;
        final SimpleStringProperty universeName;
        final SimpleStringProperty kindLabel;
        final SimpleStringProperty targetName;
        final SimpleStringProperty aliasText;
        final SimpleStringProperty descriptionExcerpt;

        AliasRow(Alias alias, String universeName, String kindLabel,
                 String targetName, String descriptionExcerpt) {
            this.alias = alias;
            this.universeName = new SimpleStringProperty(universeName);
            this.kindLabel = new SimpleStringProperty(kindLabel);
            this.targetName = new SimpleStringProperty(targetName);
            this.aliasText = new SimpleStringProperty(alias.aliasText());
            this.descriptionExcerpt = new SimpleStringProperty(descriptionExcerpt);
        }
    }

    /** Visible for tests — current row list. */
    ObservableList<AliasRow> rowsForTest() {
        return rows;
    }

    /** Visible for tests — reload without UI events. */
    void reloadForTest() {
        reload();
    }

    /** Visible for tests — set the universe filter programmatically. */
    void setUniverseFilterForTest(String value) {
        universeFilter.setValue(value);
    }

    /** Visible for tests — set the kind filter programmatically. */
    void setKindFilterForTest(String value) {
        kindFilter.setValue(value);
    }
}
