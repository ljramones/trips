package com.teamgannon.trips.jpa.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Embeddable component containing all catalog identifiers for a star.
 * These are various astronomical catalog IDs that identify the same star
 * across different astronomical databases.
 */
@Getter
@Setter
@Embeddable
public class StarCatalogIds implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The Simbad id
     */
    private String simbadId = "";

    /**
     * The Bayer catalog id.
     * Validated by the BayerChecker based on greek letter for first part
     * and genitive form of the constellation for the second part.
     */
    private String bayerCatId = "";

    /**
     * The Gliese catalog id - GJ
     */
    private String glieseCatId = "";

    /**
     * The Hipparcos catalog id - HIP
     */
    private String hipCatId = "";

    /**
     * The Henry Draper catalog id - HD
     */
    private String hdCatId = "";

    /**
     * The Flamsteed catalog id.
     * Format has a number as the first part and the genitive form
     * of the constellation as the second part.
     */
    private String flamsteedCatId = "";

    /**
     * The Tycho 2 catalog id - TYC2
     */
    private String tycho2CatId = "";

    /**
     * The Gaia DR2 catalog id
     */
    private String gaiaDR2CatId = "";

    /**
     * The Gaia DR3 catalog id
     */
    private String gaiaDR3CatId = "";

    /**
     * The Gaia EDR3 catalog id
     */
    private String gaiaEDR3CatId = "";

    /**
     * The 2MASS catalog id
     */
    private String twoMassCatId = "";

    /**
     * The CSI catalog id
     */
    private String csiCatId = "";

    /**
     * Comma-separated list of all catalog IDs.
     * One object has names in many catalogs.
     */
    @Lob
    private String catalogIdList = "";

    /**
     * Get the raw catalog ID list string.
     *
     * @return the raw comma-separated string
     */
    public String getRawCatalogIdList() {
        return catalogIdList;
    }

    /**
     * Get the catalog IDs as a parsed list.
     *
     * @return list of catalog IDs
     */
    public List<String> getCatalogIdListParsed() {
        if (catalogIdList == null || catalogIdList.isEmpty() || "NA".equals(catalogIdList)) {
            return new ArrayList<>();
        }
        return Arrays.asList(catalogIdList.split("\\s*,\\s*"));
    }

    /**
     * Check if this star has a specific catalog entry.
     *
     * @param catalogPrefix the catalog prefix to search for (e.g., "HIP", "HD", "GJ")
     * @return true if the catalog ID list contains the prefix
     */
    public boolean hasCatalogEntry(String catalogPrefix) {
        return catalogIdList != null && catalogIdList.contains(catalogPrefix);
    }

    /**
     * Initialize all fields to default empty values.
     */
    public void initDefaults() {
        simbadId = "";
        bayerCatId = "";
        glieseCatId = "";
        hipCatId = "";
        hdCatId = "";
        flamsteedCatId = "";
        tycho2CatId = "";
        gaiaDR2CatId = "";
        gaiaDR3CatId = "";
        gaiaEDR3CatId = "";
        twoMassCatId = "";
        csiCatId = "";
        catalogIdList = "NA";
    }
}
