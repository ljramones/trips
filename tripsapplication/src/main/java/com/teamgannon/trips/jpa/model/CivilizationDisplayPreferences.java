package com.teamgannon.trips.jpa.model;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private static final long serialVersionUID = 2463297545433929036L;

    @Id
    private String id = UUID.randomUUID().toString();  // Initialize with a new UUID

    @Column(unique = true)
    private @NotNull String storageTag = "Main";

    private String humanPolityColor = Color.BEIGE.toString();
    private String dornaniPolityColor = Color.FUCHSIA.toString();
    private String ktorPolityColor = Color.HONEYDEW.toString();
    private String aratKurPolityColor = Color.ALICEBLUE.toString();
    private String hkhRkhPolityColor = Color.LIGHTGREEN.toString();
    private String slaasriithiPolityColor = Color.LIGHTCORAL.toString();
    private String other1PolityColor = Color.LIGHTGOLDENRODYELLOW.toString();
    private String other2PolityColor = Color.LIGHTSKYBLUE.toString();
    private String other3PolityColor = Color.LIGHTGRAY.toString();
    private String other4PolityColor = Color.LEMONCHIFFON.toString();

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

