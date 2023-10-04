package com.teamgannon.trips.dialogs.exoplanets;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class LoadExoPlanetsCSVFile {

    public List<ExoPlanet> loadFile(File csvFile) {
        List<ExoPlanet> records = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] line;
            boolean isFirstLine = true;

            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // skip header line
                }

                ExoPlanet exoPlanet = new ExoPlanet();

                // Set the fields of exoPlanet based on the values in data
                exoPlanet.setName(getDataAtIndex(line, 0, true)); // set name
                exoPlanet.setPlanetStatus(getDataAtIndex(line, 1, true)); // set planetStatus if not empty
                exoPlanet.setMass(safeParseDouble(getDataAtIndex(line, 2, false))); // set mass if not empty
                exoPlanet.setMassSini(safeParseDouble(getDataAtIndex(line, 5, false))); // set massSini if not empty
                exoPlanet.setRadius(safeParseDouble(getDataAtIndex(line, 8, false))); // set radius if not empty
                exoPlanet.setOrbitalPeriod(safeParseDouble(getDataAtIndex(line, 11, false))); // set orbitalPeriod if not empty
                exoPlanet.setSemiMajorAxis(safeParseDouble(getDataAtIndex(line, 14, false))); // set semiMajorAxis if not empty
                exoPlanet.setEccentricity(safeParseDouble(getDataAtIndex(line, 17, false))); // set eccentricity if not empty
                exoPlanet.setInclination(safeParseDouble(getDataAtIndex(line, 20, false))); // set inclination if not empty
                exoPlanet.setAngularDistance(safeParseDouble(getDataAtIndex(line, 23, false))); // set angularDistance if not empty
                exoPlanet.setDiscovered(safeParseInt(getDataAtIndex(line, 24, false))); // set discovered if not empty
                exoPlanet.setUpdated(getDataAtIndex(line, 25, true)); // set updated if not empty
                exoPlanet.setOmega(safeParseDouble(getDataAtIndex(line, 26, false))); // set omega if not empty
                exoPlanet.setTperi(safeParseDouble(getDataAtIndex(line, 29, false))); // set tperi if not empty
                exoPlanet.setTconj(safeParseDouble(getDataAtIndex(line, 32, false))); // set tconj if not empty
                exoPlanet.setTzeroTr(safeParseDouble(getDataAtIndex(line, 35, false))); // set tzeroTr if not empty
                exoPlanet.setTzeroTrSec(safeParseDouble(getDataAtIndex(line, 38, false))); // set tzeroTrSec if not empty
                exoPlanet.setLambdaAngle(safeParseDouble(getDataAtIndex(line, 41, false))); // set lambdaAngle if not empty
                exoPlanet.setImpactParameter(safeParseDouble(getDataAtIndex(line, 44, false))); // set impactParameter if not empty
                exoPlanet.setTzeroVr(safeParseDouble(getDataAtIndex(line, 47, false))); // set tzeroVr if not empty
                exoPlanet.setK(safeParseDouble(getDataAtIndex(line, 50, false))); // set k if not empty
                exoPlanet.setTempCalculated(safeParseDouble(getDataAtIndex(line, 53, false))); // set tempCalculated if not empty
                exoPlanet.setTempMeasured(safeParseDouble(getDataAtIndex(line, 56, false))); // set tempMeasured if not empty
                exoPlanet.setHotPoIntegerLon(safeParseDouble(getDataAtIndex(line, 57, false))); // set hotPointLon if not empty
                exoPlanet.setGeometricAlbedo(safeParseDouble(getDataAtIndex(line, 58, false))); // set geometricAlbedo if not empty
                exoPlanet.setLogG(safeParseDouble(getDataAtIndex(line, 61, false))); // set logG if not empty
                exoPlanet.setPublication(getDataAtIndex(line, 62, true)); // set publication
                exoPlanet.setDetectionType(getDataAtIndex(line, 63, false)); // set detectionType
                exoPlanet.setMassDetectionType(getDataAtIndex(line, 64, true)); // set massDetectionType
                exoPlanet.setRadiusDetectionType(getDataAtIndex(line, 65, true)); // set radiusDetectionType
                exoPlanet.setAlternateNames(getDataAtIndex(line, 66, true)); // set alternateNames
                exoPlanet.setMolecules(getDataAtIndex(line, 67, true)); // set molecules

                // host star properties
                exoPlanet.setStarName(getDataAtIndex(line, 68, true)); // set starName
                exoPlanet.setRa(safeParseDouble(getDataAtIndex(line, 69, false))); // set ra if not empty
                exoPlanet.setDec(safeParseDouble(getDataAtIndex(line, 70, false))); // set dec if not empty
                exoPlanet.setMagV(safeParseDouble(getDataAtIndex(line, 71, false))); // set magV if not empty
                exoPlanet.setStarDistance(safeParseDouble(getDataAtIndex(line, 76, false))); // set starDistance if not empty
                exoPlanet.setStarMetallicity(safeParseDouble(getDataAtIndex(line, 79, false))); // set starMetallicity if not empty
                exoPlanet.setStarMass(safeParseDouble(getDataAtIndex(line, 82, false))); // set starMass if not empty
                exoPlanet.setStarRadius(safeParseDouble(getDataAtIndex(line, 85, false))); // set starRadius if not empty
                exoPlanet.setStarSpType(getDataAtIndex(line, 88, true)); // set starSpType
                exoPlanet.setStarAge(safeParseDouble(getDataAtIndex(line, 89, false))); // set starAge if not empty
                exoPlanet.setStarTeff(safeParseDouble(getDataAtIndex(line, 92, false))); // set starTeff if not empty
                exoPlanet.setStarDetectedDisc(safeParseBoolean(getDataAtIndex(line, 95, false))); // set starDetectedDisc if not empty
                exoPlanet.setStarMagneticField(getDataAtIndex(line, 96, true)); // set starMagneticField
                exoPlanet.setStarAlternateNames(getDataAtIndex(line, 97, true)); // set starAlternateNames

                records.add(exoPlanet); // add exoPlanet to records

            }
        } catch (IOException | CsvValidationException e) {
            log.error("Failed to read file: {}, because of {}", csvFile, e.getMessage());
        }
        return records;
    }

    /**
     * Utility method to safely get data at a specific index
     *
     * @param data         the data array
     * @param index        the index to get
     * @param removeQuotes should we remove quotes
     * @return the data at the index
     */
    private static String getDataAtIndex(String[] data, int index, boolean removeQuotes) {
        if (index < data.length && data[index] != null) {
            return removeQuotes ? removeQuotes(data[index]) : data[index];
        }
        return ""; // or return a default value if preferred
    }

    private static String removeQuotes(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        input = input.replaceFirst("^\"+", "").trim();
        input = input.replaceFirst("\"+$", "").trim();
        return input;
    }


    /**
     * safe parse a double value from a string and give default values if it fails
     *
     * @param s the string to parse
     * @return the double value
     */
    private double safeParseDouble(String s) {
        try {
            if (s.isEmpty()) {
                return 0.0;
            } else {
                return Double.parseDouble(s);
            }
        } catch (NumberFormatException e) {
            log.error("Failed to parse double: {}, because of {}", s, e.getMessage());
            return 0.0;
        }
    }

    /**
     * safe parse an integer valuue form a string and give default values if it fails
     *
     * @param s the string to parse
     * @return the integer value
     */
    private int safeParseInt(String s) {
        try {
            if (s.isEmpty()) {
                return 0;
            } else {
                return Integer.parseInt(s);
            }
        } catch (NumberFormatException e) {
            log.error("Failed to parse int: {}, because of {}", s, e.getMessage());
            return 0;
        }
    }

    /**
     * safe parse a boolean value from a string and give default values if it fails to parse
     *
     * @param s the string to parse
     * @return the boolean value
     */
    private boolean safeParseBoolean(String s) {
        return !s.isEmpty() && Boolean.parseBoolean(s);
    }

}
