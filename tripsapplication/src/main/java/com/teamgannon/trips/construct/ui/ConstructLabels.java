package com.teamgannon.trips.construct.ui;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Accessor for the Constructs feature's externalised UI strings.
 * <p>
 * Strings live in {@code com/teamgannon/trips/construct/construct.properties}. Mirrors
 * {@code SpaceshipModellerLabels}: a missing key returns the key itself (or a supplied fallback)
 * and logs a warning rather than throwing, so a typo never crashes the UI.
 */
@Slf4j
public final class ConstructLabels {

    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("com.teamgannon.trips.construct.construct");

    private ConstructLabels() {
    }

    public static String get(String key) {
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            log.warn("Missing construct label: {}", key);
            return key;
        }
    }

    public static String get(String key, String fallback) {
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return fallback;
        }
    }

    /**
     * MessageFormat-style lookup. Argument substitution uses {@link MessageFormat}.
     *
     * @param key  property key
     * @param args arguments substituted into the template
     * @return the formatted string, or the key itself if the lookup fails
     */
    public static String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
