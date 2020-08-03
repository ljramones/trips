package com.teamgannon.trips.file.simbad;

import com.teamgannon.trips.jpa.model.SimbadEntry;
import lombok.Data;

/**
 * The results of a simbad parse entry
 * <p>
 * Created by larrymitchell on 2017-02-24.
 */
@Data
public class SimbadParseResult {

    private boolean success;

    private long idProcessed;

    private SimbadEntry simbadEntry;
}
