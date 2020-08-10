package com.teamgannon.trips.file.csvin.model;


import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.stardata.StarColor;
import com.teamgannon.trips.stardata.StellarClassification;
import com.teamgannon.trips.stardata.StellarFactory;
import com.teamgannon.trips.stardata.StellarType;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public AstrographicObject toAstrographicObject() {
        try {
            AstrographicObject astro = new AstrographicObject();

            astro.setId(UUID.randomUUID());

            astro.setDisplayName(name);
            astro.setStarClassType(type);

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

                // get the star color and store it
                Color starColor = getColor(stellarClassification.getStarColor());
                astro.setStarColor(starColor);

                // the star radius
                double radius = stellarClassification.getAverageRadius();
                astro.setRadius(radius);
            } else {
                astro.setSpectralClass(spectralClass);
                String stellarClass = spectralClass.substring(0, 1);
                if (stellarFactory.classes(stellarClass)) {
                    // the stellar classification
                    stellarClassification = stellarFactory.getStellarClass(stellarClass);

                    // get the star color and store it
                    Color starColor = getColor(stellarClassification.getStarColor());
                    astro.setStarColor(starColor);

                    // the star radius
                    double radius = stellarClassification.getAverageRadius();
                    astro.setRadius(radius);
                } else {
                    astro.setStarColor(getColor(StarColor.M));
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
            astro.setNnClass(nnclass);

            astro.getCatalogIdList().add(catalogid);

            return astro;

        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("failed to convert a RB CSV star object into a astrographic one: {}", e.getMessage());
//            System.out.println(String.format("star is :%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
//                    name, type, ra, dec, pmra, pmdec, parallax,
//                    radialvel, spectral, magv, bprp, bpg, grp, temp, position,
//                    distance, source, nnclass, catalogid)
//            );
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

        return xyz;
    }


}
