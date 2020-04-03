package com.teamgannon.trips.algorithms;

/**
 * Created by larrymitchell on 2017-02-02.
 */
public class AstroAlgorithms {


    /**
     * B1950 to J2000   From Explanatory Supplement
     * <p>
     * E.g. the ApJ article gives the first row as
     * 0.9999257079523629, -0.0111789381377700, -0.0048590038153592
     */
    double[][] BtoJ = new double[][]{
            {0.9999256782, -0.0111820611, -0.0048579477},
            {0.0111820610, 0.9999374784, -0.0000271765},
            {0.0048579479, -0.0000271474, 0.9999881997}
    };

    // J2000 to B1950   From Explanatory Supplement
    double[][] JtoB = new double[][]{
            {0.9999256795, 0.0111814828, 0.0048590039},
            {-0.0111814828, 0.9999374849, -0.0000271771},
            {-0.0048590040, -0.0000271557, 0.9999881946}
    };

    /**
     * From J2000 to "galactic coordinates"
     * Spherical Astronomy by Green, equation 14.55, page 355
     */
    double[][] JtoG = new double[][]{
            {-0.054876, -0.873437, -0.483835},
            {0.494109, -0.444830, 0.746982},
            {-0.867666, -0.198076, 0.455984}
    };

    /**
     * Aha, printed good article from ApJ at
     * http://adsabs.harvard.edu/full/1989A&A...218..325M
     * The ApJ flattenedMatrix for J2000 to Galactic agrees with Smart to six digits.
     */
    double[][] JtoGapj = new double[][]{
            {-0.054875529, 0.494109454, -0.867666136},
            {-0.873437105, -0.444829594, -0.198076390},
            {-0.483834992, 0.746982249, 0.455983795}
    };

    /**
     * 17h 45m -29 degrees is about the location of the Galactic Center, new coords.
     * And the Green values give the right answer J => Galactic
     * <p>
     * From http://idlastro.gsfc.nasa.gov/ftp/pro/astro/gal_uvw.pro
     * A_G = [ [ 0.0548755604, +0.4941094279, -0.8676661490], $
     *    [ 0.8734370902, -0.4448296300, -0.1980763734], $
     *    [ 0.4838350155,  0.7469822445, +0.4559837762] ]
     *    This agrees with the ApJ article except for signs of the first column!
     */
    double[][] GtoJ = new double[][]{
            {-0.0548755604, 0.4941094279, -0.8676661490},
            {-0.8734370902, -0.4448296300, -0.1980763734},
            {-0.4838350155, 0.7469822445, 0.4559837762}
    };

    public double[][] makeDirectionCosineMatrix(double a, double b, double c) {
        return null;
    }

}
