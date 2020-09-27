package com.teamgannon.trips.file.csvin.model;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.stardata.StarColor;
import com.teamgannon.trips.stardata.StellarFactory;
import javafx.scene.paint.Color;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Data
@Builder
public class AstroCSVStar {

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    @Builder.Default
    private StellarFactory stellarFactory = new StellarFactory();

    private String datasetName;
    private String displayName;
    private String constellationName;
    private String mass;
    private String actualMass;
    private String notes;
    private String source;
    private String catalogIdList;

    public String x;
    public String y;
    public String z;

    private String radius;
    private String ra;
    private String pmra;
    private String declination;
    private String pmdec;
    private String dec_deg;
    private String rs_cdeg;

    private String parallax;
    private String distance;
    private String radialVelocity;
    private String spectralClass;
    private String orthoSpectralClass;

    private String temperature;
    private String realStar;

    private String bprp;
    private String bpg;
    private String grp;
    private String temp;

    private String luminosity;

    private String magu;
    private String magb;
    private String magv;
    private String magr;
    private String magi;

    private String other;
    private String anomaly;

    private String catalogid;

    /**
     * What polity does this object belong to.  Obviously, it has to be null or one of the polities listed in
     * the theme above.
     */
    private String polity;

    /**
     * the type of world
     */
    private String worldType;

    /**
     * the type of fuel
     */
    private String fuelType;

    /**
     * the type of port
     */
    private String portType;

    /**
     * the type of population
     */
    private String populationType;

    /**
     * the tech type
     */
    private String techType;

    /**
     * the product type
     */
    private String productType;

    /**
     * the type of military in space
     */
    private String milSpaceType;

    /**
     * the type of military on the planet
     */
    private String milPlanType;


    /**
     * for user custom use in future versions
     */
    private String miscText1;

    /**
     * for user custom use in future versions
     */
    private String miscText2;

    /**
     * for user custom use in future versions
     */
    private String miscText3;

    /**
     * for user custom use in future versions
     */
    private String miscText4;

    /**
     * for user custom use in future versions
     */
    private String miscText5;

    /**
     * for user custom use in future versions
     */
    private double miscNum1;

    /**
     * for user custom use in future versions
     */
    private double miscNum2;

    /**
     * for user custom use in future versions
     */
    private double miscNum3;

    /**
     * for user custom use in future versions
     */
    private double miscNum4;

    /**
     * for user custom use in future versions
     */
    private double miscNum5;

    public AstrographicObject toAstrographicObject() {
        try {
            AstrographicObject astro = new AstrographicObject();

            astro.setId(UUID.randomUUID());

            astro.setDataSetName(datasetName);
            astro.setDisplayName(displayName);
            astro.setConstellationName(constellationName);

            astro.setMass(Double.parseDouble(mass));
            astro.setActualMass(Double.parseDouble(actualMass));
            astro.setNotes(notes);
            astro.setSource(source);
            List<String> catalogList = new ArrayList<>();
            catalogList.add(catalogIdList);
            astro.setCatalogIdList(catalogList);

            astro.setX(Double.parseDouble(x));
            astro.setY(Double.parseDouble(y));
            astro.setZ(Double.parseDouble(z));

            astro.setRadius(Double.parseDouble(radius));
            astro.setRa(Double.parseDouble(ra));
            astro.setPmra(Double.parseDouble(pmra));
            astro.setDeclination(Double.parseDouble(declination));
            astro.setPmdec(Double.parseDouble(pmdec));
            astro.setDec_deg(Double.parseDouble(dec_deg));
            astro.setRs_cdeg(Double.parseDouble(rs_cdeg));

            astro.setParallax(Double.parseDouble(parallax));
            astro.setDistance(Double.parseDouble(distance));
            astro.setRadialVelocity(Double.parseDouble(radialVelocity));
            astro.setSpectralClass(spectralClass);
            astro.setOrthoSpectralClass(orthoSpectralClass);

            astro.setTemperature(Double.parseDouble(temperature));
            astro.setRealStar(Boolean.parseBoolean(realStar));

            astro.setBprp(Double.parseDouble(bprp));


            return astro;

        } catch (Exception e) {
            log.error("failed to convert a RB CSV star object into a astrographic one: {}", e.getMessage());
            return null;
        }

    }

    private Color getColor(StarColor starColor) {
        String[] colorRGB = starColor.color().split(",");
        return Color.rgb(
                Integer.parseInt(colorRGB[0]),
                Integer.parseInt(colorRGB[1]),
                Integer.parseInt(colorRGB[2])
        );
    }

    private double[] parseCoordinate(String position) {
        double[] xyz = new double[3];
        String numbers = position.substring(1, position.length() - 1);
        String[] split = numbers.split(",");

        xyz[0] = Double.parseDouble(split[0]);
        xyz[1] = Double.parseDouble(split[1]);
        xyz[2] = Double.parseDouble(split[2]);
        if (xyz[0] == 0.0 && xyz[1] == 0.0 && xyz[2] == 0.0) {
            log.error("really?");
        }

        return xyz;
    }


}
