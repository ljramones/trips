package com.teamgannon.trips.jpa.model;

import javafx.scene.paint.Color;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class TransitSettings {

    @Id
    private String id;

    private double upperDistance = 9;

    private double lowerDistance = 3;

    private double lineWidth = 1;

    private String lineColor = Color.CYAN.toString();

}
