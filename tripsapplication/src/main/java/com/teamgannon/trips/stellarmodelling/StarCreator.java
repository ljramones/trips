package com.teamgannon.trips.stellarmodelling;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ^((sd|sg|d|c|g))?(O|B|A|F|G|K|M|L|T|Y|P|Q|WN|WC|WR|DA|DQ|DB|DZ|DO|DC|DX|C\-R|C\-N|C\-J|C\-H|C\-Hd)(d(\.d)(\/d(\.d))?)?((0|Ia\+|Ia|Iab|Ib|II|III|IV|V|VII|VII))?((:|...|!|comp|e|\[e\]|er|eq|f|f\*|f\+|\(f\(|\(f\+\)|\(\(f\)\)|\(\(f\*\)\)|h|ha|He wk|k|m|n|nn|neb|p|pq|q|s|ss|sh|var|wl))?
 */
@Slf4j
public class StarCreator {

    private String prefixPatternStr = "^(sd|sg|d|c|g)";
    Pattern prefixPattern = Pattern.compile(prefixPatternStr);

    //private String classPatternStr = "^(O|B|A|F|G|K|M|L|T|Y|P|Q|WN|WC|WR|DA|DQ|DB|DZ|DO|DC|DX|C\\-R|C\\-N|C\\-J|C\\-H|C\\-Hd)d(\\.d)";
    private String classPatternStr = "^(O|B|A|F|G|K|M|L|T|Y|P|Q|WN|WC|WR|DA|DQ|DB|DZ|DO|DC|DX|C\\-R|C\\-N|C\\-J|C\\-H||C\\-Hd)(\\d(\\.\\d)?)?";
    Pattern classPattern = Pattern.compile(classPatternStr);

    private String yerkesPatternStr = "^(0|Ia\\+|Ia|Iab|Ib|II|III|IV|V|VII|VII)";
    Pattern yerkesPattern = Pattern.compile(yerkesPatternStr);

    private String pecularitiesPatternStr = "^(:|...|!|comp|e|\\[e\\]|er|eq|f|f\\*|f\\+|\\(f\\(|\\(f\\+\\)|\\(\\(f\\)\\)|\\(\\(f\\*\\)\\)|h|ha|He wk|k|m|n|nn|neb|p|pq|q|s|ss|sh|var|wl)";
    Pattern pecularitiesPattern = Pattern.compile(pecularitiesPatternStr);

    /**
     * create a stellar model based on the spectral classification
     *
     * @param spectralClassification the classification
     * @return the stellar model
     */
    public StarModel parseSpectral(String spectralClassification) {

        StarModel starModel = new StarModel();
        Matcher preMatcher = prefixPattern.matcher(spectralClassification);
        if (preMatcher.find()) {
            String prefix = preMatcher.group();
            starModel.setStarType(prefix);
            spectralClassification = spectralClassification.substring(prefix.length());
            log.info("prefix is " + prefix);
        }
        Matcher spectralClassMatcher = classPattern.matcher(spectralClassification);
        if (spectralClassMatcher.find()) {
            String spectralClass = spectralClassMatcher.group();
            starModel.setStarClass(spectralClass);
            spectralClassification = spectralClassification.substring(spectralClass.length());
            log.info("star is " + spectralClass);
        }
        Matcher yerkes = yerkesPattern.matcher(spectralClassification);
        if (yerkes.find()) {
            String yerkesClass = yerkes.group();
            starModel.setLuminosityClass(yerkesClass);
            spectralClassification = spectralClassification.substring(yerkesClass.length());
            log.info("luminosity is " + yerkesClass);
        }
        Matcher pecular = pecularitiesPattern.matcher(spectralClassification);
        if (pecular.find()) {
            String percularitiesString = pecular.group();
            starModel.setSpectralPecularities(percularitiesString);
            spectralClassification = spectralClassification.substring(percularitiesString.length());
            log.info("pecularities is " + percularitiesString);
        }

        return starModel;
    }

    public static void main(String[] arg) {
        StarCreator starCreator = new StarCreator();
        StarModel starModel = starCreator.parseSpectral("sgO3.5Ia+[e]");
        log.info("done");
    }

}
