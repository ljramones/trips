package com.teamgannon.trips.astrogation.timesystems;

import lombok.extern.slf4j.Slf4j;
import org.orekit.time.*;


@Slf4j
public class TimeTransformation {

    public AbsoluteDate getAbsoluteDate(int year, int month, int day, int hour, int minute, float second) {
        UTCScale utc = TimeScalesFactory.getUTC();
        AbsoluteDate dateUTC = new AbsoluteDate(year, month, day, hour, minute, second, utc);
        log.info("UTC Date: " + dateUTC);
        return dateUTC;
    }

    public AbsoluteDate getInternationalAtomicTime(int year, int month, int day, int hour, int minute, float second) {
        TAIScale tai = TimeScalesFactory.getTAI();
        AbsoluteDate dateTAI = new AbsoluteDate(year, month, day, hour, minute, second, tai);
        log.info("TAI Date: " + dateTAI);
        return dateTAI;
    }

    public AbsoluteDate getTerrestialTime(int year, int month, int day, int hour, int minute, float second) {
        TTScale tt = TimeScalesFactory.getTT();
        AbsoluteDate dateTT = new AbsoluteDate(year, month, day, hour, minute, second, tt);
        log.info("TT Date: " + dateTT);
        return dateTT;
    }

    /**
     * Convert from UTC to TDB
     * <p>
     * TDB (Barycentric Dynamical Time) is a time scale used for calculations related to the solar system's barycenter.
     * It's a relativistic coordinate time scale, taking into account the gravitational time dilation caused by the Sun.
     * The method convertFromUTCtoTDB takes a DateComponents object (representing the date) and secondsInDayUTC
     * (representing the time of day in seconds). It then converts the given UTC date and time to TDB and returns
     * the corresponding AbsoluteDate in TDB.
     * <p>
     * Note: The conversion from UTC to TDB in this example involves using the offset from TAI to TDB (offsetFromTAI)
     * and the offset from UTC to TAI (offsetToTAI). This is because TDB is usually expressed relative to TAI, and
     * TAI is a common intermediary when converting between time scales.
     *
     * @param dateComponents  the date components
     * @param secondsInDayUTC the seconds in day utc
     * @return the absolute date
     */
    public AbsoluteDate convertFromUTCtoTDB(DateComponents dateComponents, double secondsInDayUTC) {
        UTCScale utc = TimeScalesFactory.getUTC();
        TDBScale tdb = TimeScalesFactory.getTDB();

        // Convert secondsInDayUTC to TimeComponents
        TimeComponents timeComponents = new TimeComponents((int) secondsInDayUTC);

        // Convert DateComponents and TimeComponents to AbsoluteDate (in UTC)
        AbsoluteDate dateUTC = new AbsoluteDate(dateComponents, timeComponents, TimeScalesFactory.getUTC());

        // Calculate the difference between UTC and TDB for the given date
        double offsetSeconds = tdb.offsetFromTAI(dateUTC) - utc.offsetToTAI(dateComponents, timeComponents);

        // Convert UTC to TDB
        AbsoluteDate dateTDB = dateUTC.shiftedBy(offsetSeconds);

        System.out.println("Converted to TDB: " + dateTDB);

        return dateTDB;
    }

    /**
     * Convert from UTC to TCG
     * <p>
     * TCG (Geocentric Coordinate Time) is a relativistic coordinate time scale used for timekeeping within the
     * Earth's gravitational field. It accounts for the gravitational time dilation effects of Earth.
     * <p>
     * The method convertFromUTCtoTCG takes a DateComponents object (representing the date) and secondsInDayUTC
     * (representing the time of day in seconds). It then converts the given UTC date and time to TCG and
     * returns the corresponding AbsoluteDate in TCG.
     * <p>
     * Note: Similar to the TDB conversion, the conversion from UTC to TCG in this example involves using the
     * offset from TAI to TCG (offsetFromTAI) and the offset from UTC to TAI (offsetToTAI). This is because
     * TCG is usually expressed relative to TAI, and TAI is a common intermediary when converting between time scales.
     *
     * @param dateComponents  the date
     * @param secondsInDayUTC the time of day in seconds
     * @return the absolute date in TCG
     */
    public AbsoluteDate convertFromUTCtoTCG(DateComponents dateComponents, double secondsInDayUTC) {
        UTCScale utc = TimeScalesFactory.getUTC();
        TCGScale tcg = TimeScalesFactory.getTCG();

        // Convert secondsInDayUTC to TimeComponents
        TimeComponents timeComponents = new TimeComponents((int) secondsInDayUTC);

        // Convert DateComponents and TimeComponents to AbsoluteDate (in UTC)
        AbsoluteDate dateUTC = new AbsoluteDate(dateComponents, timeComponents, TimeScalesFactory.getUTC());

        // Calculate the difference between UTC and TCG for the given date
        double offsetSeconds = tcg.offsetFromTAI(dateUTC) - utc.offsetToTAI(dateComponents, timeComponents);

        // Convert UTC to TCG
        AbsoluteDate dateTCG = dateUTC.shiftedBy(offsetSeconds);

        System.out.println("Converted to TCG: " + dateTCG);

        return dateTCG;
    }

    /**
     * Convert from UTC to GPST
     * GPST (Global Positioning System Time) is the time scale used by the GPS system. It is based on atomic time
     * and does not include leap seconds, which means it has a fixed offset from UTC.
     * <p>
     * The method convertFromUTCtoGPST takes a DateComponents object (representing the date) and secondsInDayUTC
     * (representing the time of day in seconds). It then converts the given UTC date and time to GPST and returns
     * the corresponding AbsoluteDate in GPST.
     * <p>
     * Note: The conversion from UTC to GPST in this example involves using the offset from TAI to GPST
     * (offsetFromTAI) and the offset from UTC to TAI (offsetToTAI). This is because GPST is usually expressed
     * relative to TAI, and TAI is a common intermediary when converting between time scales.
     *
     * @param dateComponents  the date
     * @param secondsInDayUTC the time of day in seconds
     * @return the absolute date in GPST
     */
    public AbsoluteDate convertFromUTCtoGPST(DateComponents dateComponents, double secondsInDayUTC) {
        UTCScale utc = TimeScalesFactory.getUTC();
        GPSScale gps = TimeScalesFactory.getGPS();

        // Convert secondsInDayUTC to TimeComponents
        TimeComponents timeComponents = new TimeComponents((int) secondsInDayUTC);

        // Convert DateComponents and TimeComponents to AbsoluteDate (in UTC)
        AbsoluteDate dateUTC = new AbsoluteDate(dateComponents, timeComponents, TimeScalesFactory.getUTC());

        // Calculate the difference between UTC and GPST for the given date
        double offsetSeconds = gps.offsetFromTAI(dateUTC) - utc.offsetToTAI(dateComponents, timeComponents);

        // Convert UTC to GPST
        AbsoluteDate dateGPST = dateUTC.shiftedBy(offsetSeconds);

        System.out.println("Converted to GPST: " + dateGPST);

        return dateGPST;
    }


    public AbsoluteDate convertFromUTCtoTAI(DateComponents dateComponents, double secondsInDayUTC) {
        UTCScale utc = TimeScalesFactory.getUTC();

        // Convert secondsInDayUTC to TimeComponents
        TimeComponents timeComponents = new TimeComponents((int) secondsInDayUTC);

        // Get the offset using DateComponents and timeComponents
        double offsetSeconds = utc.offsetToTAI(dateComponents, timeComponents);

        // Convert DateComponents and TimeComponents to AbsoluteDate and then apply the offset
        AbsoluteDate dateUTC = new AbsoluteDate(dateComponents, timeComponents, TimeScalesFactory.getUTC());
        AbsoluteDate dateConvertedToTAI = dateUTC.shiftedBy(offsetSeconds);

        System.out.println("Converted to TAI: " + dateConvertedToTAI);

        return dateConvertedToTAI;
    }

    public AbsoluteDate convertUTCtoTAI(AbsoluteDate dateUTC) {
        UTCScale utc = TimeScalesFactory.getUTC();

        DateComponents dateComponents = dateUTC.getComponents(utc).getDate();
        TimeComponents timeComponents = dateUTC.getComponents(utc).getTime();

        double offsetSeconds = utc.offsetToTAI(dateComponents, timeComponents);
        return dateUTC.shiftedBy(offsetSeconds);
    }


    public AbsoluteDate convertUTCtoTT(AbsoluteDate dateUTC) {
        TTScale tt = TimeScalesFactory.getTT();
        double offsetSeconds = tt.offsetFromTAI(dateUTC);
        return convertUTCtoTAI(dateUTC).shiftedBy(offsetSeconds);
    }

    public AbsoluteDate convertUTCtoTDB(AbsoluteDate dateUTC) {
        TDBScale tdb = TimeScalesFactory.getTDB();
        double offsetSeconds = tdb.offsetFromTAI(dateUTC);
        return convertUTCtoTAI(dateUTC).shiftedBy(offsetSeconds);
    }

    public AbsoluteDate convertUTCtoTCG(AbsoluteDate dateUTC) {
        TCGScale tcg = TimeScalesFactory.getTCG();
        double offsetSeconds = tcg.offsetFromTAI(dateUTC);
        return convertUTCtoTAI(dateUTC).shiftedBy(offsetSeconds);
    }

    public AbsoluteDate convertUTCtoGPST(AbsoluteDate dateUTC) {
        GPSScale gps = TimeScalesFactory.getGPS();
        double offsetSeconds = gps.offsetFromTAI(dateUTC);
        return convertUTCtoTAI(dateUTC).shiftedBy(offsetSeconds);
    }

    public AbsoluteDate convertTAItoUTC(AbsoluteDate dateTAI) {
        UTCScale utc = TimeScalesFactory.getUTC();

        DateComponents dateComponents = dateTAI.getComponents(TimeScalesFactory.getTAI()).getDate();
        TimeComponents timeComponents = dateTAI.getComponents(TimeScalesFactory.getTAI()).getTime();

        double offsetSeconds = -utc.offsetToTAI(dateComponents, timeComponents);
        return dateTAI.shiftedBy(offsetSeconds);
    }


    public AbsoluteDate convertTAItoTT(AbsoluteDate dateTAI) {
        TTScale tt = TimeScalesFactory.getTT();
        double offsetSeconds = tt.offsetFromTAI(dateTAI);
        return dateTAI.shiftedBy(offsetSeconds);
    }

    public AbsoluteDate convertTAItoTDB(AbsoluteDate dateTAI) {
        TDBScale tdb = TimeScalesFactory.getTDB();
        double offsetSeconds = tdb.offsetFromTAI(dateTAI);
        return dateTAI.shiftedBy(offsetSeconds);
    }

    public AbsoluteDate convertTAItoTCG(AbsoluteDate dateTAI) {
        TCGScale tcg = TimeScalesFactory.getTCG();
        double offsetSeconds = tcg.offsetFromTAI(dateTAI);
        return dateTAI.shiftedBy(offsetSeconds);
    }

    public AbsoluteDate convertTAItoGPST(AbsoluteDate dateTAI) {
        GPSScale gps = TimeScalesFactory.getGPS();
        double offsetSeconds = gps.offsetFromTAI(dateTAI);
        return dateTAI.shiftedBy(offsetSeconds);
    }


}
