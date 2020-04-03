package com.teamgannon.trips.algorithms.time;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.temporal.JulianFields;

/**
 * Used to calculate various Julian time periods
 * <p>
 * Created by larrymitchell on 2017-02-04.
 */
@Slf4j
@Data
public class JulianDate {

    /**
     * julian day from January 1, 4713 BC
     */
    private double julianDay;

    private JulianFields julianFields;

    public double getReducedJulianDay() {
        // offset from 12h Nov 16, 1858
        return julianDay - 2400000;
    }

    /**
     * get the Modified Julian Date (MJD)
     * <p>
     * The Modified Julian Date (MJD) was introduced by the Smithsonian Astrophysical Observatory in 1957
     * to record the orbit of Sputnik via an IBM 704 (36-bit machine) and using only 18 bits until
     * August 7, 2576. MJD is the epoch of VAX/VMS and its successor OpenVMS, using 63-bit date/time,
     * which allows times to be stored up to July 31, 31086, 02:48:05.47. The MJD has a starting
     * point of midnight on November 17, 1858 and is computed by MJD = JD - 2400000.5
     *
     * @return the modified julian day
     */
    public double getModifiedJulianDay() {
        // offset from 0h Nov 17, 1858

        return julianDay - 2400000.5;
    }

    /**
     * get the Truncated Julian Day (TJD)
     * <p>
     * The Truncated Julian Day (TJD) was introduced by NASA/Goddard in 1979 as part of a parallel grouped
     * binary time code (PB-5) "designed specifically, although not exclusively, for spacecraft applications."
     * TJD was a 4-digit day count from MJD 40000, which was May 24, 1968, represented as a 14-bit binary number.
     * Since this code was limited to four digits, TJD recycled to zero on MJD 50000, or October 10, 1995,
     * "which gives a long ambiguity period of 27.4 years". (NASA codes PB-1—PB-4 used a 3-digit day-of-year
     * count.) Only whole days are represented. Time of day is expressed by a count of seconds of a day,
     * plus optional milliseconds, microseconds and nanoseconds in separate fields. Later PB-5J was
     * introduced which increased the TJD field to 16 bits, allowing values up to 65535, which will occur
     * in the year 2147. There are five digits recorded after TJD 9999
     *
     * @return Truncated Julian Day
     */
    public double getTruncatedJulianDay() {
        // offset from 0h May 24, 1968
        return Math.floor(julianDay - 2440000.5);
    }

    /**
     * get the Dublin Julian Date (DJD)
     * The Dublin Julian Date (DJD) is the number of days that has elapsed since the epoch of the solar and
     * lunar ephemerides used from 1900 through 1983, Newcomb's Tables of the Sun and Ernest W. Brown's
     * Tables of the Motion of the Moon (1919). This epoch was noon UT on January 0, 1900, which is the
     * same as noon UT on December 31, 1899. The DJD was defined by the International Astronomical Union
     * at their meeting in Dublin, Ireland, in 1955.
     * @return
     */
    public double getDublinJulianDay() {
        // offset from 12h Dec 31, 1899
        return julianDay - 2415020;
    }

    public double getCNESJulianDay() {
        // from 0h Jan 1, 1950
        return julianDay - 2433282.5;
    }

    public double getCCSDSJulianDay() {
        // offset from 0h Jan 1, 1958
        return julianDay - 2436204.5;
    }

    public double getLOPJulianDay() {
        // offset from 0h Jan 1, 1992
        return julianDay - 2448622.5;
    }


}
