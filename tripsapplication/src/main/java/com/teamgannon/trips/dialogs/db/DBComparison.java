package com.teamgannon.trips.dialogs.db;

import javafx.scene.control.Dialog;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@Data
@Builder
public class DBComparison  {

    /**
     * the database we want to compare from
     */
    private String sourceDataSet;

    /**
     * the database we want to compare to
     */
    private String targetDataset;

    /**
     * this entires in the source database that we didn't find
     */
    private Set<String> namesNotFound;

    /**
     * did we do a comparison
     */
    private boolean selected;

}
