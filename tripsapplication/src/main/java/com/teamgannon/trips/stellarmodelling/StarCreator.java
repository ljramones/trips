package com.teamgannon.trips.stellarmodelling;

import com.teamgannon.trips.solarsysmodelling.utils.spectralclass.StarModel;
import lombok.extern.slf4j.Slf4j;

/**
 * ^((sd|sg|d|c|g))?(O|B|A|F|G|K|M|L|T|Y|P|Q|WN|WC|WR|DA|DQ|DB|DZ|DO|DC|DX|C\-R|C\-N|C\-J|C\-H|C\-Hd)(d(\.d)(\/d(\.d))?)?((0|Ia\+|Ia|Iab|Ib|II|III|IV|V|VII|VII))?((:|...|!|comp|e|\[e\]|er|eq|f|f\*|f\+|\(f\(|\(f\+\)|\(\(f\)\)|\(\(f\*\)\)|h|ha|He wk|k|m|n|nn|neb|p|pq|q|s|ss|sh|var|wl))?
 */
@Slf4j
public class StarCreator {

    private String prefixPattern = "^(sd|sg|d|c|g)";

    private String classPattern = "(O|B|A|F|G|K|M|L|T|Y|P|Q|WN|WC|WR|DA|DQ|DB|DZ|DO|DC|DX|C\\-R|C\\-N|C\\-J|C\\-H|C\\-Hd)d(\\.d)";

    private String yerkesPattern = "(0|Ia\\+|Ia|Iab|Ib|II|III|IV|V|VII|VII)";

    private String pecularitiesPattern = "(:|...|!|comp|e|\\[e\\]|er|eq|f|f\\*|f\\+|\\(f\\(|\\(f\\+\\)|\\(\\(f\\)\\)|\\(\\(f\\*\\)\\)|h|ha|He wk|k|m|n|nn|neb|p|pq|q|s|ss|sh|var|wl)";

    public StarModel getStar(String spectralClass) {
        StarModel starModel = new StarModel();
        String spectralType = spectralClass.substring(0, 1);
        try {
            StellarType stellarType = StellarType.valueOf(spectralType);
            starModel.setStellarClass(stellarType);
        } catch (Exception e) {
            return null;
        }
        return starModel;
    }

    public StarModel parseSpectral(String spectralClassification) {
        StarModel starModel = new StarModel();
        String prefix = spectralClassification.substring(0, 1);
        switch (prefix) {

            case "s" -> {
                String prefix1 = spectralClassification.substring(0, 2);
                if (prefix1.equals("sd")) {
                    log.info("sub dwarf");
                } else if (prefix1.equals("sg")) {
                    log.info("sub giant");
                } else {
                    log.error("improper spectral classification");
                    return null;
                }
            }
            case "d" -> {
                log.info("dwarf");
            }
            case "c" -> {
                log.info("supergiant");
            }
            case "g" -> {
                log.info("giant");
            }

            default -> {
                log.info("no prefix");
            }
        }

        return starModel;
    }

    public static void main(String[] arg) {

    }

}
