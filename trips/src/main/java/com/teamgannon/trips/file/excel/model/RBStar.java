package com.teamgannon.trips.file.excel.model;

import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Data
public class RBStar {

    private String number;
    private String starName;
    private String catalog1;
    private String catalog2;
    private String simbadId;
    private String type;
    private String parallax;
    private String magu;
    private String magb;
    private String magv;
    private String magr;
    private String magi;
    private String spectralType;
    private String parents;
    private String siblings;
    private String children;
    private String ra_dms;
    private String dec_dms;
    private String dec_deg;
    private String rs_cdeg;
    private String lyDistance;
    private String x;
    private String y;
    private String z;

    public AstrographicObject toAstrographicObject() {
        try {
            AstrographicObject astro = new AstrographicObject();

            astro.setId(UUID.randomUUID());
            astro.setRbNumber(Integer.parseInt(number));

            astro.getCatalogIdList().add(catalog1);
            astro.getCatalogIdList().add(catalog2);
            astro.setSimbadId(simbadId);
            astro.setStarClassType(type);
            astro.setParallax(Double.parseDouble(parallax));
            astro.setMagu(Double.parseDouble(magu));
            astro.setMagb(Double.parseDouble(magb));
            astro.setMagv(Double.parseDouble(magv));
            astro.setMagr(Double.parseDouble(magr));
            astro.setMagi(Double.parseDouble(magi));
            astro.setSpectralClass(spectralType);

            astro.setRa(Double.parseDouble(ra_dms));
            astro.setDeclination(Double.parseDouble(dec_dms));
            astro.setDec_deg(Double.parseDouble(dec_deg));
            astro.setRs_cdeg(Double.parseDouble(rs_cdeg));
            astro.setDistance(Double.parseDouble(lyDistance));

            double[] coordinates = new double[3];
            coordinates[0] = Double.parseDouble(x);
            coordinates[1] = Double.parseDouble(y);
            coordinates[2] = Double.parseDouble(z);
            astro.setCoordinates(coordinates);

            return astro;
        } catch (Exception e) {
            log.error("failed to convert this star to the Astrographic rep: {}", this);
            return null;
        }
    }

}
