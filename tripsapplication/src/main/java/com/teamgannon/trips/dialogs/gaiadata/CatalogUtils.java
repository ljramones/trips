package com.teamgannon.trips.dialogs.gaiadata;

import com.teamgannon.trips.dialogs.tycho2hip.BayerChecker;
import com.teamgannon.trips.jpa.model.StarObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CatalogUtils {

    public static final String HIP = "HIP";
    public static final String HD = "HD";
    public static final String GJ = "GJ";
    public static final String TYC2 = "TYC2";
    public static final String TWO_MASS = "2MASS";
    public static final String GAIADR2 = "Gaia DR2";
    public static final String GAIADR3 = "Gaia DR3";
    public static final String GAIAEDR3 = "Gaia EDR3";
    public static final String CSI = "CSI";


    public static String getCatalogId(String catalogId) {
        if (catalogId.startsWith(HIP)) {
            return HIP;
        } else if (catalogId.startsWith(HD)) {
            return HD;
        } else if (catalogId.startsWith(GJ)) {
            return GJ;
        } else if (catalogId.startsWith(TYC2)) {
            return TYC2;
        } else if (catalogId.startsWith(TWO_MASS)) {
            return TWO_MASS;
        } else if (catalogId.startsWith(GAIADR2)) {
            return GAIADR2;
        } else if (catalogId.startsWith(GAIADR3)) {
            return GAIADR3;
        } else if (catalogId.startsWith(GAIAEDR3)) {
            return GAIAEDR3;
        } else if (catalogId.startsWith(CSI)) {
            return CSI;
        } else {
            return null;
        }
    }

    /**
     * Extracts the catalog IDs from the given input string.
     *
     * @param catalogIdList The input string.
     * @return The list of catalog IDs.
     */
    public static String getGlieseId(String catalogIdList) {
        List<String> catItems = extractCatalogItems(catalogIdList);
        for (String catItem : catItems) {
            if (catItem.startsWith(GJ)) {
                return catItem;
            }
        }
        return "";
    }

    /**
     * Extracts the HIP catalog ID from the given list of catalog IDs.
     *
     * @param catalogIdList The list of catalog IDs.
     * @return The HIP catalog ID.
     */
    public static String getHipId(String catalogIdList) {
        List<String> catItems = extractCatalogItems(catalogIdList);
        for (String catItem : catItems) {
            if (catItem.startsWith(HIP)) {
                return catItem;
            }
        }
        return "";
    }

    /**
     * Extracts the HD catalog ID from the given list of catalog IDs.
     *
     * @param catalogIdList The list of catalog IDs.
     * @return The HD catalog ID.
     */
    public static String getHDId(String catalogIdList) {
        List<String> catItems = extractCatalogItems(catalogIdList);
        for (String catItem : catItems) {
            if (catItem.startsWith(HD)) {
                return catItem;
            }
        }
        return "";
    }

    /**
     * Replaces the substring in the input that starts with the given sequence.
     *
     * @param input       The input string.
     * @param beginsWith  The starting sequence of the substring to replace.
     * @param replacement The string to replace the substring with.
     * @return The modified input string.
     */
    public static String replaceOrAddSubstringStartingWith(String input, String beginsWith, String replacement) {
        // Split the input string using the | separator
        String[] parts = input.split("\\|");

        boolean sequenceFound = false;

        // Loop through each part to find and replace the substring starting with the given sequence
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith(beginsWith)) {
                parts[i] = replacement;
                sequenceFound = true;
            }
        }

        // If the sequence was not found, append the replacement to the end
        if (!sequenceFound) {
            input = input + "| " + replacement;
        } else {
            // Join the parts back together using the | separator
            input = String.join("| ", parts);
        }

        return input;
    }

    /**
     * Extracts the catalog items from the given input string.
     *
     * @param input The input string.
     * @return The list of catalog items.
     */
    private static List<String> extractCatalogItems(String input) {
        String regex = "(?<=\\||^)\\s*(GJ|HD|HIP|TYC2|Gaia DR2|Gaia DR3|Gaia EDR3|2MASS|CSI)\\s*[^|]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        List<String> matchedSubstrings = new ArrayList<>();
        while (matcher.find()) {
            matchedSubstrings.add(matcher.group().trim());
        }
        return matchedSubstrings;
    }

    /**
     * Replaces multiple consecutive spaces with a single space.
     *
     * @param input The string to process.
     * @return The processed string with consecutive spaces reduced to a single space.
     */
    public static String reduceMultipleSpaces(String input) {
        if (input == null) {
            return null;
        }

        return input.replaceAll("\\s+", " ");
    }


    // "8pc 310.75|PLX  742|2MASS J03325591-0927298|* eps Eri|*  18 Eri|1AXG J033256-0926|2E   788|2RE J033253-092646|2RE J0332-092|BD-09   697|CCDM J03329-0927A|Ci 20  241|FK5  127|GC  4244|GCRV  1962|GEN# +1.00022049|GJ   144.0|GJ   144|HD  22049|HIC  16537|HIP  16537|HR  1084|IRAS 03305-0937|IRC -10048|JP11   792|LFT  291|LHS  1557|LPM 158|LTT  1675|N30  726|NLTT 11207|PM 03306-0938|PMC 90-93    91|PPM 185905|RAFGL  497|RBS   439|RE J0332-092|RE J033255-092717|ROT   531|SACS  76|SAO 130564|SKY#  5366|SPOCS  171|TD1  2301|TYC 5296-1533-1|UBV    3410|UBV M   9580|V* eps Eri|YZ   0  4513|YZ  99   836|Zkh  51|[FS2003] 0134|uvby98 100022049|PLX  742.00|1E 0330.5-0937|1RXS J033256.4-092727|2E 0330.5-0937|2EUVE J0332-09.4|EUVE J0332-09.4|RX J0332.9-0927|WDS J03329-0927A|WDS J03329-0927Aa,Ab|** BLA    2|Gaia DR2 5164707970261630080|** MBA    1A|NAME Ran|WEB  3160|GES J03325495-0927294                                                                                                                                                           "

    public static StarObject setCatalogIds(StarObject starObject) {

        if (starObject.getRawCatalogIdList() == null || starObject.getRawCatalogIdList().isEmpty()) {
            return starObject;
        }

        // reduce multiple spaces to 1
        starObject.setCatalogIdList(reduceMultipleSpaces(starObject.getRawCatalogIdList()));

        List<String> catItems = extractCatalogItems(starObject.getRawCatalogIdList());
        if (catItems.isEmpty()) {
            return starObject;
        }

        if (BayerChecker.isBayerFormat(starObject.getDisplayName())) {
            starObject.setBayerCatId(starObject.getDisplayName());
        } else if (BayerChecker.isFlamsteedFormat(starObject.getDisplayName())) {
            starObject.setFlamsteedCatId(starObject.getDisplayName());
        }
        starObject.setTycho2CatId(searchCatalogs(catItems, TYC2));
        starObject.setHipCatId(searchCatalogs(catItems, HIP));
        starObject.setHdCatId(searchCatalogs(catItems, HD));
        starObject.setGlieseCatId(searchCatalogs(catItems, GJ));
        starObject.setTwoMassCatId(searchCatalogs(catItems, TWO_MASS));
        starObject.setGaiaDR2CatId(searchCatalogs(catItems, GAIADR2));
        starObject.setGaiaDR3CatId(searchCatalogs(catItems, GAIADR3));
        starObject.setGaiaEDR3CatId(searchCatalogs(catItems, GAIAEDR3));
        starObject.setCsiCatId(searchCatalogs(catItems, CSI));

        return starObject;
    }

    /**
     * Searches the provided list of catalog IDs for the first entry that starts with the given prefix.
     *
     * @param catalogIds List of catalog IDs to search.
     * @param prefix     The prefix to search for.
     * @return The first matched catalog ID or null if no match is found.
     */
    public static String searchCatalogs(List<String> catalogIds, String prefix) {
        for (String id : catalogIds) {
            if (id.startsWith(prefix)) {
                return id;
            }
        }

        return ""; // Return empty string if no match is found
    }

    /**
     * Removes everything including and after the '|' character from the provided string.
     *
     * @param input The string to process.
     * @return The processed string with content after '|' removed.
     */
    public static String removeAfterPipe(String input) {
        int index = input.indexOf('|');
        if (index != -1) {
            return input.substring(0, index).trim();
        } else {
            return input; // Return original string if no '|' character found
        }
    }

    /**
     * Replaces all double quotes in the provided string with spaces and then trims the string.
     *
     * @param input The string to process.
     * @return The processed string with double quotes replaced by spaces, and then trimmed.
     */
    public static String replaceQuotesWithSpaceAndTrim(String input) {
        if (input == null) {
            return null;
        }

        return input.replace("\"", " ").trim();
    }

    public static void cleanUpEntries(StarObject starObject) {
        // some of the gaia dr2 entries have a | in them, remove it
        starObject.setGaiaDR2CatId(removeAfterPipe(starObject.getGaiaDR2CatId()));
        starObject.setCatalogIdList(replaceQuotesWithSpaceAndTrim(starObject.getRawCatalogIdList()));
    }

    public static void setGaiaDR2Id(StarObject starObject) {
        if (starObject.getRawCatalogIdList() == null || starObject.getRawCatalogIdList().isEmpty()) {
            return;
        }

        // reduce multiple spaces to 1
        starObject.setCatalogIdList(reduceMultipleSpaces(starObject.getRawCatalogIdList()));

        List<String> catItems = extractCatalogItems(starObject.getRawCatalogIdList());
        if (catItems.isEmpty()) {
            return;
        }
        starObject.setGaiaDR2CatId(searchCatalogs(catItems, GAIADR2));
    }
}
