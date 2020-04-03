package com.teamgannon.trips.service;

import com.teamgannon.trips.elasticsearch.model.Star;
import com.teamgannon.trips.elasticsearch.model.StellarSystem;
import lombok.Data;

/**
 * Defines a parse result form the CSV file
 * <p>
 * Created by larrymitchell on 2017-01-23.
 */
@Data
public class ParseResult {

    private boolean success;

    private long idProcessed;

    private Star star;

    private StellarSystem stellarSystem;

}
