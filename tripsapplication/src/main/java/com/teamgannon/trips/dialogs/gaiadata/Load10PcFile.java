package com.teamgannon.trips.dialogs.gaiadata;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.teamgannon.trips.astrogation.Coordinates.calculateEquatorialCoordinates;
import static com.teamgannon.trips.astrogation.Coordinates.parsecToLightYears;

@Slf4j
public class Load10PcFile {

    /**
     * load the file
     *
     * @param file the file name
     * @return the list of records
     */
    public List<StarRecord> loadFile(File file) {
        // String fileName = "tablea1.dat";

        List<StarRecord> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                StarRecord record = new StarRecord();

                record.setSeq(safeParseInt(line.substring(0, 4).trim()));
                record.setSys(safeParseInt(line.substring(5, 9).trim()));
                record.setSystemName(line.substring(10, 39).trim());
                record.setObjType(line.substring(40, 46).trim());
                record.setObjName(line.substring(47, 76).trim());
                record.setRAdeg(safeParseDouble(line.substring(78, 91)));
                record.setDEdeg(safeParseDouble(line.substring(93, 106)));
                record.setEpoch(safeParseDouble(line.substring(107, 113)));
                record.setPlx(masToParsecs(safeParseDouble(line.substring(114, 122))));
                record.setDistance(parsecToLightYears(record.getPlx()));
                double[] coord = calculateEquatorialCoordinates(record.getRAdeg(), record.getDEdeg(), record.getDistance());
                record.setCoordinates(coord);
                log.info("name: {} ra: {} dec: {} distance: {} x: {} y: {} z: {}",
                        record.getObjName(),
                        record.getRAdeg(),
                        record.getDEdeg(),
                        record.getDistance(),
                        coord[0],
                        coord[1],
                        coord[2]);
                record.setE_plx(safeParseDouble(line.substring(123, 131)));
                record.setR_plx(line.substring(132, 163).trim());
                record.setPmRA(safeParseDouble(line.substring(164, 180)));
                record.setPmDE(safeParseDouble(line.substring(181, 197)));
                record.setE_pmRA(safeParseDouble(line.substring(198, 214)));
                record.setE_pmDE(safeParseDouble(line.substring(215, 231)));
                record.setR_pmDE(line.substring(232, 262).trim());
                record.setRV(safeParseDouble(line.substring(263, 271)));
                record.setE_RV(safeParseDouble(line.substring(272, 280)));
                record.setR_RV(line.substring(281, 300).trim());
                record.setSpType(line.substring(301, 309).trim());
                record.setR_SpType(line.substring(310, 335).trim());
                record.setSpMethod(line.substring(336, 348).trim());
                record.setGCode(safeParseInt(line.substring(349, 351)));
                record.setGmag(safeParseDouble(line.substring(352, 361)));
                record.setGest(safeParseDouble(line.substring(362, 368)));
                record.setGBPmag(safeParseDouble(line.substring(369, 378)));
                record.setGRPmag(safeParseDouble(line.substring(379, 388)));
                record.setUmag(safeParseDouble(line.substring(389, 396)));
                record.setBmag(safeParseDouble(line.substring(397, 404)));
                record.setVmag(safeParseDouble(line.substring(405, 412)));
                record.setRmag(safeParseDouble(line.substring(413, 420)));
                record.setImag(safeParseDouble(line.substring(421, 428)));
                record.setJmag(safeParseDouble(line.substring(429, 436)));
                record.setHmag(safeParseDouble(line.substring(437, 444)));
                record.setKsmag(safeParseDouble(line.substring(445, 452)));
                record.setR_Sys(line.substring(453, 474).trim());
                record.setNexopl(safeParseInt(line.substring(475, 476)));
                record.setGaiaDR2(safeParseInt(line.substring(477, 496)));
                record.setGaiaEDR3(safeParseInt(line.substring(497, 516)));
                record.setSIMBAD(line.substring(517, 543).trim());
                record.setCommon(line.substring(544, 561).trim());
                record.setGJ(line.substring(562, 572).trim());
                record.setHD(line.substring(573, 585).trim());
                record.setHIP(line.substring(586, 596).trim());
                record.setCom(line.substring(597, 945).trim());

                // Print or process the record as needed
                records.add(record);
            }
            return records;
        } catch (IOException e) {
            System.err.println("error reading file: " + file);
            return new ArrayList<>();
        }
    }

    public double masToParsecs(double Plx) {
        if (Plx == 0) {
            return 0;  // To avoid division by zero
        }
        return 1000 / Plx;
    }

    public double safeParseDouble(String str) {
        str = str.trim();
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0;  // Return zero if the string is not a number
        }
    }

    public int safeParseInt(String str) {
        str = str.trim();
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;  // Return zero if the string is not a number
        }
    }


}
