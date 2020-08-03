package com.teamgannon.trips.dataset.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of constants
 * <p>
 * Created by larrymitchell on 2017-03-06.
 */
@Data
public class LookupFile {

    /**
     * the headers
     */
    private String[] headers = new String[3];

    /**
     * the list of lookups
     */
    private List<LookupDescription> records = new ArrayList<>();

    public void addLookupDescription(LookupDescription lookupDescription) {
        records.add(lookupDescription);
    }

}
