package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.service.StarService;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public class StarDisplayRecordCellFactory implements Callback<ListView<StarDisplayRecord>, ListCell<StarDisplayRecord>> {

    private final StarService starService;
    private final ApplicationEventPublisher eventPublisher;

    public StarDisplayRecordCellFactory(StarService starService,
                                        ApplicationEventPublisher eventPublisher) {
        this.starService = starService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public @NotNull ListCell<StarDisplayRecord> call(ListView<StarDisplayRecord> routeListView) {
        return new StarDisplayRecordCell(
                starService,
                eventPublisher);
    }

}
