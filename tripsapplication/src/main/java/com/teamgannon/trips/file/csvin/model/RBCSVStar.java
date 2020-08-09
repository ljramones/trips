package com.teamgannon.trips.file.csvin.model;


import com.teamgannon.trips.jpa.model.AstrographicObject;
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
            astro.setSpectralClass(spectral);

            astro.setMagv(Double.parseDouble(magv));

            // bprp
            // bpg
            // grp

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
            log.error("failed to convert a RB CSV star object into a astrographic one: {}", e.getMessage());
            return null;
        }

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
