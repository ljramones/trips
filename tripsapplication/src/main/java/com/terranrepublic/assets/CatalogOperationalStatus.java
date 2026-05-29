package com.terranrepublic.assets;

/**
 * Documentary / lifecycle status of a catalogued station from the catalog's point of view.
 *
 * <p>This is <strong>distinct from</strong> {@link OperationalState}, which describes the
 * <em>physical condition</em> of the asset ({@code OPERATIONAL}, {@code DAMAGED},
 * {@code DERELICT}, {@code WRECK}, {@code UNDER_CONSTRUCTION}, {@code SALVAGED}). Two different
 * facts about the same station — neither redundant — per the v2 design doc §4.3.
 *
 * <p>Worked examples illustrating the distinction:
 * <table>
 *   <caption>OperationalState vs CatalogOperationalStatus</caption>
 *   <tr><th>Station</th><th>OperationalState</th><th>CatalogOperationalStatus</th></tr>
 *   <tr><td>Mir</td><td>SALVAGED (deorbited)</td><td>HISTORIC (was real, no longer extant)</td></tr>
 *   <tr><td>Babylon 5</td><td>OPERATIONAL (in its setting)</td><td>FICTIONAL (from a TV show)</td></tr>
 *   <tr><td>Death Star</td><td>OPERATIONAL (in its setting)</td><td>FICTIONAL</td></tr>
 *   <tr><td>Lunar Gateway</td><td>UNDER_CONSTRUCTION</td><td>PLANNED</td></tr>
 * </table>
 */
public enum CatalogOperationalStatus {

    /** Real station that existed, flew, and was retired/destroyed. Example: Skylab, Mir, Salyut series. */
    HISTORIC,

    /** Real station currently operational. Example: ISS, Tiangong. */
    ACTIVE,

    /** Real station planned or under construction. Example: Lunar Gateway, Axiom Station. */
    PLANNED,

    /** Real station planned but cancelled. Example: Space Station Freedom, Skylab B. */
    CANCELLED,

    /** In-universe station from any fictional setting. Example: Babylon 5, Death Star, the Citadel. */
    FICTIONAL,

    /** Status not yet determined. */
    UNKNOWN
}
