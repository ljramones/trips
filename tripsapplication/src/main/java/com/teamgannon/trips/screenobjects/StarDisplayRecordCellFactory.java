package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.listener.DatabaseListener;
import com.teamgannon.trips.listener.ListSelectorActionsListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class StarDisplayRecordCellFactory implements Callback<ListView<StarDisplayRecord>, ListCell<StarDisplayRecord>> {

    private DatabaseListener databaseListener;
    private ListSelectorActionsListener listSelectorActionsListener;

    public StarDisplayRecordCellFactory(DatabaseListener databaseListener, ListSelectorActionsListener listSelectorActionsListener) {
        this.databaseListener = databaseListener;
        this.listSelectorActionsListener = listSelectorActionsListener;
    }

    @Override
    public ListCell<StarDisplayRecord> call(ListView<StarDisplayRecord> routeListView) {
        return new StarDisplayRecordCell(databaseListener, listSelectorActionsListener);
    }

}
