package com.terranrepublic.economy;

/**
 * Strong-key commodity definition. Provenance is loose flavor text and is not validated.
 */
public record Commodity(
        String id,
        String name,
        CommodityClass commodityClass,
        double unitMassTons,
        String provenanceNote
) {

    public Commodity {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Commodity id must be provided");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Commodity name must be provided");
        }
        if (unitMassTons < 0) {
            throw new IllegalArgumentException("Commodity unit mass must not be negative");
        }
        commodityClass = commodityClass == null ? CommodityClass.FABRICATED_GOOD : commodityClass;
        provenanceNote = provenanceNote == null ? "" : provenanceNote;
    }
}
