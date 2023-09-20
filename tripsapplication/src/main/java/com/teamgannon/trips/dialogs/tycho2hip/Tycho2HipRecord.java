package com.teamgannon.trips.dialogs.tycho2hip;

import lombok.Data;

@Data
public class Tycho2HipRecord {

    /**
     * The Tycho-2 identifier (TYC2)
     */
    long id;

    /**
     * tyc: The Tycho-2 ID, with leading zeros removed from the first and second portion (for consistency with Gaia linking tables)
     */
    String tyc;
    /**
     * gaia: The Gaia Data Release 3 ID.
     */
    String gaia;

    /**
     * hyg: The HYG main catalog ID from HYG v3.
     */
    String hyg;

    /**
     * hip: The HIPPARCOS ID, from HYG if known, otherwise Tycho-2.
     */
    String hip;

    /**
     * hd: The Henry Draper (HD) catalog ID, from HYG if known, otherwise Tycho-2.
     */
    String hd;

    /**
     * hr: The Harvard / Yale Bright Star Catalog ID, from HYG.
     */
    String hr;

    /**
     * gl: The Gliese Catalog ID, from HYG.
     */
    String gl;

    /**
     * bayer: The Bayer (Greek letter) designation, from HYG
     */
    String bayer;

    /**
     * flam: The Flamsteed number, from HYG
     */
    String flam;

    /**
     * con: The three-letter constellation abbreviation, from HYG
     */
    String con;

    /**
     * proper: A proper name for the star, from HYG
     */
    String proper;

    /**
     * ra: Right ascension (epoch + equinox 2000.0), in hours, from HYG or TYC
     */
    double ra;

    /**
     * dec: Declination (epoch + equinox 2000.0), in degrees, from HYG or TYC
     */
    double dec;

    /**
     * pos_src: Indicator of source for the position fields ra and dec (see below)
     */
    String pos_src;

    /**
     * dist: Distance from Sol in parsecs. From Gaia if known, otherwise HYG.
     */
    double dist;

    /**
     * x0
     */
    double x0;

    /**
     * y0
     */
    double y0;

    /**
     * z0: These three fields are Cartesian coordinates. The directions are such that x is towards RA 0, Dec 0, y towards RA 6 hr., Dec 0, and z towards Dec 90 degrees.
     */
    double z0;

    /**
     * dist_src: Indicator of source for the distance fields dist, x0, y0, z0 (see below). x0, y0, and z0 also depend on ra and dec, so they will also be determined by the position source. An extremely common combination is raw distance from Gaia but the position from TYC.
     */
    String dist_src;

    /**
     * mag: V or VT magnitude for the star
     */
    double mag;

    /**
     * absmag: Corresponding absolute magnitude
     */
    double absmag;

    /**
     * mag_src: Indicator of source for the magnitude field mag (see below). absmag depends on both apparent magnitude and distance, so may be determined by values from two sources.
     */
    String mag_src;

}
