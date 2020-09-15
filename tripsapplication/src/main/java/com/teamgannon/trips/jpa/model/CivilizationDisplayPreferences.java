package com.teamgannon.trips.jpa.model;

import javafx.scene.paint.Color;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
public class CivilizationDisplayPreferences implements Serializable {

    /**
     * id of the object
     */
    @Id
    private UUID id;

    private static final long serialVersionUID = -3083652537359014304L;

    /**
     * storage tag
     */
    @Column(unique = true)
    private String storageTag = "Main";

    /**
     * color of human polity
     */
    private String humanPolityColor = Color.BEIGE.toString();

    /**
     * color of Dornani polity
     */
    private String dornaniPolityColor = Color.FUCHSIA.toString();

    /**
     * color of Ktor polity
     */
    private String ktorPolityColor = Color.HONEYDEW.toString();

    /**
     * color of Arat kur
     */
    private String aratKurPolityColor = Color.ALICEBLUE.toString();

    /**
     * color Hrk'Rkh polity
     */
    private String hkhRkhPolityColor = Color.LIGHTGREEN.toString();

    /**
     * color of Slaasriithi polity
     */
    private String slaasriithiPolityColor = Color.LIGHTCORAL.toString();

    /**
     * custom polity 1
     */
    private String other1PolityColor = Color.LIGHTGOLDENRODYELLOW.toString();

    /**
     * custom polity 2
     */
    private String other2PolityColor = Color.LIGHTSKYBLUE.toString();

    /**
     * custom polity 3
     */
    private String other3PolityColor = Color.LIGHTGRAY.toString();

    /**
     * custom polity 4
     */
    private String other4PolityColor = Color.LEMONCHIFFON.toString();

    /**
     * reset the colors
     */
    public void reset() {
        humanPolityColor = Color.BEIGE.toString();
        dornaniPolityColor = Color.FUCHSIA.toString();
        ktorPolityColor = Color.HONEYDEW.toString();
        aratKurPolityColor = Color.ALICEBLUE.toString();
        hkhRkhPolityColor = Color.LIGHTGREEN.toString();
        slaasriithiPolityColor = Color.LIGHTCORAL.toString();
        other1PolityColor = Color.LIGHTGOLDENRODYELLOW.toString();
        other2PolityColor = Color.LIGHTSKYBLUE.toString();
        other3PolityColor = Color.LIGHTGRAY.toString();
        other4PolityColor = Color.LEMONCHIFFON.toString();
    }

}
