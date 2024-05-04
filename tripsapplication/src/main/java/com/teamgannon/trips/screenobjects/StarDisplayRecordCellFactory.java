package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.listener.DatabaseListener;
import com.teamgannon.trips.listener.ListSelectorActionsListener;
import com.teamgannon.trips.listener.RedrawListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public class StarDisplayRecordCellFactory implements Callback<ListView<StarDisplayRecord>, ListCell<StarDisplayRecord>> {

    private final DatabaseListener databaseListener;
    private final ListSelectorActionsListener listSelectorActionsListener;
    private final RedrawListener redrawListener;
    private final ApplicationEventPublisher eventPublisher;

    public StarDisplayRecordCellFactory(DatabaseListener databaseListener,
                                        ListSelectorActionsListener listSelectorActionsListener,
                                        RedrawListener redrawListener,
                                        ApplicationEventPublisher eventPublisher) {
        this.databaseListener = databaseListener;
        this.listSelectorActionsListener = listSelectorActionsListener;
        this.redrawListener = redrawListener;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public @NotNull ListCell<StarDisplayRecord> call(ListView<StarDisplayRecord> routeListView) {
        return new StarDisplayRecordCell(
                databaseListener,
                listSelectorActionsListener,
                redrawListener,
                eventPublisher);
    }

}
