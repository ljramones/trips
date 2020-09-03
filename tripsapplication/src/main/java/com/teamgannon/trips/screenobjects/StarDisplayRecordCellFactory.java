package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class StarDisplayRecordCellFactory implements Callback<ListView<StarDisplayRecord>, ListCell<StarDisplayRecord>> {

    private ListSelectorActionsListener listSelectorActionsListener;

    public StarDisplayRecordCellFactory(ListSelectorActionsListener listSelectorActionsListener) {
        this.listSelectorActionsListener = listSelectorActionsListener;
    }

    @Override
    public ListCell<StarDisplayRecord> call(ListView<StarDisplayRecord> routeListView) {
        return new StarDisplayRecordCell(listSelectorActionsListener);
    }

}
