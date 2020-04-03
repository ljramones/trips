package com.teamgannon.trips.algorithms;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Stores equatorial coordinates and contains some utilities to convert decimal degrees to strings and vice-versa.
 */
@Slf4j
@Data
public class Coordinates {

    /**
     * Right ascension in arcsec
     */
    private double raDec;

    /**
     * the raDec in string form
     */
    private String raStr;

    /**
     * Declination in arcsec
     */
    private double decDec;

    /**
     * the declination in string form
     */
    private String decStr;

    /**
     * Class constructor. Processes the input strings and calculates right ascension and declination.
     *
     * @param raDec  Right ascension (string).
     * @param decDec Declination (string).
     */
    public Coordinates(String raDec, String decDec) {
        this.raStr = raDec;
        this.raDec = convertRa(raDec);

        this.decStr = decDec;
        this.decDec = convertDec(decDec);
    }


    /**
     * Class constructor.
     *
     * @param raDec  Right ascension (decimal degrees).
     * @param decDec Declination (decimal degrees).
     */
    public Coordinates(double raDec, double decDec) {
        this.raDec = raDec;
        this.raStr = convertRa(raDec);

        this.decDec = decDec;
        this.decStr = convertDec(decDec);
    }

    private String convertDec(double dec) {
        return decToDms(dec);
    }

    private String decToDms(double dec) {
        int sign = dec < -1 ? -1 : 1;
        dec = Math.abs(dec);
        int degree = (int) Math.floor(dec);
        double minutes = (dec - degree) * 60;
        double seconds = (minutes - Math.floor(minutes)) * 60;
        return (sign * degree) + " " + (int) minutes + " " + String.format("%.2f", seconds);
    }

    private String convertRa(double ra) {
        return decToDms(ra);
    }


    private double convertDec(String dec) throws NumberFormatException {
        return dmsToDec(dec);
    }

    private double convertRa(String ra) {
        return dmsToDec(ra);
    }

    private double dmsToDec(String value) {
        // format is xx xx xx.x or xx xx xx
        String[] valStr = value.split(" ");
        double degree = Double.parseDouble(valStr[0]);
        double sign = degree < -1 ? -1 : 1;
        return sign * ((Math.abs(degree))
                + Double.parseDouble(valStr[1]) / 60
                + Double.parseDouble(valStr[2]) / 3600);
    }

    public String toString() {
        return "RA: " + getRaStr() + " Dec: " + getDecStr();
    }

    // rz_dms = 147 44 06 and dec_dms = -74 52 39,

    public static void main(String[] args) {

        String ra = "147 44 06";
        String dec = "-74 52 39";
        Coordinates coordinates = new Coordinates(ra, dec);

        Coordinates coordinates1 = new Coordinates(coordinates.getRaDec(), coordinates.getDecDec());


        log.info("Right Ascension of {} = {}, Declination of {} = {}", ra, coordinates.getRaDec(), dec, coordinates.getDecDec());

    }

}