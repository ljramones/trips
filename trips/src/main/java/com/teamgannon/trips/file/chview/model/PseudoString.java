package com.teamgannon.trips.file.chview.model;

import lombok.Data;

/**
 * Used to identify what type of string this is
 * <p>
 * Created by larrymitchell on 2017-02-12.
 */
@Data
public class PseudoString {

    private String value;

    private int length;

    private boolean name;

}
