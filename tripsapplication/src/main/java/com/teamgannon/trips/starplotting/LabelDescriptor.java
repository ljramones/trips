package com.teamgannon.trips.starplotting;

import javafx.geometry.Point3D;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LabelDescriptor implements Serializable {

    private static final long serialVersionUID = 3931631698021405619L;

    /**
     * the label text
     */
    private String text;

    /**
     * the actual point location
     */
    private Point3D labelLocation;

}
