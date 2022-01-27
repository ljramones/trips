package com.teamgannon.trips.file.csvin.model;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.stellarmodelling.StarColor;
import com.teamgannon.trips.stellarmodelling.StellarFactory;
import javafx.scene.paint.Color;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Slf4j
@Data
@Builder
public class AstroCSVStar {

    public String x;
    public String y;
    public String z;
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    @Builder.Default
    private @NotNull StellarFactory stellarFactory = new StellarFactory();
    private String datasetName;
    private String displayName;
    private String commonName;
    private String simbadId;
    private String gaiaId;
    private String constellationName;
    private String mass;
    private String age;
    private String metallicity;
    private String notes;
    private String source;
    private String catalogIdList;
    private String radius;
    private String ra;
    private String pmra;
    private String declination;
    private String pmdec;


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

    /**
     * galactic Lat
     */
    private String galacticLattitude;

    /**
     * galactic long
     */
    private String galacticLongitude;


    /**
     * conversion constructor
     *
     * @return the star object
     */
    public @Nullable StarObject toStarObject() {
        try {
            StarObject astro = new StarObject();

            astro.setId(UUID.randomUUID());

            astro.setDataSetName(datasetName);
            astro.setDisplayName(displayName.trim());
            astro.setCommonName(commonName.trim());
            astro.setSimbadId(simbadId.trim());
            astro.setGaiaId(gaiaId.trim());
            astro.setConstellationName(constellationName.trim());

            astro.setMass(parseDouble(mass.trim()));
            astro.setNotes(notes.trim());
            astro.setSource(source.trim());
            astro.setCatalogIdList(catalogIdList);

            astro.setRa(parseDouble(ra.trim()));
            astro.setDeclination(parseDouble(declination.trim()));
            astro.setDistance(parseDouble(distance.trim()));

            // if the xyz coordinates aren't defined then calculate them from the RA and dec
            if (x.trim().isEmpty() && y.trim().isEmpty() && z.trim().isEmpty()) {
                // calculate form RA and dec
                calculateXYZ(astro);
            } else {
                // xyz are there
                astro.setX(parseDouble(x.trim()));
                astro.setY(parseDouble(y.trim()));
                astro.setZ(parseDouble(z.trim()));

                if (astro.getX() == 0 && astro.getY() == 0 && astro.getZ() == 0 && astro.getDistance() > 0) {
                    calculateXYZ(astro);
                }
            }

            astro.setRadius(parseDouble(radius.trim()));

            astro.setPmra(parseDouble(pmra.trim()));
            astro.setPmdec(parseDouble(pmdec.trim()));

            astro.setParallax(parseDouble(parallax.trim()));

            astro.setRadialVelocity(parseDouble(radialVelocity.trim()));
            astro.setSpectralClass(spectralClass.trim());
            astro.setOrthoSpectralClass(orthoSpectralClass.trim());

            astro.setTemperature(parseDouble(temperature.trim()));
            astro.setRealStar(Boolean.parseBoolean(realStar.trim()));

            astro.setBprp(parseDouble(bprp.trim()));

            astro.setPolity(polity);
            astro.setWorldType(worldType);
            astro.setFuelType(fuelType);
            astro.setPortType(portType);
            astro.setPopulationType(populationType);
            astro.setTechType(techType);
            astro.setProductType(productType);
            astro.setMilSpaceType(milSpaceType);
            astro.setMilPlanType(milPlanType);

            astro.setMiscText1(miscText1);
            astro.setMiscText2(miscText2);
            astro.setMiscText3(miscText3);
            astro.setMiscText4(miscText4);
            astro.setMiscText5(miscText5);

            astro.setMiscNum1(miscNum1);
            astro.setMiscNum2(miscNum2);
            astro.setMiscNum3(miscNum3);
            astro.setMiscNum4(miscNum4);
            astro.setMiscNum5(miscNum5);

            try {

                if (galacticLattitude.isEmpty()) {
                    astro.setGalacticLat(0);
                } else {
                    double galLat = Double.parseDouble(galacticLattitude.trim());
                    astro.setGalacticLat(galLat);
                }

                if (galacticLongitude.isEmpty()) {
                    astro.setGalacticLong(0);
                } else {
                    double galLong = Double.parseDouble(galacticLongitude.trim());
                    astro.setGalacticLong(galLong);
                }

            } catch (Exception e) {
                astro.setGalacticLat(0.0);
                astro.setGalacticLong(0.0);
            }

            astro.setMiscText5(miscText5);

            astro.setMiscNum1(miscNum1);
            astro.setMiscNum2(miscNum2);
            astro.setMiscNum3(miscNum3);
            astro.setMiscNum4(miscNum4);
            astro.setMiscNum5(miscNum5);

            // this appears to slow down import by a lot
//            astro.calculateDisplayScore();

            return astro;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("failed to convert a CSV star object into a astrographic one: {}", e.getMessage());
            return null;
        }

    }

    private void calculateXYZ(StarObject astro) {
        double[] coordinates = StarMath.getPosition(astro.getRa(), astro.getDeclination(), astro.getDistance());
        astro.setX(coordinates[0]);
        astro.setY(coordinates[1]);
        astro.setZ(coordinates[2]);
    }

    private double parseDouble(String string) {
        if (string.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    private @NotNull Color getColor(@NotNull StarColor starColor) {
        String[] colorRGB = starColor.color().split(",");
        return Color.rgb(
                Integer.parseInt(colorRGB[0]),
                Integer.parseInt(colorRGB[1]),
                Integer.parseInt(colorRGB[2])
        );
    }

    private double @NotNull [] parseCoordinate(@NotNull String position) {
        double[] xyz = new double[3];
        String numbers = position.substring(1, position.length() - 1);
        String[] split = numbers.split(",");

        xyz[0] = parseDouble(split[0]);
        xyz[1] = parseDouble(split[1]);
        xyz[2] = parseDouble(split[2]);
        if (xyz[0] == 0.0 && xyz[1] == 0.0 && xyz[2] == 0.0) {
            log.error("really?");
        }

        return xyz;
    }

}
