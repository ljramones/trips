package com.teamgannon.trips.jpa.model;

import com.teamgannon.trips.stellarmodelling.StarCreator;
import com.teamgannon.trips.stellarmodelling.StarModel;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for calculating display scores for stars.
 * The display score determines whether and how prominently a star's
 * label should be shown in the 3D visualization.
 */
@Slf4j
public final class DisplayScoreCalculator {

    /**
     * Pattern to match Flamsteed catalog ID: "* nnn Con"
     * where nnn is 3 digits and Con is a constellation abbreviation
     */
    private static final Pattern FLAMSTEED_PATTERN = Pattern.compile("[\\*] +[0-9]{3} +[a-zA-Z]{3}");

    /**
     * Pattern to match Bayer catalog ID: "* ggg Con"
     * where ggg is a greek letter abbreviation and Con is a constellation abbreviation
     */
    private static final Pattern BAYER_PATTERN = Pattern.compile("[\\*] +[a-zA-Z]{3} +[a-zA-Z]{3}");

    private DisplayScoreCalculator() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculate the display score for a star.
     * Higher scores indicate the star should be more prominently labeled.
     *
     * @param star the star object to calculate score for
     * @return the calculated display score
     */
    public static double calculate(StarObject star) {
        return calculateBaseScore(star.getOrthoSpectralClass()) * calculateLabelMultiplier(star);
    }

    /**
     * Calculate the label multiplier based on catalog presence and other factors.
     *
     * @param star the star object
     * @return the multiplier value
     */
    static double calculateLabelMultiplier(StarObject star) {
        double cumulativeTotal = 0;

        // 1. Is there something in the Common Name field?
        String commonName = star.getCommonName();
        if (commonName != null && !commonName.isEmpty()) {
            cumulativeTotal += 3;
        }

        String catalogIdList = star.getCatalogIds().getCatalogIdList();
        if (catalogIdList == null) {
            catalogIdList = "";
        }

        // 2. Does the star have a Flamsteed catalog ID?
        Matcher flamMatcher = FLAMSTEED_PATTERN.matcher(catalogIdList);
        if (flamMatcher.find()) {
            cumulativeTotal += 3;
        }

        // 3. Does the star have a Bayer catalog ID?
        Matcher bayerMatcher = BAYER_PATTERN.matcher(catalogIdList);
        if (bayerMatcher.find()) {
            cumulativeTotal += 3;
        }

        // 4. Is the star in the BD catalog?
        if (catalogIdList.contains("BD+") || catalogIdList.contains("BD-")) {
            cumulativeTotal += 1.5;
        }

        // 5. Is the star in the Gliese catalog?
        if (catalogIdList.contains("GJ")) {
            cumulativeTotal += 1.5;
        }

        // 6. Is the star in Hipparchos?
        if (catalogIdList.contains("HIP")) {
            cumulativeTotal += 1.5;
        }

        // 7. Is the star in Henry Draper?
        if (catalogIdList.contains("HD")) {
            cumulativeTotal += 1.5;
        }

        // 8. Is there an entry in polity?
        String polity = star.getWorldBuilding().getPolity();
        if (polity != null && !polity.trim().isEmpty() && !"NA".equals(polity)) {
            cumulativeTotal += 3;
        }

        // 9. Check if any other fictional item is set
        if (star.getWorldBuilding().hasAnyFieldsSet()) {
            cumulativeTotal += 3;
        }

        // 10. If none of the above, make the multiplier one
        if (cumulativeTotal == 0) {
            cumulativeTotal = 1;
        }

        return cumulativeTotal;
    }

    /**
     * Calculate the base score from spectral class.
     *
     * @param orthoSpectralClass the orthogonal spectral class
     * @return the base score
     */
    static double calculateBaseScore(String orthoSpectralClass) {
        int base = 0;

        if (orthoSpectralClass == null || orthoSpectralClass.isEmpty()) {
            return 1;
        }

        StarModel starModel = new StarCreator().parseSpectral(orthoSpectralClass);

        if (starModel.getStellarClass() == null) {
            log.error("could not find stellar class for: {}", orthoSpectralClass);
            return 1;
        }

        // Process Harvard spectral class
        if (orthoSpectralClass.length() > 1) {
            String harvardSpecClass = starModel.getStellarClass().getValue();

            switch (harvardSpecClass) {
                case "O", "A", "B" -> base += 2;
                case "F", "K" -> base += 4;
                case "G" -> base += 5;
                case "M" -> base += 3;
                case "L", "T", "Y" -> base += 1;
            }
        }

        // Process luminosity class
        String luminosityValue = starModel.getLuminosityClass();
        if (luminosityValue != null && !luminosityValue.isEmpty()) {
            int lumNum = switch (luminosityValue) {
                case "I" -> 1;
                case "II" -> 2;
                case "III" -> 3;
                case "IV" -> 4;
                case "V" -> 5;
                case "VI" -> 6;
                case "VII" -> 7;
                case "VIII" -> 8;
                case "IX" -> 9;
                case "X" -> 10;
                default -> 1;
            };
            base += (11 - lumNum);
        } else {
            base += 1;
        }

        return base;
    }
}
