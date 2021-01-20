package com.teamgannon.trips.file.csvin.model;


import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.stardata.StarColor;
import com.teamgannon.trips.stardata.StellarClassification;
import com.teamgannon.trips.stardata.StellarFactory;
import com.teamgannon.trips.stardata.StellarType;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Data
public class RBCSVStar {

    private StellarFactory stellarFactory;

    private String name;
    private String type;
    private String ra;

    private String dec;
    private String pmra;
    private String pmdec;

    private String parallax;
    private String radialvel;
    private String spectral;

    private String magv;
    private String bprp;
    private String bpg;

    private String grp;
    private String temp;
    private String position;

    private String distance;
    private String source;
    private String nnclass;

    private String catalogid;

    public RBCSVStar(StellarFactory stellarFactory) {
        this.stellarFactory = stellarFactory;
    }

    public @Nullable StarObject toAstrographicObject() {
        try {
            StarObject astro = new StarObject();

            astro.setId(UUID.randomUUID());

            astro.setDisplayName(name);

            astro.setRa(Double.parseDouble(ra));
            astro.setDeclination(Double.parseDouble(dec));

            astro.setPmra(Double.parseDouble(pmra));
            astro.setPmdec(Double.parseDouble(pmdec));

            astro.setParallax(Double.parseDouble(parallax));
            astro.setRadialVelocity(Double.parseDouble(radialvel));

            StellarClassification stellarClassification;
            String spectralClass = spectral.trim();

            // we check for an empty spectral class which requires special circumstances
            if (spectralClass.isEmpty()) {
                spectralClass = "?";
                astro.setSpectralClass(spectralClass);
                stellarClassification = stellarFactory.getStellarClass(StellarType.Q.toString());
                astro.setOrthoSpectralClass(StellarType.Q.toString());

                // the star radius
                double radius = stellarClassification.getAverageRadius();
                if (radius == 0) {
                    radius = 1.2;
                }
                astro.setRadius(radius);
            } else {
                astro.setSpectralClass(spectralClass);
                String stellarClass = spectralClass.substring(0, 1);
                astro.setOrthoSpectralClass(stellarClass);
                if (stellarFactory.classes(stellarClass)) {
                    // the stellar classification
                    stellarClassification = stellarFactory.getStellarClass(stellarClass);

                    // the star radius
                    double radius = stellarClassification.getAverageRadius();
                    if (radius == 0) {
                        radius = 1.2;
                    }
                    astro.setRadius(radius);
                } else {
                    astro.setRadius(0.5);
                }
            }


            astro.setMagv(Double.parseDouble(magv));

            // bprp
            astro.setBprp(Double.parseDouble(bprp));

            // bpg
            astro.setBpg(Double.parseDouble(bpg));

            // grp
            astro.setGrp(Double.parseDouble(grp));

            // temp
            astro.setTemperature(Double.parseDouble(temp));

            // position
            double[] coordinates = parseCoordinate(position);
            astro.setCoordinates(coordinates);


            astro.setDistance(Double.parseDouble(distance));

            astro.setRealStar(true);

            astro.setSource(source);

            astro.getCatalogIdList().add(catalogid);

            return astro;

        } catch (Exception e) {
            log.error("failed to convert a RB CSV star object into a astrographic one: {}", e.getMessage());
            return null;
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

        xyz[0] = Double.parseDouble(split[0]);
        xyz[1] = Double.parseDouble(split[1]);
        xyz[2] = Double.parseDouble(split[2]);
        if (xyz[0] == 0.0 && xyz[1] == 0.0 && xyz[2] == 0.0) {
            log.error("really?");
        }

        return xyz;
    }


}
