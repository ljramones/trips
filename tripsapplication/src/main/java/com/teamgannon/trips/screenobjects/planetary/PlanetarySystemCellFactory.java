package com.teamgannon.trips.screenobjects.planetary;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Cell factory for creating PlanetarySystemCell instances.
 */
public class PlanetarySystemCellFactory implements Callback<ListView<PlanetarySystemRecord>, ListCell<PlanetarySystemRecord>> {

    private final ApplicationEventPublisher eventPublisher;

    public PlanetarySystemCellFactory(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public @NotNull ListCell<PlanetarySystemRecord> call(ListView<PlanetarySystemRecord> listView) {
        return new PlanetarySystemCell(eventPublisher);
    }
}
