package com.teamgannon.trips.spaceshipmodeller.ui;

import lombok.extern.slf4j.Slf4j;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Accessor for the Spaceship Modeller's externalised UI strings and defaults.
 * <p>
 * Strings live in {@code com/teamgannon/trips/spaceshipmodeller/spaceshipmodeller.properties}. This helper
 * is intentionally lenient: a missing key returns the key itself (or a supplied fallback) and logs a
 * warning rather than throwing, so a typo never crashes the UI.
 */
@Slf4j
public final class SpaceshipModellerLabels {

    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("com.teamgannon.trips.spaceshipmodeller.spaceshipmodeller");

    private SpaceshipModellerLabels() {
    }

    /**
     * @param key property key
     * @return the localised string, or the key itself if it is missing
     */
    public static String get(String key) {
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            log.warn("Missing spaceshipmodeller label: {}", key);
            return key;
        }
    }

    /**
     * @param key      property key
     * @param fallback value to use when the key is absent
     * @return the localised string, or {@code fallback} if it is missing
     */
    public static String get(String key, String fallback) {
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return fallback;
        }
    }

    /**
     * @param key      property key holding an integer default
     * @param fallback value to use when the key is absent or unparseable
     * @return the parsed integer, or {@code fallback}
     */
    public static int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(BUNDLE.getString(key).trim());
        } catch (MissingResourceException | NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * @param key      property key holding a numeric default
     * @param fallback value to use when the key is absent or unparseable
     * @return the parsed double, or {@code fallback}
     */
    public static double getDouble(String key, double fallback) {
        try {
            return Double.parseDouble(BUNDLE.getString(key).trim());
        } catch (MissingResourceException | NumberFormatException e) {
            return fallback;
        }
    }
}
