package com.teamgannon.trips.file.csvin.model;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.stellarmodelling.StarCreator;
import com.teamgannon.trips.stellarmodelling.StellarFactory;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Slf4j
@Data
@Builder
public class AstroCSVStar {


    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    @Builder.Default
    private @NotNull StellarFactory stellarFactory = new StellarFactory();

    private String datasetName;
    private String displayName;
    private String commonName;
    private String systemName;
    private String epoch;
    private String simbadId;
    private String bayerCatId;
    private String glieseCatId;
    private String hipCatId;
    private String hdCatId;
    private String flamsteedCatId;
    private String tycho2CatId;
    private String gaiaDR2CatId;
    private String gaiaDR3CatId;
    private String gaiaEDR3CatId;
    private String twoMassCatId;
    private String csiCatId;
    private String constellationName;
    private double mass;
    private double age;
    private double metallicity;
    private String notes;
    private String source;
    private String catalogIdList;
    private String radius;
    private double ra;
    private double pmra;
    private double declination;
    private double pmdec;

    private double parallax;

    private double distance;
    private double x;
    private double y;
    private double z;
    private double radialVelocity;
    private String spectralClass;

    private String temperature;
    private String realStar;

    private double bprp;
    private double bpg;
    private double grp;
    private double temp;

    private String luminosity;

    private double magu;
    private double magb;
    private double magv;
    private double magr;
    private double magi;
    private String absoluteMagnitude;

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
     * number of exoplanets
     */
    private int numExoplanets;



    /**
     * conversion constructor
     *
     * @return the star object
     */
    public @Nullable StarObject toStarObject() {
        try {
            StarObject astro = new StarObject();

            astro.setId(UUID.randomUUID().toString());

            astro.setDataSetName(datasetName);
            astro.setDisplayName(displayName.trim());
            astro.setCommonName(commonName.trim());
            astro.setSystemName(systemName.trim());
            astro.setEpoch(epoch.trim());
            astro.setConstellationName(constellationName.trim());
            astro.setMass(mass);
            astro.setNotes(notes.trim());
            astro.setSource(source.trim());
            astro.setCatalogIdList(catalogIdList);

            astro.setSimbadId(simbadId.trim());
            astro.setGaiaDR2CatId(gaiaDR2CatId == null ? "" : gaiaDR2CatId.trim());
            astro.setGaiaDR3CatId(gaiaDR3CatId == null ? "" : gaiaDR3CatId.trim());
            astro.setGaiaEDR3CatId(gaiaEDR3CatId == null ? "" : gaiaEDR3CatId.trim());
            astro.setRadius(parseDouble(radius.trim()));
            astro.setRa(ra);
            astro.setDeclination(declination);
            astro.setPmra(pmra);
            astro.setPmdec(pmdec);
            astro.setDistance(distance);
            astro.setParallax(parallax);
            if (astro.getDistance() <= 0 && parallax > 0) {
                astro.setDistance(calculateDistanceFromParallax(parallax));
            }
            if (hasCoordinates()) {
                astro.setX(x);
                astro.setY(y);
                astro.setZ(z);
                if (astro.getDistance() <= 0) {
                    astro.setDistance(calculateDistanceFromCoords(x, y, z));
                }
            } else {
                setCoordinatesFromRaDec(astro, ra, declination, distance);
            }
            astro.setRadialVelocity(radialVelocity);
            String spectralValue = spectralClass == null ? "" : spectralClass.trim();
            astro.setSpectralClass(spectralValue);
            if (!spectralValue.isEmpty()) {
                astro.setOrthoSpectralClass(getOrthoSpectralClass(spectralValue.substring(0, 1)));
            } else {
                astro.setOrthoSpectralClass("M");
            }

            astro.setTemperature(parseDouble(temperature.trim()));
            astro.setRealStar(Boolean.parseBoolean(realStar.trim()));

            astro.setBprp(bprp);
            astro.setBpg(bpg);
            astro.setGrp(grp);

            astro.setLuminosity(luminosity.trim());

            astro.setMagu(magu);
            astro.setMagb(magb);
            astro.setMagv(magv);
            astro.setMagr(magr);
            astro.setMagi(magi);
            if (absoluteMagnitude != null) {
                astro.setAbsoluteMagnitude(absoluteMagnitude.trim());
            } else {
                astro.setAbsoluteMagnitude("");
            }

            astro.setOther(Boolean.parseBoolean(other.trim()));
            astro.setAnomaly(Boolean.parseBoolean(anomaly.trim()));

            astro.setPolity(polity);
            astro.setWorldType(worldType);
            astro.setFuelType(fuelType);
            astro.setPortType(portType);
            astro.setPopulationType(populationType);
            astro.setTechType(techType);
            astro.setProductType(productType);
            astro.setMilSpaceType(milSpaceType);
            astro.setMilPlanType(milPlanType);

            astro.setAge(age);
            astro.setMetallicity(metallicity);


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

            astro.setMiscNum1(miscNum1);
            astro.setMiscNum2(miscNum2);
            astro.setMiscNum3(miscNum3);
            astro.setMiscNum4(miscNum4);
            astro.setMiscNum5(miscNum5);

            astro.setNumExoplanets(numExoplanets);

            return astro;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("failed to convert a CSV star object into a astrographic one: {}", e.getMessage());
            return null;
        }

    }

    private String getOrthoSpectralClass(String spectralClass) {

        // this should never happen but we hedge our bets
        if (spectralClass.isEmpty()) {
            log.error("spectral class cannot be empty");
            return "M";
        }

        if (StarCreator.isValidStarType(spectralClass.charAt(0))) {
            return spectralClass;
        } else {
            if (spectralClass.length()>2 && spectralClass.charAt(0) == ('d')) {
                if (StarCreator.isValidStarType(spectralClass.charAt(1))) {
                    return spectralClass.substring(1, 2);
                } else {
                    return "M";
                }
            }
            if (spectralClass.equals("Unk")) {
                return "M";
            }
        }

        return "M";
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

    private boolean hasCoordinates() {
        return x != 0.0 || y != 0.0 || z != 0.0;
    }

    private void setCoordinatesFromRaDec(StarObject astro, double raDeg, double decDeg, double distance) {
        if (distance <= 0) {
            return;
        }
        double raRad = Math.toRadians(raDeg);
        double decRad = Math.toRadians(decDeg);
        double cosDec = Math.cos(decRad);
        double x = distance * cosDec * Math.cos(raRad);
        double y = distance * cosDec * Math.sin(raRad);
        double z = distance * Math.sin(decRad);
        astro.setX(x);
        astro.setY(y);
        astro.setZ(z);
    }

    private double calculateDistanceFromCoords(double x, double y, double z) {
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    private double calculateDistanceFromParallax(double parallaxMas) {
        if (parallaxMas <= 0) {
            return 0.0;
        }
        double distanceParsecs = 1000.0 / parallaxMas;
        return distanceParsecs * 3.26156;
    }

}
