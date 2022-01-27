package com.teamgannon.trips.jpa.model;

import javafx.scene.paint.Color;
import lombok.*;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class CivilizationDisplayPreferences implements Serializable {

    public static final String TERRAN = "Terran";
    public static final String DORNANI = "Dornani";
    public static final String KTOR = "Ktor";
    public static final String ARAKUR = "Arat Kur";
    public static final String HKHRKH = "Hkh'Rkh";
    public static final String SLAASRIITHI = "Slaasriithi";
    public static final String OTHER1 = "Other 1";
    public static final String OTHER2 = "Other 2";
    public static final String OTHER3 = "Other 3";
    public static final String OTHER4 = "Other 4";
    public static final String NONE = "None";

    @Serial
    private static final long serialVersionUID = -3937969336190562493L;


    /**
     * id of the object
     */
    @Id
    private UUID id;
    /**
     * storage tag
     */
    @Column(unique = true)
    private @NotNull String storageTag = "Main";

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
        humanPolityColor = Color.valueOf("25ff29").toString();
        dornaniPolityColor = Color.MAGENTA.toString();
        ktorPolityColor = Color.CYAN.toString();
        aratKurPolityColor = Color.valueOf("ff69b4").toString();
        hkhRkhPolityColor = Color.valueOf("FF0A0F").toString();
        slaasriithiPolityColor = Color.LIGHTCORAL.toString();
        other1PolityColor = Color.valueOf("aa37ff").toString();
        other2PolityColor = Color.valueOf("ffc502").toString();
        other3PolityColor = Color.valueOf("0088dc").toString();
        other4PolityColor = Color.valueOf("98bcf9").toString();
    }

    public @NotNull Color getColorForPolity(String polity) {
        return switch (polity) {
            case CivilizationDisplayPreferences.TERRAN -> Color.valueOf(humanPolityColor);
            case CivilizationDisplayPreferences.DORNANI -> Color.valueOf(dornaniPolityColor);
            case CivilizationDisplayPreferences.KTOR -> Color.valueOf(ktorPolityColor);
            case CivilizationDisplayPreferences.ARAKUR -> Color.valueOf(aratKurPolityColor);
            case CivilizationDisplayPreferences.HKHRKH -> Color.valueOf(hkhRkhPolityColor);
            case CivilizationDisplayPreferences.SLAASRIITHI -> Color.valueOf(slaasriithiPolityColor);
            case CivilizationDisplayPreferences.OTHER1 -> Color.valueOf(other1PolityColor);
            case CivilizationDisplayPreferences.OTHER2 -> Color.valueOf(other2PolityColor);
            case CivilizationDisplayPreferences.OTHER3 -> Color.valueOf(other3PolityColor);
            case CivilizationDisplayPreferences.OTHER4 -> Color.valueOf(other4PolityColor);
            default -> Color.GRAY;
        };

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CivilizationDisplayPreferences that = (CivilizationDisplayPreferences) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
