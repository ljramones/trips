package com.terranrepublic.assets;

/**
 * Where a spaceship design comes from: an actual built vehicle, a proposed/theoretical concept, or a
 * fictional ship from a particular series.
 * <p>
 * For {@link #SCIENCE_FICTION} the specific franchise (Foundation, Dune, The Expanse, Halo, the Terran
 * Republic, ...) is carried in the free-text {@code series} field of {@link com.terranrepublic.assets.SpaceshipDesign};
 * for {@link #REAL} and {@link #PROPOSED} that field may optionally name the agency or program.
 * {@link #UNKNOWN} is the default for designs that have not been classified yet.
 */
public enum SourceType {

    /** A real, built (or flown) spacecraft. */
    REAL("Real"),

    /** A proposed or theoretical concept that has not been built. */
    PROPOSED("Proposed"),

    /** A fictional ship; the {@code series} field names the franchise. */
    SCIENCE_FICTION("Science Fiction"),

    /** Not yet classified. */
    UNKNOWN("Unknown");

    private final String label;

    SourceType(String label) {
        this.label = label;
    }

    /** @return a human-readable label for menus and tables */
    public String label() {
        return label;
    }
}
