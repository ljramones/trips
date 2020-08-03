package com.teamgannon.trips.dialogs;

import lombok.Data;

@Data
public class DataFileFormat {

    /**
     * the data format type
     */
    private DataFormatEnum dataFormatEnum;

    /**
     * the suffix to search for
     */
    private String suffix;

}
