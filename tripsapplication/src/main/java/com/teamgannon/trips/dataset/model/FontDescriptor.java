package com.teamgannon.trips.dataset.model;

import lombok.Data;

import javax.persistence.Embeddable;

/**
 * Font descriptor
 * <p>
 * Created by larrymitchell on 2017-04-02.
 */
@Data
@Embeddable
public class FontDescriptor {

    private String name;

    private int size;

    public FontDescriptor() {
    }

    public FontDescriptor(String name, int size) {
        this.name = name;
        this.size = size;
    }
}
