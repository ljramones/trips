package com.teamgannon.trips.dialogs.dataset.model;

import com.teamgannon.trips.dialogs.support.DataFileFormat;
import lombok.Data;

@Data
public class Dataset {

    /**
     * the name of this dataset
     */
    private String name;

    /**
     * the full file path of the file selected
     */
    private String fileSelected;

    /**
     * the type of the file (CHView, Excel, csv, etc
     */
    private DataFileFormat dataType;

    /**
     * notes entered for the file
     */
    private String notes;

    /**
     * who added this
     */
    private String author;

}
