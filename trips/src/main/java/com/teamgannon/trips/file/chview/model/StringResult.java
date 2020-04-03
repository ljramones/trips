package com.teamgannon.trips.file.chview.model;

import lombok.Data;

/**
 * The string result
 * used to parse an array of ASCII bytes to a string with corresponding length
 * <p>
 * Created by larrymitchell on 2017-02-07.
 */
@Data
public class StringResult {

    /**
     * the length of the string in bytes
     */
    private int length;

    /**
     * the value
     */
    private String value;

    /**
     * index advancement
     */
    private int indexAdd;

}
