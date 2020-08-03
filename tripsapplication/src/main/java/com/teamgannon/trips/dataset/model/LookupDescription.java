package com.teamgannon.trips.dataset.model;

import lombok.Data;

/**
 * Describes the specific record
 * <p>
 * Created by larrymitchell on 2017-03-07.
 */
@Data
public class LookupDescription {

    private String longCode;

    private String shortCode;

    private String description;

    public LookupDescription(String[] nextLine) {
        longCode = nextLine[0];
        shortCode = nextLine[1];
        description = nextLine[2];
    }
}
