package com.teamgannon.trips.file.csvin;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.teamgannon.trips.jpa.model.ExoPlanet;

import java.io.FileReader;
import java.io.IOException;

public class ExoPlanetCsvReader {


    public void loadCSVData(String filePath) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            try {
                reader.readNext();  // skip header line

                while ((line = reader.readNext()) != null) {
                    ExoPlanet exoplanet = new ExoPlanet();

                    // planet properties
                    exoplanet.setName(line[1]);
                    exoplanet.setPlanetStatus(line[2]);
                    exoplanet.setMass(line[3] != null && !line[3].isEmpty() ? Double.parseDouble(line[3]) : null);
                    exoplanet.setMassSini(line[6] != null && !line[6].isEmpty() ? Double.parseDouble(line[6]) : null);
                    exoplanet.setRadius(line[9] != null && !line[9].isEmpty() ? Double.parseDouble(line[9]) : null);
                    exoplanet.setOrbitalPeriod(line[12] != null && !line[12].isEmpty() ? Double.parseDouble(line[12]) : null);
                    exoplanet.setSemiMajorAxis(line[15] != null && !line[15].isEmpty() ? Double.parseDouble(line[15]) : null);
                    exoplanet.setEccentricity(line[18] != null && !line[18].isEmpty() ? Double.parseDouble(line[18]) : null);
                    exoplanet.setInclination(line[21] != null && !line[21].isEmpty() ? Double.parseDouble(line[21]) : null);
                    exoplanet.setAngularDistance(line[24] != null && !line[24].isEmpty() ? Double.parseDouble(line[24]) : null);
                    exoplanet.setDiscovered(line[25] != null && !line[25].isEmpty() ? Integer.parseInt(line[25]) : null);
                    exoplanet.setUpdated(line[26]);
                    exoplanet.setOmega(line[27] != null && !line[27].isEmpty() ? Double.parseDouble(line[27]) : null);
                    exoplanet.setTperi(line[30] != null && !line[30].isEmpty() ? Double.parseDouble(line[30]) : null);
                    exoplanet.setTconj(line[33] != null && !line[33].isEmpty() ? Double.parseDouble(line[33]) : null);
                    exoplanet.setTzeroTr(line[36] != null && !line[36].isEmpty() ? Double.parseDouble(line[36]) : null);
                    exoplanet.setTzeroTrSec(line[39] != null && !line[39].isEmpty() ? Double.parseDouble(line[39]) : null);
                    exoplanet.setLambdaAngle(line[42] != null && !line[42].isEmpty() ? Double.parseDouble(line[42]) : null);
                    exoplanet.setImpactParameter(line[45] != null && !line[45].isEmpty() ? Double.parseDouble(line[45]) : null);
                    exoplanet.setTzeroVr(line[48] != null && !line[48].isEmpty() ? Double.parseDouble(line[48]) : null);
                    exoplanet.setK(line[51] != null && !line[51].isEmpty() ? Double.parseDouble(line[51]) : null);
                    exoplanet.setTempCalculated(line[54] != null && !line[54].isEmpty() ? Double.parseDouble(line[54]) : null);
                    exoplanet.setTempMeasured(line[57] != null && !line[57].isEmpty() ? Double.parseDouble(line[57]) : null);
                    exoplanet.setHotPoIntegerLon(line[58] != null && !line[58].isEmpty() ? Double.parseDouble(line[58]) : null);
                    exoplanet.setGeometricAlbedo(line[59] != null && !line[59].isEmpty() ? Double.parseDouble(line[59]) : null);
                    exoplanet.setLogG(line[62] != null && !line[62].isEmpty() ? Double.parseDouble(line[62]) : null);
                    exoplanet.setPublication(line[63]);
                    exoplanet.setDetectionType(line[64]);
                    exoplanet.setMassDetectionType(line[65]);
                    exoplanet.setRadiusDetectionType(line[66]);
                    exoplanet.setAlternateNames(line[67]);
                    exoplanet.setMolecules(line[68]);

                    // star properties
                    exoplanet.setStarName(line[69]);
                    exoplanet.setRa(line[70] != null && !line[70].isEmpty() ? Double.parseDouble(line[70]) : null);
                    exoplanet.setDec(line[71] != null && !line[71].isEmpty() ? Double.parseDouble(line[71]) : null);
                    exoplanet.setMagV(line[72] != null && !line[72].isEmpty() ? Double.parseDouble(line[72]) : null);
                    exoplanet.setMagI(line[73] != null && !line[73].isEmpty() ? Double.parseDouble(line[73]) : null);
                    exoplanet.setMagJ(line[74] != null && !line[74].isEmpty() ? Double.parseDouble(line[74]) : null);
                    exoplanet.setMagH(line[75] != null && !line[75].isEmpty() ? Double.parseDouble(line[75]) : null);
                    exoplanet.setMagK(line[76] != null && !line[76].isEmpty() ? Double.parseDouble(line[76]) : null);
                    exoplanet.setStarDistance(line[77] != null && !line[77].isEmpty() ? Double.parseDouble(line[77]) : null);
                    exoplanet.setStarMetallicity(line[80] != null && !line[80].isEmpty() ? Double.parseDouble(line[80]) : null);
                    exoplanet.setStarMass(line[83] != null && !line[83].isEmpty() ? Double.parseDouble(line[83]) : null);
                    exoplanet.setStarRadius(line[86] != null && !line[86].isEmpty() ? Double.parseDouble(line[86]) : null);
                    exoplanet.setStarSpType(line[89]);
                    exoplanet.setStarAge(line[90] != null && !line[90].isEmpty() ? Double.parseDouble(line[90]) : null);
                    exoplanet.setStarTeff(line[93] != null && !line[93].isEmpty() ? Double.parseDouble(line[93]) : null);
                    exoplanet.setStarDetectedDisc(Boolean.parseBoolean(line[96]));  // Assuming this is a String, adjust if different type
                    exoplanet.setStarMagneticField(line[97]);  // Assuming this is a String, adjust if different type
                    exoplanet.setStarAlternateNames(line[98]);
                }

            } catch (CsvValidationException e) {
                throw new RuntimeException(e);


            }
        }
    }
}
