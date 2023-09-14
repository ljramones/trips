package com.teamgannon.trips.dialogs.gaiadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StarObjectUtils {


    /**
     * Extracts the catalog IDs from the given input string.
     *
     * @param catalogIdList The input string.
     * @return The list of catalog IDs.
     */
    public static String getGlieseId(List<String> catalogIdList) {
        Pattern pattern = Pattern.compile("^GJ\\s*\\d+");

        Optional<String> result = catalogIdList.stream()
                .filter(entry -> pattern.matcher(entry).find())
                .findFirst();

        return result.orElse(null);
    }

    /**
     * Extracts the HIP catalog ID from the given list of catalog IDs.
     *
     * @param catalogIdList The list of catalog IDs.
     * @return The HIP catalog ID.
     */
    public static String getHipId(List<String> catalogIdList) {
        Pattern pattern = Pattern.compile("^HIP\\s*\\d+");

        Optional<String> result = catalogIdList.stream()
                .filter(entry -> pattern.matcher(entry).find())
                .findFirst();

        return result.orElse(null);
    }

    /**
     * Extracts the HD catalog ID from the given list of catalog IDs.
     *
     * @param catalogIdList The list of catalog IDs.
     * @return The HD catalog ID.
     */
    public static String getHDId(List<String> catalogIdList) {
        Pattern pattern = Pattern.compile("^HD\\s*\\d+");

        Optional<String> result = catalogIdList.stream()
                .filter(entry -> pattern.matcher(entry).find())
                .findFirst();

        return result.orElse(null);
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
            input = input + "|" + replacement;
        } else {
            // Join the parts back together using the | separator
            input = String.join("|", parts);
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
        List<String> matchedItems = new ArrayList<>();

        // Regular expression to match strings starting with HIP, HD or GJ followed by spaces and numbers.
        Pattern pattern = Pattern.compile("\\b(HIP|HD|GJ)\\s*\\d+\\b");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            matchedItems.add(matcher.group());
        }

        return matchedItems;
    }
}
