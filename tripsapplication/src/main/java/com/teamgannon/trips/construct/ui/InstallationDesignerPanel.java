package com.teamgannon.trips.construct.ui;

import com.teamgannon.trips.construct.ConstructRegistry;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.spaceshipmodeller.service.MegastructureDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.StationDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.TransportNodeService;
import com.teamgannon.trips.spaceshipmodeller.service.WeaponInstallationDesignerService;
import com.terranrepublic.assets.AssetKind;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.InstallationType;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.MegastructureArchetype;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.StationType;
import com.terranrepublic.assets.WeaponInstallation;
import com.terranrepublic.infrastructure.InfrastructureKind;
import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.SpaceInfrastructure;
import com.terranrepublic.infrastructure.TransportNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.teamgannon.trips.construct.ui.ConstructLabels.get;
import static com.teamgannon.trips.construct.ui.ConstructLabels.format;

/**
 * Installations Designer panel — read-only browsing of catalogued stations, weapon installations,
 * and transport nodes.
 *
 * <p>This panel deliberately omits ships (handled by {@code SpaceshipDesignerPanel}) and conduits
 * (deferred per v2 §6.1 Q3). All reads flow through {@link ConstructRegistry}, which the panel
 * receives at construction time as its only Spring-managed dependency.
 *
 * <h2>FX-thread discipline (Issue 11)</h2>
 * <ul>
 *   <li>{@link #loadAsync()} fires a background {@link Task}. The Task's {@code call()} method
 *       is the only place that hits the registry.</li>
 *   <li>{@code setOnSucceeded} routes the result back to {@link #applyConstructs(List)} on the
 *       FX thread.</li>
 *   <li>{@link #applyConstructs(List)} is package-private so headless tests can drive it
 *       synchronously without spinning up the Task lifecycle. It guards with
 *       {@link FxThread#assertFxThread()} so accidental off-thread calls fail loudly.</li>
 *   <li>Filter and selection callbacks run on the FX thread already (JavaFX event dispatch); they
 *       operate against the already-loaded in-memory list, so no further Task is needed.</li>
 * </ul>
 *
 * <h2>Phase boundaries</h2>
 * <ul>
 *   <li>Phase C (this phase): read-only. Filter strip, table, details pane.</li>
 *   <li>Phase D will add Edit / New / Delete buttons and editor dialogs.</li>
 *   <li>Phase E will integrate transport-node entries with the route finder.</li>
 *   <li>Phase F will render installations and gates in the 3D interstellar view.</li>
 * </ul>
 */
@Slf4j
public class InstallationDesignerPanel extends BorderPane {

    private static final String ALL = get("filter.all");
    private static final String KIND_STATION = "STATION";
    private static final String KIND_WEAPON = "WEAPON_INSTALLATION";
    private static final String KIND_TRANSPORT = "TRANSPORT_NODE";
    private static final String KIND_MEGASTRUCTURE = "MEGASTRUCTURE";

    /** Tab-strip sentinel for the "All" toggle. */
    private static final String UNIVERSE_ALL = "__all__";
    /** Source string the universe tab strip pins as the second tab when present. Phase D.5 lands real space stations under this label. */
    private static final String UNIVERSE_REAL_PROPOSED = "Real / Proposed";

    private final ConstructRegistry registry;
    private final StationDesignerService stationService;
    private final WeaponInstallationDesignerService weaponService;
    private final TransportNodeService transportService;
    private final MegastructureDesignerService megastructureService;
    /**
     * v2 Phase F.1 §5.3 — the §5-invariants chokepoint. Applied to {@link #loadFromRegistry()}'s
     * output before {@link #applyConstructs(java.util.List)} sees the data. Nullable for
     * backward-compat with the 5-arg pre-F.1 constructor.
     */
    private final com.teamgannon.trips.worldbuilding.UniverseFilteringService filteringService;
    /** Unsubscribe handle returned by the filter broker; called on dispose. */
    private Runnable filterChangeUnsubscribe;

    // Master list — populated by loadAsync / applyConstructs, never mutated after.
    private List<ConstructRow> allRows = List.of();
    private final ObservableList<ConstructRow> visibleRows = FXCollections.observableArrayList();

    // Filter controls
    private final ComboBox<String> kindFilter = new ComboBox<>();
    private final ComboBox<String> functionFilter = new ComboBox<>();
    private final ComboBox<String> subtypeFilter = new ComboBox<>();
    private final ComboBox<String> factionFilter = new ComboBox<>();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final Button refreshButton = new Button(get("button.refresh"));

    // CRUD buttons (Phase D)
    private final Button newButton = new Button(get("button.new"));
    private final Button editButton = new Button(get("button.edit"));
    private final Button deleteButton = new Button(get("button.delete"));

    // Universe tab strip (Phase D.5)
    private final FlowPane universeTabBar = new FlowPane(4, 4);
    private final ToggleGroup universeToggleGroup = new ToggleGroup();
    /** Currently-selected universe key — either {@link #UNIVERSE_ALL} or a real source value. */
    private String selectedUniverse = UNIVERSE_ALL;

    // Table + details
    private final TableView<ConstructRow> table = new TableView<>(visibleRows);
    private final VBox detailsContent = new VBox(6);
    private final Label statusLabel = new Label();

    /**
     * Backwards-compatible 5-arg constructor (no filtering service). Existing call sites + tests
     * keep compiling; this panel renders the raw catalog without universe-scope filtering. The
     * 6-arg constructor below is the canonical F.1 form.
     */
    public InstallationDesignerPanel(ConstructRegistry registry,
                                     StationDesignerService stationService,
                                     WeaponInstallationDesignerService weaponService,
                                     TransportNodeService transportService,
                                     MegastructureDesignerService megastructureService) {
        this(registry, stationService, weaponService, transportService, megastructureService, null);
    }

    /**
     * v2 Phase F.1 §5.3 canonical constructor. The {@code filteringService} enforces the §5
     * invariants on every catalog read + drives live refresh on universe activation changes.
     *
     * @param filteringService nullable for backward-compat; F.1 production wiring always provides
     */
    public InstallationDesignerPanel(ConstructRegistry registry,
                                     StationDesignerService stationService,
                                     WeaponInstallationDesignerService weaponService,
                                     TransportNodeService transportService,
                                     MegastructureDesignerService megastructureService,
                                     com.teamgannon.trips.worldbuilding.UniverseFilteringService filteringService) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.stationService = Objects.requireNonNull(stationService, "stationService");
        this.weaponService = Objects.requireNonNull(weaponService, "weaponService");
        this.transportService = Objects.requireNonNull(transportService, "transportService");
        this.megastructureService = Objects.requireNonNull(megastructureService, "megastructureService");
        this.filteringService = filteringService;
        setPadding(new Insets(10));
        setTop(buildHeader());
        setCenter(buildCenter());
        setBottom(buildStatusBar());
        // Empty initial details panel.
        renderDetailsForSelection(null);
        if (filteringService != null) {
            this.filterChangeUnsubscribe = filteringService.subscribeToFilterChanges(this::loadAsync);
        }
    }

    /**
     * Dispose hook for the universe-activation subscription. Called by the menu controller's
     * {@code stage.setOnCloseRequest} handler when the user closes the Installations Designer.
     */
    public void dispose() {
        if (filterChangeUnsubscribe != null) {
            filterChangeUnsubscribe.run();
            filterChangeUnsubscribe = null;
        }
    }

    // ----------------------------------------------------------------- header

    private VBox buildHeader() {
        Label title = new Label(get("panel.title"));
        title.setFont(Font.font(title.getFont().getFamily(), 18));
        title.getStyleClass().add("trips-bold");
        Label subtitle = new Label(get("panel.subtitle"));
        subtitle.getStyleClass().add("trips-text-muted");

        // Kind filter — drives which subtype values are valid.
        kindFilter.getItems().setAll(ALL,
                kindLabel(KIND_STATION),
                kindLabel(KIND_WEAPON),
                kindLabel(KIND_TRANSPORT),
                kindLabel(KIND_MEGASTRUCTURE));
        kindFilter.setValue(ALL);
        kindFilter.setTooltip(new Tooltip(get("filter.kind.tooltip")));
        kindFilter.setAccessibleText(get("filter.kind"));
        kindFilter.setAccessibleHelp(get("filter.kind.tooltip"));
        kindFilter.valueProperty().addListener((o, a, b) -> {
            rebuildSubtypeFilter();
            applyFilters();
        });

        // v2 Phase D.6 Step 7 — Function filter sits between Kind and Subtype on the strip.
        // Items are "All" + every StationFunction enum constant in declaration order.
        functionFilter.getItems().setAll(ALL);
        for (StationFunction f : StationFunction.values()) {
            functionFilter.getItems().add(f.name());
        }
        functionFilter.setValue(ALL);
        functionFilter.setTooltip(new Tooltip(get("filter.function.tooltip")));
        functionFilter.setAccessibleText(get("filter.function"));
        functionFilter.setAccessibleHelp(get("filter.function.tooltip"));
        functionFilter.valueProperty().addListener((o, a, b) -> applyFilters());

        subtypeFilter.getItems().setAll(ALL);
        subtypeFilter.setValue(ALL);
        subtypeFilter.setDisable(true);
        subtypeFilter.setTooltip(new Tooltip(get("filter.subtype.tooltip")));
        subtypeFilter.setAccessibleText(get("filter.subtype"));
        subtypeFilter.setAccessibleHelp(get("filter.subtype.tooltip"));
        subtypeFilter.valueProperty().addListener((o, a, b) -> applyFilters());

        factionFilter.getItems().setAll(ALL);
        factionFilter.setValue(ALL);
        factionFilter.setTooltip(new Tooltip(get("filter.faction.tooltip")));
        factionFilter.setAccessibleText(get("filter.faction"));
        factionFilter.setAccessibleHelp(get("filter.faction.tooltip"));
        factionFilter.valueProperty().addListener((o, a, b) -> applyFilters());

        categoryFilter.getItems().setAll(ALL);
        categoryFilter.setValue(ALL);
        categoryFilter.setTooltip(new Tooltip(get("filter.category.tooltip")));
        categoryFilter.setAccessibleText(get("filter.category"));
        categoryFilter.setAccessibleHelp(get("filter.category.tooltip"));
        categoryFilter.valueProperty().addListener((o, a, b) -> applyFilters());

        searchField.setPromptText(get("filter.search.prompt"));
        searchField.setTooltip(new Tooltip(get("filter.search.tooltip")));
        searchField.setAccessibleText(get("filter.search.prompt"));
        searchField.setAccessibleHelp(get("filter.search.tooltip"));
        searchField.textProperty().addListener((o, a, b) -> applyFilters());

        refreshButton.setTooltip(new Tooltip(get("tooltip.refresh")));
        refreshButton.setAccessibleText(get("button.refresh"));
        refreshButton.setAccessibleHelp(get("tooltip.refresh"));
        refreshButton.setOnAction(e -> loadAsync());

        HBox filterRow = new HBox(8,
                new Label(get("filter.kind")), kindFilter,
                new Label(get("filter.function")), functionFilter,
                new Label(get("filter.subtype")), subtypeFilter,
                new Label(get("filter.faction")), factionFilter,
                new Label(get("filter.category")), categoryFilter,
                searchField, refreshButton);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.setPadding(new Insets(8, 0, 8, 0));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        newButton.setTooltip(new Tooltip(get("tooltip.new")));
        newButton.setAccessibleText(get("button.new"));
        newButton.setAccessibleHelp(get("tooltip.new"));
        newButton.setOnAction(e -> onNew());

        editButton.setTooltip(new Tooltip(get("tooltip.edit")));
        editButton.setAccessibleText(get("button.edit"));
        editButton.setAccessibleHelp(get("tooltip.edit"));
        editButton.setDisable(true);
        editButton.setOnAction(e -> onEdit());

        deleteButton.setTooltip(new Tooltip(get("tooltip.delete")));
        deleteButton.setAccessibleText(get("button.delete"));
        deleteButton.setAccessibleHelp(get("tooltip.delete"));
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> onDelete());

        HBox actionRow = new HBox(8, newButton, editButton, deleteButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(0, 0, 8, 0));

        configureUniverseTabBar();

        return new VBox(2, title, subtitle, universeTabBar, filterRow, actionRow);
    }

    private void configureUniverseTabBar() {
        universeTabBar.setPadding(new Insets(4, 0, 4, 0));
        // Sticky single-select: clicking the active toggle won't deselect it (always exactly
        // one universe filter in effect). Mirrors the SpaceshipDesignerPanel behaviour.
        universeToggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null && oldT != null) {
                Platform.runLater(() -> universeToggleGroup.selectToggle(oldT));
                return;
            }
            if (newT != null && newT.getUserData() != null) {
                selectedUniverse = (String) newT.getUserData();
                applyFilters();
            }
        });
        // Seed with a lone "All" toggle until applyConstructs populates real ones.
        ToggleButton all = makeUniverseToggle(get("tab.all"), UNIVERSE_ALL);
        all.setSelected(true);
        universeTabBar.getChildren().setAll(all);
    }

    /**
     * Rebuild the tab strip from the distinct {@code source} values across all loaded rows.
     * <p>
     * Order: "All" → "Real / Proposed" (pinned second when present) → other sources alphabetical.
     * Preserves the previously-selected toggle when possible; falls back to "All" if its universe
     * disappears.
     */
    private void rebuildUniverseTabs() {
        FxThread.assertFxThread();
        String previous = selectedUniverse;

        List<String> universes = allRows.stream()
                .map(ConstructRow::getSource)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();

        List<ToggleButton> toggles = new ArrayList<>();
        toggles.add(makeUniverseToggle(get("tab.all"), UNIVERSE_ALL));
        if (universes.contains(UNIVERSE_REAL_PROPOSED)) {
            toggles.add(makeUniverseToggle(UNIVERSE_REAL_PROPOSED, UNIVERSE_REAL_PROPOSED));
        }
        for (String u : universes) {
            if (UNIVERSE_REAL_PROPOSED.equals(u)) {
                continue;
            }
            toggles.add(makeUniverseToggle(u, u));
        }
        universeTabBar.getChildren().setAll(toggles);

        ToggleButton toSelect = toggles.stream()
                .filter(t -> previous.equals(t.getUserData()))
                .findFirst()
                .orElse(toggles.get(0));
        toSelect.setSelected(true);
        selectedUniverse = (String) toSelect.getUserData();
    }

    private ToggleButton makeUniverseToggle(String label, String universeKey) {
        ToggleButton tb = new ToggleButton(label);
        tb.setUserData(universeKey);
        tb.setToggleGroup(universeToggleGroup);
        tb.setFocusTraversable(false);
        tb.setTooltip(new Tooltip(get("tab.tooltip")));
        tb.setAccessibleText(label);
        tb.setAccessibleHelp(get("tab.tooltip"));
        return tb;
    }

    private Label buildStatusBar() {
        statusLabel.getStyleClass().add("trips-text-muted-sm");
        statusLabel.setPadding(new Insets(4, 0, 0, 0));
        statusLabel.setText(format("status.loaded", 0));
        return statusLabel;
    }

    private static String kindLabel(String kindKey) {
        return get("kind." + kindKey, humanise(kindKey));
    }

    private static String humanise(String enumName) {
        if (enumName == null || enumName.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(enumName.length());
        boolean nextUpper = true;
        for (char c : enumName.toCharArray()) {
            if (c == '_') {
                sb.append(' ');
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(c);
                nextUpper = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * Pick the kind discriminator string that matches the {@link #kindFilter} label.
     */
    private String selectedKind() {
        String label = kindFilter.getValue();
        if (ALL.equals(label)) {
            return ALL;
        }
        if (label.equals(kindLabel(KIND_STATION))) {
            return KIND_STATION;
        }
        if (label.equals(kindLabel(KIND_WEAPON))) {
            return KIND_WEAPON;
        }
        if (label.equals(kindLabel(KIND_TRANSPORT))) {
            return KIND_TRANSPORT;
        }
        if (label.equals(kindLabel(KIND_MEGASTRUCTURE))) {
            return KIND_MEGASTRUCTURE;
        }
        return ALL;
    }

    private void rebuildSubtypeFilter() {
        FxThread.assertFxThread();
        String kind = selectedKind();
        subtypeFilter.getItems().setAll(ALL);
        switch (kind) {
            case KIND_STATION -> {
                Arrays.stream(StationType.values()).map(Enum::name).forEach(subtypeFilter.getItems()::add);
                subtypeFilter.setDisable(false);
            }
            case KIND_WEAPON -> {
                Arrays.stream(InstallationType.values()).map(Enum::name).forEach(subtypeFilter.getItems()::add);
                subtypeFilter.setDisable(false);
            }
            case KIND_TRANSPORT -> {
                Arrays.stream(NodeType.values()).map(Enum::name).forEach(subtypeFilter.getItems()::add);
                subtypeFilter.setDisable(false);
            }
            case KIND_MEGASTRUCTURE -> {
                Arrays.stream(MegastructureArchetype.values()).map(Enum::name)
                        .forEach(subtypeFilter.getItems()::add);
                subtypeFilter.setDisable(false);
            }
            default -> subtypeFilter.setDisable(true);
        }
        subtypeFilter.setValue(ALL);
    }

    // ----------------------------------------------------------------- center

    private SplitPane buildCenter() {
        configureTable();

        VBox left = new VBox(4, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox right = new VBox(6, sectionHeader(get("details.title")), detailsContent);
        right.setPadding(new Insets(0, 4, 4, 12));

        SplitPane split = new SplitPane(left, right);
        split.setDividerPositions(0.62);
        return split;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setPlaceholder(new Label(get("table.placeholder")));
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<ConstructRow, String> nameCol = col(get("column.name"), "name", 150);
        TableColumn<ConstructRow, String> desigCol = col(get("column.designation"), "designation", 110);
        TableColumn<ConstructRow, String> kindCol = col(get("column.kind"), "kind", 140);
        TableColumn<ConstructRow, String> subtypeCol = col(get("column.subtype"), "subtype", 150);
        TableColumn<ConstructRow, String> sourceCol = col(get("column.source"), "source", 150);
        TableColumn<ConstructRow, String> factionCol = col(get("column.faction"), "faction", 120);
        TableColumn<ConstructRow, Boolean> concealedCol = col(get("column.concealed"), "concealed", 90);
        TableColumn<ConstructRow, String> stateCol = col(get("column.operationalState"), "operationalState", 130);

        table.getColumns().setAll(nameCol, desigCol, kindCol, subtypeCol, sourceCol, factionCol,
                concealedCol, stateCol);
        // Issue 34: last column flexes to absorb leftover width.
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        table.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            renderDetailsForSelection(b == null ? null : b.getConstruct());
            boolean hasSelection = b != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
    }

    // ----------------------------------------------------------------- CRUD handlers

    /** Subtype picker key — kept in sync with {@code construct.properties} {@code kind.*} entries. */
    private static final String NEW_STATION = "STATION";
    private static final String NEW_WEAPON = "WEAPON_INSTALLATION";
    private static final String NEW_TRANSPORT = "TRANSPORT_NODE";
    private static final String NEW_MEGASTRUCTURE = "MEGASTRUCTURE";

    private void onNew() {
        FxThread.assertFxThread();
        // ChoiceDialog over the three subtype labels; the labels come from the bundle so they
        // line up with the Kind column.
        String stationLabel = get("kind." + NEW_STATION, "Station");
        String weaponLabel = get("kind." + NEW_WEAPON, "Weapon Installation");
        String transportLabel = get("kind." + NEW_TRANSPORT, "Transport Node");
        String megastructureLabel = get("kind." + NEW_MEGASTRUCTURE, "Megastructure");
        ChoiceDialog<String> picker = new ChoiceDialog<>(stationLabel,
                List.of(stationLabel, weaponLabel, transportLabel, megastructureLabel));
        picker.setTitle(get("new.picker.title"));
        picker.setHeaderText(get("new.picker.header"));
        picker.setContentText(get("new.picker.content"));
        Optional<String> chosen = picker.showAndWait();
        chosen.ifPresent(label -> {
            if (label.equals(stationLabel)) {
                openStationEditor(null);
            } else if (label.equals(weaponLabel)) {
                openWeaponEditor(null);
            } else if (label.equals(transportLabel)) {
                openTransportEditor(null);
            } else if (label.equals(megastructureLabel)) {
                openMegastructureEditor(null);
            }
        });
    }

    private void onEdit() {
        FxThread.assertFxThread();
        ConstructRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Cataloged construct = selected.getConstruct();
        switch (construct) {
            case StationDesign s -> openStationEditor(s);
            case WeaponInstallation w -> openWeaponEditor(w);
            case TransportNode t -> openTransportEditor(t);
            case Megastructure m -> openMegastructureEditor(m);
            default -> {
                // ships + conduits aren't surfaced here, so the default branch is defensive only.
            }
        }
    }

    private void onDelete() {
        FxThread.assertFxThread();
        ConstructRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(get("delete.confirm.title"));
        confirm.setHeaderText(get("delete.confirm.header"));
        confirm.setContentText(format("delete.confirm.content", selected.getName()));
        Optional<ButtonType> response = confirm.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) {
            return;
        }
        try {
            Cataloged construct = selected.getConstruct();
            switch (construct) {
                case StationDesign s -> stationService.deleteById(s.id());
                case WeaponInstallation w -> weaponService.deleteById(w.id());
                case TransportNode t -> transportService.deleteById(t.id());
                case Megastructure m -> megastructureService.deleteById(m.id());
                default -> { /* defensive */ }
            }
            statusLabel.setText(get("status.deleted"));
            loadAsync();
        } catch (Exception ex) {
            log.error("Failed to delete construct '{}'", selected.getName(), ex);
            statusLabel.setText(get("status.deleteFailed"));
        }
    }

    private void openStationEditor(StationDesign existing) {
        StationEditorDialog dialog = existing == null
                ? new StationEditorDialog()
                : new StationEditorDialog(existing);
        dialog.showAndWait().ifPresent(saved -> persistStation(saved));
    }

    private void openWeaponEditor(WeaponInstallation existing) {
        WeaponInstallationEditorDialog dialog = existing == null
                ? new WeaponInstallationEditorDialog()
                : new WeaponInstallationEditorDialog(existing);
        dialog.showAndWait().ifPresent(saved -> persistWeapon(saved));
    }

    private void openTransportEditor(TransportNode existing) {
        TransportNodeEditorDialog dialog = existing == null
                ? new TransportNodeEditorDialog()
                : new TransportNodeEditorDialog(existing);
        dialog.showAndWait().ifPresent(saved -> persistTransport(saved));
    }

    private void openMegastructureEditor(Megastructure existing) {
        MegastructureEditorDialog dialog = existing == null
                ? new MegastructureEditorDialog()
                : new MegastructureEditorDialog(existing);
        dialog.showAndWait().ifPresent(saved -> persistMegastructure(saved));
    }

    private void persistStation(StationDesign draft) {
        persist(() -> stationService.save(draft));
    }

    private void persistWeapon(WeaponInstallation draft) {
        persist(() -> weaponService.save(draft));
    }

    private void persistTransport(TransportNode draft) {
        persist(() -> transportService.save(draft));
    }

    private void persistMegastructure(Megastructure draft) {
        persist(() -> megastructureService.save(draft));
    }

    /**
     * Run a save off the FX thread; refresh the panel on success. Errors are logged and surfaced
     * through the status bar. The single-method shape lets each subtype's save share the same
     * Task lifecycle.
     */
    private void persist(Runnable saveCall) {
        FxThread.assertFxThread();
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                saveCall.run();
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            statusLabel.setText(get("status.saved"));
            loadAsync();
        });
        task.setOnFailed(e -> {
            log.error("Failed to save construct", task.getException());
            statusLabel.setText(get("status.saveFailed"));
        });
        Thread t = new Thread(task, "InstallationDesignerPanel-save");
        t.setDaemon(true);
        t.start();
    }

    private static <S, T> TableColumn<S, T> col(String header, String property, int prefWidth) {
        TableColumn<S, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(prefWidth);
        return c;
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(l.getFont().getFamily(), 14));
        l.getStyleClass().add("trips-bold");
        return l;
    }

    // ----------------------------------------------------------------- data

    /**
     * Fire a background task that reads from the registry and applies the result on the FX
     * thread. Safe to call from the FX thread; safe to call repeatedly.
     */
    public void loadAsync() {
        FxThread.assertFxThread();
        statusLabel.setText(get("status.loading"));
        Task<List<Cataloged>> task = new Task<>() {
            @Override
            protected List<Cataloged> call() {
                return loadFromRegistry();
            }
        };
        task.setOnSucceeded(e -> applyConstructs(task.getValue()));
        task.setOnFailed(e -> {
            log.error("Failed to load constructs", task.getException());
            statusLabel.setText(get("status.loadFailed"));
        });
        Thread t = new Thread(task, "InstallationDesignerPanel-load");
        t.setDaemon(true);
        t.start();
    }

    /**
     * The off-FX-thread read that the load Task delegates to. The Installations Designer surfaces
     * four kinds — Station, WeaponInstallation, Megastructure, TransportNode — explicitly
     * excluding ships (covered by {@code SpaceshipDesignerPanel}) and conduits (deferred per
     * v2 §6.1 Q3). The MEGASTRUCTURE bucket was added in v2 Phase D.8 Step 6 (it existed in
     * Catalog from D.7 Step 6 but the panel didn't read it — that asymmetry was the visible
     * "Loaded 3 construct(s)" symptom on the user's running app at D.7 close-out).
     */
    private List<Cataloged> loadFromRegistry() {
        if (Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "loadFromRegistry must run off the FX thread; it issues blocking DB reads");
        }
        List<Cataloged> all = new ArrayList<>();
        all.addAll(registry.assetsByKind(AssetKind.STATION));
        all.addAll(registry.assetsByKind(AssetKind.WEAPON_INSTALLATION));
        all.addAll(registry.assetsByKind(AssetKind.MEGASTRUCTURE));
        all.addAll(registry.infrastructureByKind(InfrastructureKind.TRANSPORT_NODE));
        // v2 Phase F.1 §5.3 — chokepoint enforcement. Entries from inactive universes drop out
        // here; entries with universe_id=null (canonical/real, R5.6) always pass through. Runs
        // off the FX thread (per the assertion above) so the bulk-fetch of active universe ids
        // is non-blocking from the user's perspective.
        return (filteringService != null) ? filteringService.filter(all) : all;
    }

    /**
     * Apply a freshly-loaded list of constructs to the panel. Package-private so headless tests
     * can drive the apply step without spinning up the background Task.
     */
    void applyConstructs(List<Cataloged> constructs) {
        FxThread.assertFxThread();
        List<ConstructRow> rows = constructs.stream().map(ConstructRow::new).toList();
        allRows = rows;
        rebuildUniverseTabs();
        rebuildFactionFilterOptions();
        rebuildCategoryFilterOptions();
        applyFilters();
        statusLabel.setText(format("status.loaded", rows.size()));
    }

    private void rebuildFactionFilterOptions() {
        Set<String> factions = new LinkedHashSet<>();
        factions.add(ALL);
        allRows.stream()
                .map(ConstructRow::getFaction)
                .filter(Objects::nonNull)
                .filter(f -> !f.isBlank())
                .sorted()
                .forEach(factions::add);
        String previous = factionFilter.getValue();
        factionFilter.getItems().setAll(factions);
        factionFilter.setValue(factions.contains(previous) ? previous : ALL);
    }

    private void rebuildCategoryFilterOptions() {
        Set<String> categories = new LinkedHashSet<>();
        categories.add(ALL);
        allRows.stream()
                .map(ConstructRow::getCategory)
                .filter(Objects::nonNull)
                .filter(c -> !c.isBlank())
                .sorted()
                .forEach(categories::add);
        String previous = categoryFilter.getValue();
        categoryFilter.getItems().setAll(categories);
        categoryFilter.setValue(categories.contains(previous) ? previous : ALL);
    }

    /**
     * Recompute {@link #visibleRows} from {@link #allRows} given the current filter state. Always
     * runs on the FX thread (filter callbacks).
     */
    private void applyFilters() {
        FxThread.assertFxThread();
        String kind = selectedKind();
        String functionLabel = functionFilter.getValue();
        StationFunction selectedFunction = ALL.equals(functionLabel) || functionLabel == null
                ? null
                : StationFunction.valueOf(functionLabel);
        String subtype = subtypeFilter.getValue();
        String faction = factionFilter.getValue();
        String category = categoryFilter.getValue();
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        List<ConstructRow> filtered = new ArrayList<>(allRows.size());
        for (ConstructRow row : allRows) {
            if (!UNIVERSE_ALL.equals(selectedUniverse) && !selectedUniverse.equals(row.getSource())) {
                continue;
            }
            if (!ALL.equals(kind) && !kind.equals(row.getKind())) {
                continue;
            }
            // v2 Phase D.6 Step 7 — Function filter. When a specific StationFunction is selected,
            // rows without a function axis (WeaponInstallation, Spaceship, TransportNode) drop
            // out unconditionally. v2 Phase D.7 Step 6 extended the function axis to
            // Megastructure: both StationDesign and Megastructure carry primaryFunction +
            // secondaryFunctions, so both participate in the function filter.
            if (selectedFunction != null) {
                StationFunction primary;
                java.util.Set<StationFunction> secondaries;
                if (row.getConstruct() instanceof StationDesign s) {
                    primary = s.primaryFunction();
                    secondaries = s.secondaryFunctions();
                } else if (row.getConstruct() instanceof Megastructure m) {
                    primary = m.primaryFunction();
                    secondaries = m.secondaryFunctions();
                } else {
                    continue;
                }
                if (primary != selectedFunction && !secondaries.contains(selectedFunction)) {
                    continue;
                }
            }
            if (!ALL.equals(subtype) && !subtype.equals(row.getSubtype())) {
                continue;
            }
            if (!ALL.equals(faction) && !faction.equalsIgnoreCase(row.getFaction())) {
                continue;
            }
            if (!ALL.equals(category) && !category.equalsIgnoreCase(row.getCategory())) {
                continue;
            }
            if (!search.isEmpty() && !row.getName().toLowerCase(Locale.ROOT).contains(search)) {
                continue;
            }
            filtered.add(row);
        }
        visibleRows.setAll(filtered);
    }

    // -------------------------------------------------------------- details

    /**
     * Build a fresh detail card for the selected construct. The implementation uses Java
     * pattern matching to pick a per-subtype template; the cross-cutting fields go through
     * {@link #identitySection(Cataloged)}.
     */
    private void renderDetailsForSelection(Cataloged selected) {
        FxThread.assertFxThread();
        detailsContent.getChildren().clear();
        if (selected == null) {
            Label empty = new Label(get("details.empty"));
            empty.getStyleClass().add("trips-text-italic-muted-sm");
            detailsContent.getChildren().add(empty);
            return;
        }
        detailsContent.getChildren().add(identitySection(selected));
        Node subtypeSection = switch (selected) {
            case StationDesign s -> stationSection(s);
            case WeaponInstallation w -> weaponSection(w);
            case TransportNode t -> transportSection(t);
            case Megastructure m -> megastructureSection(m);
            default -> new Label(""); // ships + conduits aren't surfaced here
        };
        detailsContent.getChildren().add(subtypeSection);
    }

    private GridPane identitySection(Cataloged c) {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("details.field.id"), c.id());
        addRow(g, r++, get("details.field.name"), c.name());
        if (c instanceof StationDesign sd) {
            addRow(g, r++, get("details.field.designation"), sd.designation());
        } else if (c instanceof WeaponInstallation wi) {
            addRow(g, r++, get("details.field.designation"), wi.designation());
        }
        if (c instanceof Megastructure mg) {
            addRow(g, r++, get("details.field.designation"), mg.designation());
        }
        addRow(g, r++, get("details.field.source"), c.source());
        addRow(g, r++, get("details.field.faction"), c.faction());
        addRow(g, r++, get("details.field.concealed"), Boolean.toString(c.concealed()));
        if (c instanceof SpaceAsset asset) {
            addRow(g, r++, get("details.field.operationalState"), asset.operationalState().name());
        }
        addRow(g, r++, get("details.field.description"), c.description());
        return g;
    }

    private GridPane megastructureSection(Megastructure m) {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("details.megastructure.archetype"), m.archetype().name());
        addRow(g, r++, get("details.megastructure.originType"), m.originType().name());
        if (m.builderPolity() != null && !m.builderPolity().isBlank()) {
            addRow(g, r++, get("details.megastructure.builderPolity"), m.builderPolity());
        }
        if (m.constructionYear() != null) {
            addRow(g, r++, get("details.megastructure.constructionYear"), Integer.toString(m.constructionYear()));
        }
        if (m.discoveryYear() != null) {
            addRow(g, r++, get("details.megastructure.discoveryYear"), Integer.toString(m.discoveryYear()));
        }
        addRow(g, r++, get("details.megastructure.dimensionsKm"), formatDouble(m.dimensionsKm()));
        addRow(g, r++, get("details.megastructure.dryMassMegatons"), formatDouble(m.dryMassMegatons()));
        addRow(g, r++, get("details.megastructure.internalVolumeKm3"), formatDouble(m.internalVolumeKm3()));
        addRow(g, r++, get("details.megastructure.mobility"), m.mobility().name());
        if (m.auxiliaryDrive() != null) {
            addRow(g, r++, get("details.megastructure.auxiliaryDrive"), m.auxiliaryDrive().name());
        }
        if (m.allegiance() != null && !m.allegiance().isBlank()) {
            addRow(g, r++, get("details.megastructure.allegiance"), m.allegiance());
        }
        addRow(g, r++, get("details.megastructure.primaryFunction"), m.primaryFunction().name());
        addRow(g, r++, get("details.megastructure.secondaryFunctions"), formatSecondaryFunctions(m.secondaryFunctions()));
        addRow(g, r++, get("details.megastructure.hasInteriorSetting"), Boolean.toString(m.hasInteriorSetting()));
        addRow(g, r++, get("details.megastructure.interiorPopulation"), Long.toString(m.interiorPopulation()));
        addRow(g, r++, get("details.megastructure.interiorGravity"), m.interiorGravity().name());
        addRow(g, r++, get("details.megastructure.armaments"), Integer.toString(m.armaments().size()));
        addRow(g, r++, get("details.megastructure.sourceUniverse"), m.provenance().sourceUniverse());
        if (m.provenance().sourceWork() != null && !m.provenance().sourceWork().isBlank()) {
            addRow(g, r++, get("details.megastructure.sourceWork"), m.provenance().sourceWork());
        }
        addRow(g, r++, get("details.megastructure.catalogStatus"), m.provenance().status().name());
        return g;
    }

    /**
     * Render the secondary-functions set as a comma-joined enum-name list, or an em dash when
     * empty. Order is by enum declaration so the display is deterministic across calls. Reused
     * by both the station and megastructure templates.
     */
    private static String formatSecondaryFunctions(java.util.Set<com.terranrepublic.assets.StationFunction> set) {
        if (set.isEmpty()) {
            return "—";
        }
        return java.util.Arrays.stream(com.terranrepublic.assets.StationFunction.values())
                .filter(set::contains)
                .map(Enum::name)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private GridPane stationSection(StationDesign s) {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("details.station.stationType"), s.stationType().name());
        addRow(g, r++, get("details.station.mobility"), s.mobility().name());
        addRow(g, r++, get("details.station.allegiance"), s.allegiance());
        if (s.auxiliaryDrive() != null) {
            addRow(g, r++, get("details.station.auxiliaryDrive"), s.auxiliaryDrive().name());
        }
        addRow(g, r++, get("details.station.overallSpanMeters"), formatDouble(s.overallSpanMeters()));
        addRow(g, r++, get("details.station.interiorSpanMeters"), formatDouble(s.interiorSpanMeters()));
        addRow(g, r++, get("details.station.dryMassTons"), formatDouble(s.dryMassTons()));
        addRow(g, r++, get("details.station.armourThicknessMeters"), formatDouble(s.armourThicknessMeters()));
        addRow(g, r++, get("details.station.crewCapacity"), Integer.toString(s.crewCapacity()));
        addRow(g, r++, get("details.station.crewComplement"), Integer.toString(s.crewComplement()));
        addRow(g, r++, get("details.station.carrierCapable"), Boolean.toString(s.carrierCapable()));
        addRow(g, r++, get("details.station.armaments"), Integer.toString(s.armaments().size()));
        addRow(g, r++, get("details.station.carriedCraft"), Integer.toString(s.carriedCraft().size()));
        // v2 Phase D.6 — function + provenance axes
        addRow(g, r++, get("details.station.primaryFunction"), s.primaryFunction().name());
        addRow(g, r++, get("details.station.secondaryFunctions"), formatSecondaryFunctions(s.secondaryFunctions()));
        addRow(g, r++, get("details.station.sourceUniverse"), s.provenance().sourceUniverse());
        if (s.provenance().sourceWork() != null && !s.provenance().sourceWork().isBlank()) {
            addRow(g, r++, get("details.station.sourceWork"), s.provenance().sourceWork());
        }
        addRow(g, r++, get("details.station.catalogStatus"), s.provenance().status().name());
        return g;
    }


    private GridPane weaponSection(WeaponInstallation w) {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("details.weapon.installationType"), w.installationType().name());
        addRow(g, r++, get("details.weapon.emplacement"), w.emplacement().name());
        addRow(g, r++, get("details.weapon.mobile"), Boolean.toString(w.mobile()));
        addRow(g, r++, get("details.weapon.dryMassTons"), formatDouble(w.dryMassTons()));
        addRow(g, r++, get("details.weapon.footprintSpanMeters"), formatDouble(w.footprintSpanMeters()));
        addRow(g, r++, get("details.weapon.crewComplement"), Integer.toString(w.crewComplement()));
        addRow(g, r++, get("details.weapon.armaments"), Integer.toString(w.armaments().size()));
        return g;
    }

    private GridPane transportSection(TransportNode t) {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("details.transport.type"), t.type().name());
        addRow(g, r++, get("details.transport.position"),
                "%s, %s, %s".formatted(formatDouble(t.positionX()), formatDouble(t.positionY()),
                        formatDouble(t.positionZ())));
        addRow(g, r++, get("details.transport.throughputTonsPerTick"), formatDouble(t.throughputTonsPerTick()));
        addRow(g, r++, get("details.transport.instantaneousTransit"), Boolean.toString(t.instantaneousTransit()));
        if (!t.instantaneousTransit()) {
            addRow(g, r++, get("details.transport.traversalTimeTicks"), formatDouble(t.traversalTimeTicks()));
        }
        addRow(g, r++, get("details.transport.connectedNodeIds"), Integer.toString(t.connectedNodeIds().size()));
        return g;
    }

    private static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(4);
        g.setPadding(new Insets(4, 0, 4, 0));
        return g;
    }

    private static void addRow(GridPane g, int row, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("trips-text-form-label");
        Label v = new Label(value == null ? "" : value);
        v.getStyleClass().add("trips-text-form-info");
        v.setWrapText(true);
        g.add(l, 0, row);
        g.add(v, 1, row);
    }

    private static String formatDouble(double v) {
        if (Double.isNaN(v)) {
            return "n/a";
        }
        return v == Math.floor(v) ? Long.toString((long) v) : "%.3f".formatted(v);
    }

    // ----------------------------------------------------------------- test seam

    /**
     * @return an unmodifiable view of the current master row list (for tests)
     */
    List<ConstructRow> rowsForTesting() {
        return List.copyOf(allRows);
    }

    /** @return the table — for tests that need to assert column / row state */
    TableView<ConstructRow> tableForTesting() {
        return table;
    }

    /** @return the details container — for tests that need to assert per-subtype rendering */
    VBox detailsContentForTesting() {
        return detailsContent;
    }

    /** @return the filter combos / search field — for tests that drive them directly */
    ComboBox<String> kindFilterForTesting() {
        return kindFilter;
    }

    ComboBox<String> subtypeFilterForTesting() {
        return subtypeFilter;
    }

    ComboBox<String> factionFilterForTesting() {
        return factionFilter;
    }

    ComboBox<String> functionFilterForTesting() {
        return functionFilter;
    }

    TextField searchFieldForTesting() {
        return searchField;
    }

    Button newButtonForTesting() {
        return newButton;
    }

    Button editButtonForTesting() {
        return editButton;
    }

    Button deleteButtonForTesting() {
        return deleteButton;
    }

    FlowPane universeTabBarForTesting() {
        return universeTabBar;
    }

    ToggleGroup universeToggleGroupForTesting() {
        return universeToggleGroup;
    }

    String selectedUniverseForTesting() {
        return selectedUniverse;
    }
}
