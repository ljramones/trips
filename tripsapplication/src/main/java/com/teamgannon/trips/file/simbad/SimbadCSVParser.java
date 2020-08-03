package com.teamgannon.trips.file.simbad;

import com.opencsv.CSVReader;
import com.teamgannon.trips.jpa.model.SimbadEntry;
import com.teamgannon.trips.jpa.repository.SimbadEntryRepository;
import com.teamgannon.trips.service.DatabaseImportStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to parse the simbad entry
 * <p>
 * Created by larrymitchell on 2017-02-24.
 */
@Slf4j
@Component
public class SimbadCSVParser {

    private final SimbadEntryRepository simbadEntryRepository;

    public SimbadCSVParser(SimbadEntryRepository simbadEntryRepository) {
        this.simbadEntryRepository = simbadEntryRepository;
    }

    /**
     * import a simbad file
     *
     * @param dataFile the data file
     */
    public void importFile(File dataFile) {

        log.info("attempting to import simbad database file from:" + dataFile.getAbsolutePath());

        DatabaseImportStatus databaseImportStatus = new DatabaseImportStatus();

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(dataFile.getAbsolutePath()));
            String[] nextLine;
            // skip first line
            reader.readNext();

            List<SimbadEntry> entityList = new ArrayList<>();

            int index = 1;
            long startTime = System.currentTimeMillis();
            while ((nextLine = reader.readNext()) != null) {
                SimbadParseResult parseResult = parseSimbadEntry(nextLine);
                databaseImportStatus.incTotal();
                if (parseResult.isSuccess()) {
                    databaseImportStatus.incRecordsSuccess();
                    entityList.add(parseResult.getSimbadEntry());
                } else {
                    databaseImportStatus.incRecordsFailed();
                    databaseImportStatus.addId(parseResult.getIdProcessed());
                }

                if (Math.floorMod(index, 10000) == 0) {
                    bulkSave(entityList);
                    entityList.clear();
                    log.info("Processing 10000 catalog records");
                }
                index++;
            }
            if (!entityList.isEmpty()) {
                bulkSave(entityList);
                entityList.clear();
            }
            long endTime = System.currentTimeMillis();
            databaseImportStatus.setProcessingTime((endTime - startTime) / 1000);

        } catch (FileNotFoundException e) {
            log.error("failed to open the file name:" + dataFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("failed to read a record");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  report the results
        log.info("done");
    }

    private void bulkSave(List<SimbadEntry> simbadEntryList) {

        log.debug("save 1000 simbad elements");
        simbadEntryRepository.saveAll(simbadEntryList);
    }

    /**
     * parse a simbad entry
     *
     * @param nextLine a single line
     * @return the parse result
     */
    private SimbadParseResult parseSimbadEntry(String[] nextLine) {

        SimbadParseResult result = new SimbadParseResult();
        SimbadEntry simbadEntry = new SimbadEntry();
        try {
            int recordNumber = Integer.parseInt(nextLine[0]);
            String identifier = nextLine[1];
            String oType = nextLine[2];
            String galacticLong = nextLine[3];
            String galacticLat = nextLine[4];
            double parallaxes = Double.parseDouble(nextLine[5]);
            double magU = Double.parseDouble(nextLine[6]);
            double magB = Double.parseDouble(nextLine[7]);
            double magV = Double.parseDouble(nextLine[8]);
            double magR = Double.parseDouble(nextLine[9]);
            double magI = Double.parseDouble(nextLine[10]);
            String spectral = nextLine[11];

            int numPar = Integer.parseInt(nextLine[12]);
            int numChild = Integer.parseInt(nextLine[13]);
            int numSiblings = Integer.parseInt(nextLine[14]);

            // create the star definition
            simbadEntry.setRecordNumber(recordNumber);
            simbadEntry.setIdentifier(identifier);
            simbadEntry.setOType(oType);
            simbadEntry.setGalacticLong(galacticLong);
            simbadEntry.setGalacticLat(galacticLat);
            simbadEntry.setParallaxes(parallaxes);
            simbadEntry.setMagU(magU);
            simbadEntry.setMagB(magB);
            simbadEntry.setMagV(magV);
            simbadEntry.setMagR(magR);
            simbadEntry.setMagI(magI);
            simbadEntry.setSpectralType(spectral);

            simbadEntry.setNumPar(numPar);
            simbadEntry.setNumChild(numChild);
            simbadEntry.setNumSiblings(numSiblings);

            result.setSuccess(true);
            result.setSimbadEntry(simbadEntry);

        } catch (Exception e) {
            log.error("failed to parse csv record for incoming data file because of " + e);
            log.error("simbad-definition:" + simbadEntry.toString());
        }
        return result;
    }

}
