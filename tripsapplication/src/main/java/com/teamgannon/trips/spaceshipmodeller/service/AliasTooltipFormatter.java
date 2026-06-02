package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService.AliasDisplay;

import java.util.List;

/**
 * Pure-function formatter for the F.2 §6.1 format-α tooltip surface. Extracted from the
 * renderers so the multi-line layout can be unit-tested without spinning up a JavaFX Toolkit
 * or Spring context.
 *
 * <p>Format α (no prefix; parenthetical universe attribution):
 * <pre>{@code
 * <starName>
 * Polity: <polity>
 * <aliasText> (<universeName>)
 * <aliasText> (<universeName>)
 * }</pre>
 *
 * <p>The renderer calls this on every mouse-enter (F1.b dynamic per-hover). Each tooltip
 * string is built fresh from the current alias query result, so universe activation changes
 * surface immediately on the next hover with no cache invalidation needed.
 */
public final class AliasTooltipFormatter {

    private AliasTooltipFormatter() {
    }

    /**
     * Format the star tooltip per F.2 §6.1 format α.
     *
     * @param starName the star's display name (line 1)
     * @param polity   raw polity string; "NA" / null / empty are normalised to "Non-Aligned"
     * @param aliases  alias-display lines to append; empty list → tooltip stops at the polity line
     * @return the multi-line tooltip text
     */
    public static String formatStarTooltip(String starName, String polity, List<AliasDisplay> aliases) {
        // The polity parameter is retained in the signature for backward-compat with existing
        // callers but ignored — the worldbuilding polity field is gone post-normalization, and
        // F.3 will reintroduce a faction line driven by FactionAssignment lookups (separate
        // accessor, separate code path). Aliases append directly to the star name.
        StringBuilder sb = new StringBuilder();
        sb.append(starName == null ? "" : starName);
        appendAliases(sb, aliases);
        return sb.toString();
    }

    /**
     * Format the planet tooltip per F.2 §6.1 format α — preserves existing orbital metadata
     * (semi-major axis, period, radius) and appends alias lines after.
     *
     * @param planetName     the planet's display name (line 1)
     * @param semiMajorAxis  semi-major axis in AU
     * @param orbitalPeriod  orbital period in days
     * @param radius         planet radius in Earth radii
     * @param aliases        alias-display lines to append; empty list → tooltip stops at the
     *                       radius line (legacy planet-tooltip behavior preserved)
     * @return the multi-line tooltip text
     */
    public static String formatPlanetTooltip(String planetName,
                                             double semiMajorAxis,
                                             double orbitalPeriod,
                                             double radius,
                                             List<AliasDisplay> aliases) {
        StringBuilder sb = new StringBuilder();
        sb.append(planetName == null ? "" : planetName);
        sb.append('\n').append(String.format("Semi-major axis: %.3f AU", semiMajorAxis));
        sb.append('\n').append(String.format("Period: %.1f days", orbitalPeriod));
        sb.append('\n').append(String.format("Radius: %.2f Earth", radius));
        appendAliases(sb, aliases);
        return sb.toString();
    }

    /**
     * Format the solar-system view's central/companion star tooltip. Mirrors the existing
     * {@code BodyRenderer.renderStar} format (name + spectral class + distance) and appends
     * alias lines after.
     */
    public static String formatSolarSystemStarTooltip(String starName,
                                                      String spectralClass,
                                                      double distanceLy,
                                                      List<AliasDisplay> aliases) {
        StringBuilder sb = new StringBuilder();
        sb.append(starName == null ? "" : starName);
        sb.append('\n').append("Spectral: ").append(spectralClass == null ? "" : spectralClass);
        sb.append('\n').append(String.format("Distance: %.2f ly", distanceLy));
        appendAliases(sb, aliases);
        return sb.toString();
    }

    // normalisePolity removed by the Worldbuilding Data Model Normalization task. F.3
    // reintroduces faction-name normalisation via FactionAssignment lookups.

    private static void appendAliases(StringBuilder sb, List<AliasDisplay> aliases) {
        if (aliases == null || aliases.isEmpty()) {
            return;
        }
        for (AliasDisplay alias : aliases) {
            sb.append('\n').append(alias.aliasText()).append(" (").append(alias.universeDisplayName()).append(')');
        }
    }

    /**
     * Format aliases as a bullet list for a panel surface (Phase F.2 §6.2 — Fictional Info tab).
     * Each line: {@code "• {aliasText} ({universeDisplayName})"}. Lines joined with {@code \n}.
     *
     * <p>Distinct from {@link #formatStarTooltip} / {@link #formatPlanetTooltip} which embed
     * aliases within a fuller multi-line metadata payload. This method produces JUST the alias
     * block, intended to be assigned to a single multi-line panel Label whose section header
     * lives in its own FXML element.
     *
     * <p>When the alias list is null or empty, returns {@code emptyPlaceholder} as-is — the
     * caller picks the empty-state copy (e.g. "(no aliases — activate a universe to see
     * worldbuilding names)" for the Fictional Info tab).
     *
     * @param aliases           alias-display lines to render as bullets
     * @param emptyPlaceholder  text to return when {@code aliases} is null/empty
     * @return the bullet-list text, or {@code emptyPlaceholder} if no aliases
     */
    public static String formatAliasesAsBulletList(List<AliasDisplay> aliases, String emptyPlaceholder) {
        if (aliases == null || aliases.isEmpty()) {
            return emptyPlaceholder;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aliases.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            AliasDisplay alias = aliases.get(i);
            sb.append("• ").append(alias.aliasText())
                    .append(" (").append(alias.universeDisplayName()).append(')');
        }
        return sb.toString();
    }
}
