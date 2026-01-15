package com.teamgannon.trips.screenobjects.planetary;

import com.teamgannon.trips.events.ContextSelectionType;
import com.teamgannon.trips.events.ContextSelectorEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;

/**
 * List cell for displaying planetary systems with context menu.
 */
@Slf4j
public class PlanetarySystemCell extends ListCell<PlanetarySystemRecord> {

    private final Tooltip tooltip = new Tooltip();
    private final ApplicationEventPublisher eventPublisher;

    public PlanetarySystemCell(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected void updateItem(PlanetarySystemRecord record, boolean empty) {
        super.updateItem(record, empty);

        if (record != null && !empty) {
            // Create display text
            String displayText = record.getStarName() + " (" + record.getPlanetCount() +
                    (record.getPlanetCount() == 1 ? " planet)" : " planets)");
            setText(displayText);

            // Create tooltip
            StarDisplayRecord star = record.getStarDisplayRecord();
            if (star != null) {
                StringBuilder tipText = new StringBuilder();
                tipText.append(star.getStarName());
                if (star.getSpectralClass() != null && !star.getSpectralClass().isEmpty()) {
                    tipText.append("\nSpectral: ").append(star.getSpectralClass());
                }
                tipText.append("\nDistance: ").append(String.format("%.2f", star.getDistance())).append(" ly");
                tipText.append("\nPlanets: ").append(record.getPlanetCount());
                tooltip.setText(tipText.toString());
                setTooltip(tooltip);
            }

            // Create context menu
            ContextMenu contextMenu = new ContextMenu();

            MenuItem enterSystemItem = new MenuItem("Enter System");
            enterSystemItem.setOnAction(event -> {
                log.info("Entering solar system for: {}", record.getStarName());
                eventPublisher.publishEvent(new ContextSelectorEvent(
                        this,
                        ContextSelectionType.SOLARSYSTEM,
                        record.getStarDisplayRecord(),
                        new HashMap<>()
                ));
            });
            MenuItem viewNightSkyItem = new MenuItem("View Night Sky");
            viewNightSkyItem.setOnAction(event -> {
                log.info("Viewing night sky for: {}", record.getStarName());
                eventPublisher.publishEvent(new ContextSelectorEvent(
                        this,
                        ContextSelectionType.PLANETARY,
                        record.getStarDisplayRecord(),
                        new HashMap<>()
                ));
            });
            contextMenu.getItems().addAll(enterSystemItem, viewNightSkyItem);

            setContextMenu(contextMenu);
        } else {
            setText(null);
            setTooltip(null);
            setContextMenu(null);
        }

        setGraphic(null);
    }
}
