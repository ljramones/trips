package com.teamgannon.trips.astrogation.util;

/**
 * Epoch transformation
 * <p>
 * This code takes the right ascension and declination in degrees, as well as the Julian Dates for Epoch 1 and Epoch 2, and returns the transformed coordinates.
 * <p>
 * Please note that this is a simplified model of precession that doesn't account for changes in declination, nutation, and other factors that might affect the
 * transformation. For more precise calculations, you may want to use a specialized astronomical library like Orekit or refer to the algorithms in the
 * "Explanatory Supplement to the Astronomical Almanac."
 * <p>
 * /**
 * * Transforming coordinates between different epochs requires accounting for the precession of the Earth's axis.
 * * Precession is the slow, continuous change in the orientation of an astronomical body's rotational axis.
 * * For Earth, this leads to a gradual shift in the equatorial coordinate system over time.
 * * <p>
 * * The transformation between epochs can be done using precession formulas. Here's a general outline
 * * of how you can perform this transformation:
 * * <h3>Precession from Epoch 1 to Epoch 2</h3>
 * * Given right ascension (\( \alpha \)) and declination (\( \delta \)) at Epoch 1, you can calculate
 * * the corresponding coordinates at Epoch 2 using the following steps:
 * * <ol>
 * *   <li><strong>Calculate the Precession Angle:</strong> Determine the amount of precession between Epoch 1
 * *   and Epoch 2 using the formula:
 * *   \[
 * *   p = (t_2 - t_1) \times 5028.796195''
 * *   \]
 * *   where \( t_1 \) and \( t_2 \) are the Julian centuries from J2000.0 for Epoch 1 and Epoch 2, respectively:
 * *   \[
 * *   t = \frac{{\text{{JD}} - 2451545.0}}{{36525}}
 * *   \]
 * *   </li>
 * *   <li><strong>Apply the Precession Transformation:</strong> Use the precession angle to transform the coordinates:
 * *   \[
 * *   \begin{align*}
 * *   \alpha_2 & = \alpha_1 + p \\
 * *   \delta_2 & = \delta_1
 * *   \end{align*}
 * *   \]
 * *   </li>
 * * </ol>
 *
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
public class EpochTransformation {

    public static double[] precessCoordinates(double raDegrees, double decDegrees, double jd1, double jd2) {
        // Calculate the Julian centuries from J2000.0 for each epoch
        double t1 = (jd1 - 2451545.0) / 36525.0;
        double t2 = (jd2 - 2451545.0) / 36525.0;

        // Calculate the precession angle in arcseconds and convert to degrees
        double precessionAngle = (t2 - t1) * 5028.796195 / 3600.0;

        // Apply the precession transformation
        double raDegrees2 = raDegrees + precessionAngle;
        double decDegrees2 = decDegrees; // Declination remains unchanged

        // Normalize the right ascension to [0, 360]
        raDegrees2 = (raDegrees2 + 360) % 360;

        return new double[]{raDegrees2, decDegrees2};
    }

}
