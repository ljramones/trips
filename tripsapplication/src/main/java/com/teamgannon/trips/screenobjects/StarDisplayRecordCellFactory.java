package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.listener.DatabaseListener;
import com.teamgannon.trips.listener.ListSelectorActionsListener;
import com.teamgannon.trips.listener.RedrawListener;
import com.teamgannon.trips.listener.ReportGenerator;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

public class StarDisplayRecordCellFactory implements Callback<ListView<StarDisplayRecord>, ListCell<StarDisplayRecord>> {

    private final DatabaseListener databaseListener;
    private final ListSelectorActionsListener listSelectorActionsListener;
    private final ReportGenerator reportGenerator;
    private final RedrawListener redrawListener;

    public StarDisplayRecordCellFactory(DatabaseListener databaseListener,
                                        ListSelectorActionsListener listSelectorActionsListener,
                                        ReportGenerator reportGenerator,
                                        RedrawListener redrawListener) {
        this.databaseListener = databaseListener;
        this.listSelectorActionsListener = listSelectorActionsListener;
        this.reportGenerator = reportGenerator;
        this.redrawListener = redrawListener;
    }

    @Override
    public @NotNull ListCell<StarDisplayRecord> call(ListView<StarDisplayRecord> routeListView) {
        return new StarDisplayRecordCell(
                databaseListener,
                listSelectorActionsListener,
                reportGenerator,
                redrawListener);
    }

}
