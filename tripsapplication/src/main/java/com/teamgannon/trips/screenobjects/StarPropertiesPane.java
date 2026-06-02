package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.events.ClearDataEvent;
import com.teamgannon.trips.events.DisplayStarEvent;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService.AliasDisplay;
import com.teamgannon.trips.spaceshipmodeller.service.AliasTooltipFormatter;
import com.teamgannon.trips.worldbuilding.UniverseFilteringService;
import com.terranrepublic.assets.AliasTargetKind;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class StarPropertiesPane extends VBox {

    // Overview
    @FXML
    private Label starNameLabel1;
    @FXML
    private Label commonNameLabel1;
    @FXML
    private Label constellationNameLabel;
    @FXML
    private Label spectralClassLabel;
    @FXML
    private Label distanceNameLabel;
    @FXML
    private Label metallicityLabel;
    @FXML
    private Label ageLabel;
    @FXML
    private TextArea notesArea;

    // Worldbuilding tab — the 13 legacy fictional-info Label/CheckBox fields (starNameLabel2,
    // commonNameLabel2, polityLabel, worldTypeLabel, fuelTypeLabel, techTypeLabel,
    // portTypeLabel, popTypeLabel, prodField, milspaceLabel, milplanLabel, anomalyCheckbox,
    // otherCheckbox) were removed by the Worldbuilding Data Model Normalization task.
    // What's left on the tab is the F.2 Aliases section + the gray-out overlay below.

    /**
     * Step 4 gray-out overlay (Worldbuilding Data Model Normalization). Visible when no
     * worldbuilding universe is currently active, so the tab tells the user where to go
     * rather than leaving them staring at an empty aliases section.
     */
    @FXML
    private VBox noUniverseOverlay;

    // Other Info
    @FXML
    private Label starNameLabel3;
    @FXML
    private Label commonNameLabel3;
    @FXML
    private Label simbadIdLabel;
    @FXML
    private Label galacticCoordinatesLabel;
    @FXML
    private Label radiusLabel;
    @FXML
    private Label massLabel;
    @FXML
    private Label luminosityLabel;
    @FXML
    private Label raLabel;
    @FXML
    private Label decLabel;
    @FXML
    private Label pmraLabel;
    @FXML
    private Label pmdecLabel;
    @FXML
    private Label radialVelocityLabel;
    @FXML
    private Label parallaxLabel;
    @FXML
    private Label tempLabel;

    @FXML
    private Label maguLabel;
    @FXML
    private Label magbLabel;
    @FXML
    private Label magvLabel;
    @FXML
    private Label magrLabel;
    @FXML
    private Label magiLabel;

    @FXML
    private Label bprpLabel;
    @FXML
    private Label bpgLabel;
    @FXML
    private Label grpLabel;

    @FXML
    private Button editButton;
    @FXML
    private Button simbadButton;

    // F.2 §6.2 — Aliases section appended at FXML row 16.
    @FXML
    private Label aliasesContentLabel;

    /** F.2 §6.2 empty-state copy when no active universe has aliased this star. */
    static final String EMPTY_ALIASES_PLACEHOLDER =
            "(no aliases — activate a universe to see worldbuilding names)";


    private @NotNull StarObject record = new StarObject();
    private final StarService starService;
    private final HostServices hostServices;
    /**
     * F.2 §6.2 alias lookup. Nullable for test contexts that don't wire Spring; when null,
     * the Aliases section shows the empty-state placeholder.
     */
    @Nullable
    private final AliasDesignerService aliasService;
    /**
     * Step 4 (Worldbuilding Data Model Normalization) — gray-out overlay driver. Used to
     * check {@link UniverseFilteringService#getActiveUniverseIds()} on each refresh and
     * toggle {@link #noUniverseOverlay} visibility accordingly. Nullable for test contexts
     * that don't wire Spring.
     */
    @Nullable
    private final UniverseFilteringService universeFilteringService;
    /**
     * F.2 §6.2 broker subscription handle for {@code UniverseActivationChangedEvent}. The pane
     * is a long-lived Spring singleton owned by {@code RightPanelController}; the subscription
     * lives for the app lifetime and the handle is held primarily for symmetry with the
     * UniversesDialog pattern (cleanup is technically unnecessary but available via the
     * package-private {@link #disposeAliasSubscription} seam for tests).
     */
    @Nullable
    private Runnable filterChangeUnsubscribe;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.###");

    @Autowired
    public StarPropertiesPane(StarService starService,
                              FxWeaver fxWeaver,
                              @Nullable AliasDesignerService aliasService,
                              @Nullable UniverseFilteringService filteringService) {
        this.starService = starService;
        this.hostServices = fxWeaver.getBean(HostServices.class);
        this.aliasService = aliasService;
        this.universeFilteringService = filteringService;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("StarPropertiesPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load StarPropertiesPane.fxml", ex);
        }

        // F.2 §6.2 — broker subscription. When a universe activates/deactivates, re-fetch
        // and refresh the aliases section for whatever star is currently displayed. Matches
        // the UniversesDialog pattern from F.1 Step 6.
        if (filteringService != null) {
            this.filterChangeUnsubscribe =
                    filteringService.subscribeToFilterChanges(this::refreshAliasesSection);
        }
    }

    @FXML
    private void initialize() {
        editButton.setDisable(true);
        editButton.setOnAction(this::editStar);
        simbadButton.setOnAction(event -> openSimbad());
    }

    private void editStar(ActionEvent actionEvent) {
        if (record.getId() != null) {
            log.info("edit star");
            // Issue 23: build the detached VM from the entity (aliases
            // materialised) before handing to the dialog.
            StarEditViewModel vm = StarEditMapper.toViewModel(record);
            StarEditDialog starEditDialog = new StarEditDialog(vm);
            Optional<StarEditStatus> statusOptional = starEditDialog.showAndWait();
            if (statusOptional.isPresent() && statusOptional.get().isChanged()) {
                // Apply the edited VM back to the live entity, persist, and refresh the pane.
                StarEditMapper.applyToEntity(statusOptional.get().getViewModel(), record);
                starService.updateStar(record);
                setStar(record);
                this.getScene().getWindow().setWidth(this.getScene().getWindow().getWidth() + 0.001);
            }
        }
    }

    public void setStar(@NotNull StarObject record) {
        this.record = record;

        editButton.setDisable(false);

        // primary tab
        starNameLabel1.setText(safeDisplay(record.getDisplayName()));
        commonNameLabel1.setText(safeDisplay(record.getCommonName()));
        constellationNameLabel.setText(safeDisplay(record.getConstellationName()));
        spectralClassLabel.setText(safeDisplay(record.getOrthoSpectralClass()));
        distanceNameLabel.setText(formatDouble(record.getDistance()));
        metallicityLabel.setText(formatDouble(record.getMetallicity()));
        ageLabel.setText(formatDouble(record.getAge()));
        notesArea.setText(safeDisplay(record.getNotes()));

        // Worldbuilding tab — legacy worldbuilding field labels removed by the Worldbuilding
        // Data Model Normalization task. The tab is renamed in Step 4 + gains gray-out
        // overlay. Only the F.2 Aliases section remains; F.3 reintroduces a Faction section.

        // F.2 §6.2 — Aliases section
        populateAliasesSection(record);

        // Step 4 (Worldbuilding Data Model Normalization) — toggle gray-out overlay based
        // on whether any worldbuilding universe is currently active.
        refreshGrayOutOverlay();

        // other info tab
        starNameLabel3.setText(safeDisplay(record.getDisplayName()));
        commonNameLabel3.setText(safeDisplay(record.getCommonName()));
        String simbadId = safeDisplay(record.getSimbadId());
        simbadIdLabel.setText(simbadId);
        galacticCoordinatesLabel.setText(formatDouble(record.getGalacticLat()) + ", " + formatDouble(record.getGalacticLong()));
        radiusLabel.setText(formatDouble(record.getRadius()));
        massLabel.setText(formatDouble(record.getMass()));
        luminosityLabel.setText(safeDisplay(record.getLuminosity()));
        raLabel.setText(formatDouble(record.getRa()));
        decLabel.setText(formatDouble(record.getDeclination()));
        pmraLabel.setText(formatDouble(record.getPmra()));
        pmdecLabel.setText(formatDouble(record.getPmdec()));
        radialVelocityLabel.setText(formatDouble(record.getRadialVelocity()));
        parallaxLabel.setText(formatDouble(record.getParallax()));
        tempLabel.setText(formatDouble(record.getTemperature()));

        maguLabel.setText(formatDouble(record.getMagu()));
        magbLabel.setText(formatDouble(record.getMagb()));
        magvLabel.setText(formatDouble(record.getMagv()));
        magrLabel.setText(formatDouble(record.getMagr()));
        magiLabel.setText(formatDouble(record.getMagi()));

        bprpLabel.setText(formatDouble(record.getBprp()));
        bpgLabel.setText(formatDouble(record.getBpg()));
        grpLabel.setText(formatDouble(record.getGrp()));

        simbadButton.setDisable(simbadId.isEmpty() || emptyDisplay().equals(simbadId));
    }

    /**
     * Clears the data displayed in the UI components.
     */
    public void clearData() {

        // primary tab
        starNameLabel1.setText(emptyDisplay());
        commonNameLabel1.setText(emptyDisplay());
        constellationNameLabel.setText(emptyDisplay());
        spectralClassLabel.setText(emptyDisplay());
        distanceNameLabel.setText(emptyDisplay());
        metallicityLabel.setText(emptyDisplay());
        ageLabel.setText(emptyDisplay());
        notesArea.setText("");

        // Worldbuilding tab — the 11 legacy fictional-info widgets came out with the
        // Worldbuilding Data Model Normalization task, so there's nothing to reset
        // beyond the Aliases section and the gray-out overlay.

        // F.2 §6.2 — clear Aliases section to empty placeholder
        if (aliasesContentLabel != null) {
            aliasesContentLabel.setText(EMPTY_ALIASES_PLACEHOLDER);
        }

        // Step 4 (Worldbuilding Data Model Normalization) — keep gray-out overlay in
        // sync after a clear so a "no active universe" state still wins through.
        refreshGrayOutOverlay();

        // other info tab
        starNameLabel3.setText(emptyDisplay());
        commonNameLabel3.setText(emptyDisplay());
        simbadIdLabel.setText(emptyDisplay());
        galacticCoordinatesLabel.setText(emptyDisplay());
        radiusLabel.setText(emptyDisplay());
        massLabel.setText(emptyDisplay());
        luminosityLabel.setText(emptyDisplay());
        raLabel.setText(emptyDisplay());
        decLabel.setText(emptyDisplay());
        pmraLabel.setText(emptyDisplay());
        pmdecLabel.setText(emptyDisplay());
        radialVelocityLabel.setText(emptyDisplay());
        parallaxLabel.setText(emptyDisplay());
        tempLabel.setText(emptyDisplay());

        maguLabel.setText(emptyDisplay());
        magbLabel.setText(emptyDisplay());
        magvLabel.setText(emptyDisplay());
        magrLabel.setText(emptyDisplay());
        magiLabel.setText(emptyDisplay());

        bprpLabel.setText(emptyDisplay());
        bpgLabel.setText(emptyDisplay());
        grpLabel.setText(emptyDisplay());

        simbadButton.setDisable(true);
    }

    private void openSimbad() {
        String simbadId = safeDisplay(record.getSimbadId());
        if (simbadId.isEmpty() || emptyDisplay().equals(simbadId)) {
            return;
        }
        String simbadRecord = URLEncoder.encode(simbadId, StandardCharsets.UTF_8);
        hostServices.showDocument("http://simbad.u-strasbg.fr/simbad/sim-id?Ident=" + simbadRecord);
    }

    private String formatDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return emptyDisplay();
        }
        return decimalFormat.format(value);
    }

    private static String safeDisplay(String value) {
        if (value == null || value.isBlank()) {
            return emptyDisplay();
        }
        return value;
    }

    private static String emptyDisplay() {
        return "--";
    }


    /**
     * Listens for the ClearDataEvent and clears the data asynchronously on the JavaFX Application Thread.
     * This method is annotated with EventListener to indicate that it is an event listener for the ClearDataEvent.
     * It uses the Platform.runLater() method to execute the clearData() method on the JavaFX Application Thread.
     * <p>
     * // Trigger the ClearDataEvent
     * EventManager.triggerEvent(new ClearDataEvent());
     * <p>
     * // The onClearDataEvent() method will be automatically called on the JavaFX Application Thread
     * // and the data will be cleared.
     */
    @EventListener
    public void onClearDataEvent(ClearDataEvent event) {
        Platform.runLater(this::clearData);
    }

    /**
     * Listens for a DisplayStarEvent and updates the star object on the UI thread.
     *
     * @param event The DisplayStarEvent to be handled.
     */
    @EventListener
    public void onDisplayStarEvent(DisplayStarEvent event) {
        Platform.runLater(() -> {
            log.info("STAR PROPERTIES PANE ::: Receive a display star event: star is:{}", event.getStarObject().getDisplayName());
            setStar(event.getStarObject());
        });
    }

    // -------------------------------------------------------------------- F.2 §6.2 Aliases section

    /**
     * Refreshes the Aliases section and gray-out overlay for the currently-displayed star.
     * Called by the UniverseFilteringService broker whenever a universe activates/deactivates.
     * Defensive on everything: pane may have no currently-displayed star (empty
     * {@code record.id}); aliasService / universeFilteringService may be null (tests);
     * FXML fields may not yet be wired (constructor-time subscription firing before initial
     * render).
     */
    private void refreshAliasesSection() {
        FxThread.runOnFxThread(() -> {
            if (record != null && record.getId() != null) {
                populateAliasesSection(record);
            }
            // Step 4 (Worldbuilding Data Model Normalization) — the gray-out overlay flips
            // alongside alias content whenever the active-universe set changes.
            refreshGrayOutOverlay();
        });
    }

    /**
     * Step 4 (Worldbuilding Data Model Normalization) — toggles the "no active universe"
     * gray-out overlay on the Worldbuilding tab. Defensive: the overlay node may be null
     * (constructor-time broker fire before FXML load) and the filtering service may be null
     * (test contexts without Spring); both cases short-circuit silently.
     */
    private void refreshGrayOutOverlay() {
        if (noUniverseOverlay == null || universeFilteringService == null) {
            return;
        }
        boolean noActiveUniverse = universeFilteringService.getActiveUniverseIds().isEmpty();
        noUniverseOverlay.setVisible(noActiveUniverse);
    }

    /**
     * Populates the Aliases section's label with bullet-list text for active-universe aliases
     * targeting this star. Skipped when {@link #aliasesContentLabel} hasn't been injected by
     * the FXML loader yet (defensive — shouldn't happen post-{@code initialize()}, but
     * test harnesses may construct the pane partially).
     */
    private void populateAliasesSection(@NotNull StarObject star) {
        if (aliasesContentLabel == null) {
            return;
        }
        List<AliasDisplay> aliases = (aliasService == null || star.getId() == null)
                ? List.of()
                : aliasService.findActiveAliasesForTooltip(AliasTargetKind.STAR, star.getId());
        aliasesContentLabel.setText(
                AliasTooltipFormatter.formatAliasesAsBulletList(aliases, EMPTY_ALIASES_PLACEHOLDER));
    }

    /**
     * Test-only seam: drops the broker subscription. Long-lived production callers don't need
     * this (the pane lives for the app lifetime), but tests that construct + destroy panes
     * benefit from clean unsubscribe to avoid stale callbacks between tests.
     */
    void disposeAliasSubscription() {
        if (filterChangeUnsubscribe != null) {
            filterChangeUnsubscribe.run();
            filterChangeUnsubscribe = null;
        }
    }
}
