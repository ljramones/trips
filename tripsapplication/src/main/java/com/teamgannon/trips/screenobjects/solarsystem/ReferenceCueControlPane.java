package com.teamgannon.trips.screenobjects.solarsystem;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.events.SolarSystemDisplayToggleEvent;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Controls for reference cues in the solar system view.
 */
@Slf4j
@Component
public class ReferenceCueControlPane extends VBox {

    private final ApplicationEventPublisher eventPublisher;

    @Getter
    private final CheckBox showEclipticPlaneCheckbox = new CheckBox("Show Ecliptic Plane/Grid");
    @Getter
    private final CheckBox showOrbitNodesCheckbox = new CheckBox("Show Orbit Nodes (Asc/Desc)");
    @Getter
    private final CheckBox showApsidesCheckbox = new CheckBox("Show Apsides (Peri/Apo)");

    public ReferenceCueControlPane(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        setPadding(new Insets(10));
        setSpacing(10);

        Label header = new Label("Reference Cues");
        header.setStyle("-fx-font-weight: bold;");

        showEclipticPlaneCheckbox.setSelected(false);
        showOrbitNodesCheckbox.setSelected(false);
        showApsidesCheckbox.setSelected(false);

        getChildren().addAll(header, showEclipticPlaneCheckbox, showOrbitNodesCheckbox, showApsidesCheckbox);

        setupListeners();
    }

    private void setupListeners() {
        showEclipticPlaneCheckbox.setOnAction(e -> {
            boolean enabled = showEclipticPlaneCheckbox.isSelected();
            log.info("Show ecliptic plane: {}", enabled);
            eventPublisher.publishEvent(new SolarSystemDisplayToggleEvent(
                    this, SolarSystemDisplayToggleEvent.ToggleType.ECLIPTIC_PLANE, enabled));
        });

        showOrbitNodesCheckbox.setOnAction(e -> {
            boolean enabled = showOrbitNodesCheckbox.isSelected();
            log.info("Show orbit nodes: {}", enabled);
            eventPublisher.publishEvent(new SolarSystemDisplayToggleEvent(
                    this, SolarSystemDisplayToggleEvent.ToggleType.ORBIT_NODES, enabled));
        });

        showApsidesCheckbox.setOnAction(e -> {
            boolean enabled = showApsidesCheckbox.isSelected();
            log.info("Show apsides: {}", enabled);
            eventPublisher.publishEvent(new SolarSystemDisplayToggleEvent(
                    this, SolarSystemDisplayToggleEvent.ToggleType.APSIDES, enabled));
        });
    }

    public void reset() {
        showEclipticPlaneCheckbox.setSelected(false);
        showOrbitNodesCheckbox.setSelected(false);
        showApsidesCheckbox.setSelected(false);
    }
}
