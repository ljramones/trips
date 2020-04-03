package com.teamgannon.trips.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.teamgannon.trips.dataset.factories.DataSetDescriptorFactory;
import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
import com.teamgannon.trips.elasticsearch.model.DataSetDescriptor;
import com.teamgannon.trips.elasticsearch.model.Star;
import com.teamgannon.trips.elasticsearch.model.StellarSystem;
import com.teamgannon.trips.elasticsearch.repository.AstrographicObjectRepository;
import com.teamgannon.trips.elasticsearch.repository.DataSetDescriptorRepository;
import com.teamgannon.trips.elasticsearch.repository.StarRepository;
import com.teamgannon.trips.elasticsearch.repository.StellarSystemRepository;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.excel.model.RBExcelFile;
import com.teamgannon.trips.file.excel.model.RBStar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Used to manage larger
 * <p>
 * Created by larrymitchell on 2017-01-20.
 */
@Slf4j
@Service
public class DatabaseManagementService {

    private static final int MAX_REQUEST_SIZE = 9999;

    @Autowired
    private StellarSystemRepository stellarSystemRepository;

    @Autowired
    private StarRepository starRepository;

    @Autowired
    private DataSetDescriptorRepository dataSetDescriptorRepository;

    @Autowired
    private AstrographicObjectRepository astrographicObjectRepository;

    /**
     * drop all the tables
     */
    public void dropDatabase() {
        log.info("Dropping database");
        starRepository.deleteAll();
        stellarSystemRepository.deleteAll();
    }

    /**
     * import a data file into the database
     *
     * @param databasefile the database file
     */
    public void importDatabase(File databasefile) {
        log.info("attempting to import database file from:" + databasefile.getAbsolutePath());

        DatabaseImportStatus databaseImportStatus = new DatabaseImportStatus();

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(databasefile.getAbsolutePath()));
            String[] nextLine;

            // skip first line
            reader.readNext();

            List<Star> starList = new ArrayList<>();
            List<StellarSystem> stellarSystemList = new ArrayList<>();

            int index = 1;
            long startTime = System.currentTimeMillis();
            while ((nextLine = reader.readNext()) != null) {
                ParseResult parseResult = saveStarSystemData(nextLine);
                databaseImportStatus.incTotal();
                if (parseResult.isSuccess()) {
                    databaseImportStatus.incRecordsSuccess();
                    starList.add(parseResult.getStar());
                    System.out.println("star name: " + parseResult.getStar().getCommonName() +
                            ", distance:{}" + parseResult.getStar().getDistance());
                    stellarSystemList.add(parseResult.getStellarSystem());
                } else {
                    databaseImportStatus.incRecordsFailed();
                    databaseImportStatus.addId(parseResult.getIdProcessed());
                }

                if (Math.floorMod(index, 10000) == 0) {
                    bulkSave(starList, stellarSystemList);
                    starList.clear();
                    stellarSystemList.clear();
                    log.info("Processing 10000 catalog records");
                }
                index++;
            }
            if (!starList.isEmpty()) {
                bulkSave(starList, stellarSystemList);
                starList.clear();
                stellarSystemList.clear();
            }
            long endTime = System.currentTimeMillis();
            databaseImportStatus.setProcessingTime((endTime - startTime) / 1000);

        } catch (FileNotFoundException e) {
            log.error("failed to open the file name:" + databasefile.getAbsolutePath());
        } catch (IOException e) {
            log.error("failed to read a record");
        }

        //  report the results
        log.info(databaseImportStatus.toString());
    }

    /**
     * export the database file
     *
     * @param dataBaseFile the database file
     */
    public void exportDatabase(File dataBaseFile) {
        log.info("attempting to export database file to:" + dataBaseFile.getAbsolutePath());

        // open a file for writing csv data
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(dataBaseFile.getAbsolutePath()), '\n');

            Iterable<Star> starDefinitions = starRepository.findAll();

            writer.writeNext(setupHeaderLine());
            for (Star starDefinition : starDefinitions) {

                String[] recordLine = getLine(starDefinition);
                writer.writeNext(recordLine);
            }
            // close the file before returning
            writer.close();
        } catch (IOException e) {
            log.error("caught error opening the file");
        }

    }

    /**
     * setup the header line so we can write it out
     *
     * @return the headers
     */
    private String[] setupHeaderLine() {
        return new String[0];
    }

    /**
     * setup an output line
     *
     * @param starDefinition the starfinition
     * @return an output line
     */
    private String[] getLine(Star starDefinition) {
        return new String[0];
    }


    /**
     * parse and save a record line
     *
     * @param nextLine the line to parse
     * @return parse result
     */
    private ParseResult saveStarSystemData(String[] nextLine) {

        ParseResult result = new ParseResult();
        long catalogId = -1;
        StellarSystem stellarSystem = new StellarSystem();
        Star star = new Star();
        try {
            catalogId = Long.parseLong(nextLine[0]);
            String hip = nextLine[1];
            String hd = nextLine[2];
            String hr = nextLine[3];
            String gl = nextLine[4];
            String bf = nextLine[5];
            String proper = nextLine[6];
            double ra = Double.parseDouble(nextLine[7]);
            double dec = Double.parseDouble(nextLine[8]);
            double dist = Double.parseDouble(nextLine[9]);
            double pmra = Double.parseDouble(nextLine[10]);
            double pmdec = Double.parseDouble(nextLine[11]);
            double rv = Double.parseDouble(nextLine[12]);
            double mag = Double.parseDouble(nextLine[13]);
            double absmag = Double.parseDouble(nextLine[14]);
            String spect = nextLine[15];
            String ci = nextLine[16];
            double x = Double.parseDouble(nextLine[17]);
            double y = Double.parseDouble(nextLine[18]);
            double z = Double.parseDouble(nextLine[19]);
            double vx = Double.parseDouble(nextLine[20]);
            double vy = Double.parseDouble(nextLine[21]);
            double vz = Double.parseDouble(nextLine[22]);
            double rarad = Double.parseDouble(nextLine[23]);
            double decrad = Double.parseDouble(nextLine[24]);
            double pmrarad = Double.parseDouble(nextLine[25]);
            double prdecrad = Double.parseDouble(nextLine[26]);
            String bayer = nextLine[27];
            String flam = nextLine[28];
            String con = nextLine[29];
            String comp = nextLine[30];
            String comp_primary = nextLine[31];
            String base = nextLine[32];
            String lum = nextLine[33];
            String var = nextLine[34];
            String var_min = nextLine[34];
            String var_max = nextLine[35];

            // create the star definition
            star.setCatalogId(catalogId);
            star.setHipparcosId(hip);
            star.setHarvardRevisedId(hd);
            star.setHenryDraperId(hr);
            star.setGlieseId(gl);
            star.setBayerFlamsteed(bf);
            star.setCommonName(proper);
            star.setRa(ra);
            star.setDec(dec);
            star.setDistance(dist);
            star.setProperMotionRightAscension(pmra);
            star.setProperMotionDeclination(pmdec);
            star.setRadialVelocity(rv);
            star.setMagnitude(mag);
            star.setAbsoluteVisualMagnitude(absmag);
            star.setSpectralType(spect);
            star.setColorIndex(ci);
            star.setX(x);
            star.setY(y);
            star.setZ(z);
            star.setXVelocity(vx);
            star.setYVelocity(vy);
            star.setZVelocity(vz);
            star.setRarad(rarad);
            star.setDecrad(decrad);
            star.setPmrarad(pmrarad);
            star.setPrdecrad(prdecrad);
            star.setBayer(bayer);
            star.setFlam(flam);
            star.setConstellation(con);
            star.setComp(comp);
            star.setComp_primary(comp_primary);
            star.setBase(base);
            star.setLum(lum);
            star.setVar(var);
            star.setVar_min(var_min);
            star.setVar_max(var_max);

            UUID systemId = java.util.UUID.randomUUID();

            star.setId(systemId.toString());
            star.setStellarSystemId(systemId.toString());

            stellarSystem.addStar(star.getId());
            stellarSystem.setId(systemId.toString());

            result.setSuccess(true);
            result.setIdProcessed(catalogId);
            result.setStar(star);
            result.setStellarSystem(stellarSystem);

        } catch (Exception e) {
            log.error("failed to parse csv record for incoming data file because of " + e);
            log.error("stardefinition:" + star.toString());
        }
        return result;
    }

    private void bulkSave(List<Star> starList, List<StellarSystem> stellarSystemList) {

        log.debug("save 1000 star elements and 1000 stellar system elements");
        starRepository.save(starList);

        stellarSystemRepository.save(stellarSystemList);


    }

    public void loadCHFile(ChViewFile chViewFile) throws Exception {

        // this method call actually saves the dataset in elasticsearch
        DataSetDescriptor dataSetDescriptor = DataSetDescriptorFactory.createDataSetDescriptor(
                dataSetDescriptorRepository,
                astrographicObjectRepository,
                "anonymous",
                chViewFile
        );
    }


    public void loadRBStarSet(RBExcelFile excelFile) throws Exception {

        // this method call actually saves the dataset in elasticsearch
        DataSetDescriptor dataSetDescriptor = DataSetDescriptorFactory.createDataSetDescriptor(
                dataSetDescriptorRepository,
                astrographicObjectRepository,
                "anonymous",
                excelFile
        );

    }

    /**
     * get the data sets
     *
     * @return the list of all descriptors in the database
     */
    public List<DataSetDescriptor> getDataSetIds() {
        Iterable<DataSetDescriptor> dataSetDescriptors = dataSetDescriptorRepository.findAll();
        List<DataSetDescriptor> descriptors = new ArrayList<>();
        dataSetDescriptors.forEach(descriptors::add);
        return descriptors;
    }

    public List<AstrographicObject> getFromDataset(DataSetDescriptor dataSetDescriptor) {
        // we can only effectively gather 500 at a time
        return toList(astrographicObjectRepository.findByIdIn(dataSetDescriptor.getAstrographicDataList(), new PageRequest(0, MAX_REQUEST_SIZE)));
    }

    public List<AstrographicObject> getFromDatasetWithinLimit(DataSetDescriptor dataSetDescriptor, double distance) {
        // we can only effectively gather 500 at a time
        return toList(astrographicObjectRepository.findByDistanceIsLessThan(distance, new PageRequest(0, MAX_REQUEST_SIZE)));
    }

    public DataSetDescriptor getDatasetFromName(String dataSetName) {
        return dataSetDescriptorRepository.findByDataSetName(dataSetName);
    }

    //////////////

    /**
     * helper method to return page as list
     *
     * @param pageResult the page result
     * @return the list representation
     */
    private List<AstrographicObject> toList(Page<AstrographicObject> pageResult) {
        return pageResult.getContent();
    }


}
