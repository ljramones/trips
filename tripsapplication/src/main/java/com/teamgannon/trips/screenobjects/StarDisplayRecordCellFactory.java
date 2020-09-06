package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.listener.DatabaseListener;
import com.teamgannon.trips.listener.ListSelectorActionsListener;
import com.teamgannon.trips.listener.ReportGenerator;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class StarDisplayRecordCellFactory implements Callback<ListView<StarDisplayRecord>, ListCell<StarDisplayRecord>> {

    private final DatabaseListener databaseListener;
    private final ListSelectorActionsListener listSelectorActionsListener;
    private final ReportGenerator reportGenerator;

    public StarDisplayRecordCellFactory(DatabaseListener databaseListener,
                                        ListSelectorActionsListener listSelectorActionsListener,
                                        ReportGenerator reportGenerator) {
        this.databaseListener = databaseListener;
        this.listSelectorActionsListener = listSelectorActionsListener;
        this.reportGenerator = reportGenerator;
    }

    @Override
    public ListCell<StarDisplayRecord> call(ListView<StarDisplayRecord> routeListView) {
        return new StarDisplayRecordCell(databaseListener, listSelectorActionsListener, reportGenerator);
    }

}
