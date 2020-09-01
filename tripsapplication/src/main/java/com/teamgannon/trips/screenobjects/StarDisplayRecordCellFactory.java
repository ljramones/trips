package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class StarDisplayRecordCellFactory implements Callback<ListView<StarDisplayRecord>, ListCell<StarDisplayRecord>> {

    private ListSelecterActionsListener listSelecterActionsListener;

    public StarDisplayRecordCellFactory(ListSelecterActionsListener listSelecterActionsListener) {
        this.listSelecterActionsListener = listSelecterActionsListener;
    }

    @Override
    public ListCell<StarDisplayRecord> call(ListView<StarDisplayRecord> routeListView) {
        return new StarDisplayRecordCell(listSelecterActionsListener);
    }

}
