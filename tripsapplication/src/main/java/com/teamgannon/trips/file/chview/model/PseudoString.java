package com.teamgannon.trips.file.chview.model;

import lombok.Data;

/**
 * Used to identify what type of string this is
 * <p>
 * Created by larrymitchell on 2017-02-12.
 */
@Data
public class PseudoString {

    /**
     * the actual value
     */
    private String value;

    /**
     * the length
     */
    private int length;

    /**
     * the name
     */
    private boolean name;

}
