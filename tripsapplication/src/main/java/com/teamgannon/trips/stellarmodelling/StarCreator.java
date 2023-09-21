package com.teamgannon.trips.stellarmodelling;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ^((sd|sg|d|c|g))?(O|B|A|F|G|K|M|L|T|Y|P|Q|WN|WC|WR|DA|DQ|DB|DZ|DO|DC|DX|C\-R|C\-N|C\-J|C\-H|C\-Hd)(d(\.d)(\/d(\.d))?)?((0|Ia\+|Ia|Iab|Ib|II|III|IV|V|VII|VII))?((:|...|!|comp|e|\[e\]|er|eq|f|f\*|f\+|\(f\(|\(f\+\)|\(\(f\)\)|\(\(f\*\)\)|h|ha|He wk|k|m|n|nn|neb|p|pq|q|s|ss|sh|var|wl))?
 */
@Slf4j
public class StarCreator {

    private final String prefixPatternStr = "^(sd|sg|d|c|g)";
    Pattern prefixPattern = Pattern.compile(prefixPatternStr);

    private final String curlyBracketsStr= "\\{(.*?)\\}";
    Pattern curlyBracketsPattern = Pattern.compile(curlyBracketsStr);

    private final String regBracketsStr= "\\((.*?)\\)";
    Pattern regBracketsPattern = Pattern.compile(regBracketsStr);

    private static final String STELLAR_CLASS = "OBAFGKMLTPQYS";

    //private String classPatternStr = "^(O|B|A|F|G|K|M|L|T|Y|P|Q|WN|WC|WR|DA|DQ|DB|DZ|DO|DC|DX|C\\-R|C\\-N|C\\-J|C\\-H|C\\-Hd)d(\\.d)";
    private final String classPatternStr = "^(O|B|A|F|G|K|M|L|T|Y|P|Q|Unk|WN|WC|WR|S|D|DA|DQ|DB|DZ|DO|DC|DX|C|C\\-R|C\\-N|C\\-J|C\\-H||C\\-Hd)(\\d(\\.\\d)?)?";
    Pattern classPattern = Pattern.compile(classPatternStr);

    private final String yerkesPatternStr = "^(0|Ia\\+|Ia|Iab|Ib|II|III|IV|V|VII|VIII|IX|X)";
    Pattern yerkesPattern = Pattern.compile(yerkesPatternStr);

    private final String pecularitiesPatternStr = "^(:|...|!|comp|e|\\[e\\]|er|eq|f|f\\*|f\\+|\\(f\\(|\\(f\\+\\)|\\(\\(f\\)\\)|\\(\\(f\\*\\)\\)|h|ha|He wk|k|m|n|nn|neb|p|pq|q|s|ss|sh|var|wl)";
    Pattern pecularitiesPattern = Pattern.compile(pecularitiesPatternStr);

    public static void main(String[] arg) {
        StarCreator starCreator = new StarCreator();
        StarModel starModel = starCreator.parseSpectral("Sw");
        log.info(starModel.toString());
    }

    public static boolean isValidStarType(char ch) {
        return STELLAR_CLASS.contains(Character.toString(ch));
    }

    /**
     * create a stellar model based on the spectral classification
     *
     * @param spectralClassification the classification
     * @return the stellar model
     */
    public StarModel parseSpectral(String spectralClassification) {
        if (spectralClassification.equals("Unk")) {
            log.info("Unknown");
        }

        StarModel starModel = new StarModel();
        Matcher preMatcher = prefixPattern.matcher(spectralClassification);
        if (preMatcher.find()) {
            String prefix = preMatcher.group();
            spectralClassification = spectralClassification.substring(prefix.length());
//            log.info("prefix is " + prefix);
        }
        Matcher spectralClassMatcher = classPattern.matcher(spectralClassification);
        if (spectralClassMatcher.find()) {
            String spectralClass = spectralClassMatcher.group();
            starModel.setStarClass(spectralClass);
            spectralClassification = spectralClassification.substring(spectralClass.length()).trim();
//            log.info("star is " + spectralClass);
        }
        Matcher curlyBracketsMatcher = curlyBracketsPattern.matcher(spectralClassification);
        if (curlyBracketsMatcher.find()) {
            String insideStr = curlyBracketsMatcher.group();
            spectralClassification = spectralClassification.substring(insideStr.length());
            insideStr = insideStr.substring(1, insideStr.length()-1);
            spectralClassification = insideStr+spectralClassification;
        }
        Matcher regBracketsMatcher = regBracketsPattern.matcher(spectralClassification);
        if (regBracketsMatcher.find()) {
            String insideStr = regBracketsMatcher.group();
            spectralClassification = spectralClassification.substring(insideStr.length());
            insideStr = insideStr.substring(1, insideStr.length()-1);
            spectralClassification = insideStr+spectralClassification;
        }
        Matcher yerkes = yerkesPattern.matcher(spectralClassification);
        if (yerkes.find()) {
            String yerkesClass = yerkes.group();
            starModel.setLuminosityClass(yerkesClass);
            spectralClassification = spectralClassification.substring(yerkesClass.length());
//            log.info("luminosity is " + yerkesClass);
        }
        Matcher pecular = pecularitiesPattern.matcher(spectralClassification);
        if (pecular.find()) {
            String percularitiesString = pecular.group();
            starModel.setSpectralPecularities(percularitiesString);
//            spectralClassification = spectralClassification.substring(percularitiesString.length());
//            log.info("peculiarities is " + peculiaritiesString);
        }

        return starModel;
    }



}
