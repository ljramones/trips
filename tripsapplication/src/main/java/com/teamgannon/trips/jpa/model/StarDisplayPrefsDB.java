package com.teamgannon.trips.jpa.model;

import javafx.scene.paint.Color;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Entity
public class StarDisplayPrefsDB implements Serializable {

    @Id
    private String id;

    private String oClassColor = Color.DARKBLUE.toString();
    private String bClassColor = Color.MEDIUMBLUE.toString();
    private String aClassColor = Color.LIGHTBLUE.toString();
    private String fClassColor = Color.LIGHTYELLOW.toString();
    private String gClassColor = Color.YELLOW.toString();
    private String kClassColor = Color.ORANGE.toString();
    private String mClassColor = Color.RED.toString();
    private String lClassColor = Color.DARKRED.toString();
    private String tClassColor = Color.PURPLE.toString();
    private String yClassColor = Color.MEDIUMVIOLETRED.toString();

    private double oClassSize = 10;
    private double bClassSize = 8;
    private double aClassSize = 6;
    private double fClassSize = 1;
    private double gClassSize = 1;
    private double kClassSize = 0.8;
    private double mClassSize = 0.5;
    private double lClassSize = 0.3;
    private double tClassSize = 0.2;
    private double yClassSize = 0.2;

}
