package com.teamgannon.trips.dataset.factories;


import com.teamgannon.trips.dataset.model.Link;
import com.teamgannon.trips.dataset.model.Polity;
import com.teamgannon.trips.dataset.model.RouteDescriptor;
import com.teamgannon.trips.dataset.model.Theme;
import com.teamgannon.trips.dialogs.support.Dataset;
import com.teamgannon.trips.file.chview.model.CHViewPreferences;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.file.excel.RBExcelFile;
import com.teamgannon.trips.filedata.model.ChViewRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.repository.AstrographicObjectRepository;
import com.teamgannon.trips.jpa.repository.DataSetDescriptorRepository;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A foctory used to create data descriptors from various formats
 * <p>
 * Created by larrymitchell on 2017-03-29.
 */
@Slf4j
public class DataSetDescriptorFactory {


    /**
     * create a Dataset descriptor for chview files
     *
     * @param dataset                      the descriptor from the user for this dataset
     * @param dataSetDescriptorRepository  the data set repo to save this in
     * @param astrographicObjectRepository the astrographic repo to save it in
     * @param chViewFile                   the ch view files
     * @return a dataset descriptor
     */
    public static DataSetDescriptor createDataSetDescriptor(
            Dataset dataset,
            DataSetDescriptorRepository dataSetDescriptorRepository,
            AstrographicObjectRepository astrographicObjectRepository,
            ChViewFile chViewFile) throws Exception {

        DataSetDescriptor dataSetDescriptor = new DataSetDescriptor();
        dataSetDescriptor.setDataSetName(dataset.getName());
        dataSetDescriptor.setFilePath(dataset.getFileSelected());
        dataSetDescriptor.setFileNotes(dataset.getNotes());
        dataSetDescriptor.setFileCreator(dataset.getAuthor());
        dataSetDescriptor.setDatasetType(dataset.getDataType().getDataFormatEnum().getValue());

        // parse chview file to create the basics for the data set to save
        dataSetDescriptor.setTheme(createTheme("default", chViewFile));

        // now validate whether the dataset actually exists already
        if (dataSetDescriptorRepository.existsById(dataSetDescriptor.getDataSetName())) {
            throw new Exception("This dataset:{" + dataSetDescriptor.getDataSetName() + "} already exists");
        }


        // process all the stellar records from chview and convert to our target objects
        Map<Integer, ChViewRecord> chViewRecordMap = chViewFile.getRecords();
        Map<UUID, AstrographicObject> astrographicObjectMap = new HashMap<>();
        for (Integer recordNumber : chViewRecordMap.keySet()) {
            ChViewRecord chViewRecord = chViewRecordMap.get(recordNumber);
            AstrographicObject astrographicObject = AstrographicObjectFactory.create(dataset, chViewRecord);
            log.info("Star:: name={}, distance={}", astrographicObject.getDisplayName(), astrographicObject.getDistance());
            astrographicObjectMap.put(astrographicObject.getId(), astrographicObject);
        }

        // save the astrographic records
        astrographicObjectRepository.saveAll(astrographicObjectMap.values());
        log.info("Number of records load for file:{} is {}",
                chViewFile.getOriginalFileName(),
                astrographicObjectMap.size());

        // set the records for this
        dataSetDescriptor.setAstrographicDataList(astrographicObjectMap.keySet());
        dataSetDescriptor.setNumberStars((long) astrographicObjectMap.keySet().size());

        // save the data set which is cross referenced to the star records
        dataSetDescriptorRepository.save(dataSetDescriptor);

        log.info("Saved the data set named: {}", dataset);

        return dataSetDescriptor;
    }


    public static DataSetDescriptor createDataSetDescriptor(
            DataSetDescriptorRepository dataSetDescriptorRepository,
            AstrographicObjectRepository astrographicObjectRepository,
            String author,
            RBExcelFile excelFile) throws Exception {

        DataSetDescriptor dataSetDescriptor = new DataSetDescriptor();

        // parse excel file to create the basics for the data set to save
        dataSetDescriptor.setDataSetName(excelFile.getFileName());
        dataSetDescriptor.setFileCreator(author);
        dataSetDescriptor.setTheme(createTheme("excel", excelFile));

        // now validate whether the dataset actually exists already
        if (dataSetDescriptorRepository.existsById(dataSetDescriptor.getDataSetName())) {
            throw new Exception("This dataset:{" + dataSetDescriptor.getDataSetName() + "} already exists");
        }

        astrographicObjectRepository.saveAll(excelFile.getAstrographicObjects());
        log.info("Number of records load for file:{} is {}",
                excelFile.getFileName(),
                excelFile.getAstrographicObjects().size());

        Set<UUID> keySet = excelFile.getAstrographicObjects().stream().map(AstrographicObject::getId).collect(Collectors.toSet());

        // set the records for this
        dataSetDescriptor.setAstrographicDataList(keySet);

        // save the data set which is cross referenced to the star records
        dataSetDescriptorRepository.save(dataSetDescriptor);

        return dataSetDescriptor;
    }


    public static DataSetDescriptor createDataSetDescriptor(DataSetDescriptorRepository dataSetDescriptorRepository,
                                                            RBCsvFile rbCsvFile) throws Exception {
        DataSetDescriptor dataSetDescriptor = new DataSetDescriptor();

        // parse excel file to create the basics for the data set to save
        Dataset dataset = rbCsvFile.getDataset();
        dataSetDescriptor.setDataSetName(dataset.getName());
        dataSetDescriptor.setFilePath(dataset.getFileSelected());
        dataSetDescriptor.setFileCreator(dataset.getAuthor());
        dataSetDescriptor.setNumberStars(rbCsvFile.getSize());
        dataSetDescriptor.setFileNotes(dataset.getNotes());
        dataSetDescriptor.setDatasetType(dataset.getDataType().getDataFormatEnum().getValue());

        Theme theme = new Theme();
        theme.setThemeName("csv");
        dataSetDescriptor.setTheme(theme);

        // now validate whether the dataset actually exists already
        if (dataSetDescriptorRepository.existsById(dataSetDescriptor.getDataSetName())) {
            throw new Exception("This dataset:{" + dataSetDescriptor.getDataSetName() + "} already exists");
        }

        log.info("Number of records load for file:{} is {}",
                dataset.getFileSelected(),
                rbCsvFile.getSize());


        // save the data set which is cross referenced to the star records
        dataSetDescriptorRepository.save(dataSetDescriptor);

        return dataSetDescriptor;
    }

    private static Theme createTheme(String themeName, RBCsvFile rbCsvFile) {
        return extractTheme(themeName, null, CHViewPreferences.earthNormal());
    }

    /**
     * create a theme for the RB Excel format
     *
     * @param themeName the theme name
     * @param excelFile the imported excel file
     * @return the theme
     */
    public static Theme createTheme(String themeName, RBExcelFile excelFile) {
        return extractTheme(themeName, null, CHViewPreferences.earthNormal());
    }


    /**
     * create a theme for CHV files
     *
     * @param themeName  the theme name
     * @param chViewFile the CHV file
     * @return the created theme
     */
    public static Theme createTheme(String themeName, ChViewFile chViewFile) {

        CHViewPreferences vp = chViewFile.getCHViewPreferences();
        return extractTheme(themeName, chViewFile, vp);
    }

    private static Theme extractTheme(String themeName, ChViewFile chViewFile, CHViewPreferences vp) {
        try {
            Theme theme = new Theme();
            theme.setThemeName(themeName);
            theme.setDispStarName(vp.isStarNameOn());
            theme.setViewRadius(vp.getRadius());
            theme.setDisplayScale(vp.isScaleOn());
            theme.setXscale(vp.getXScale());
            theme.setYscale(vp.getYScale());
            theme.setBackColor(setColor(vp.getBackgroundColor()));
            theme.setTextColor(setColor(vp.getTextColor()));

            theme.setCenterX(vp.getCentreOrdinates()[0]);
            theme.setCenterY(vp.getCentreOrdinates()[1]);
            theme.setCenterZ(vp.getCentreOrdinates()[2]);
            theme.setTheta(vp.getTheta());
            theme.setPhi(vp.getPhi());
            theme.setRho(vp.getRho());
            theme.setDisplayGrid(vp.isGridOn());
            theme.setGridSize((int) vp.getGridSize());

            theme.setGridLineColor(setColor(vp.getGridColor()));
            theme.setStemColor(setColor(vp.getStemColor()));
            theme.setStarOutline(vp.isStarOutlineOn());

            theme.setOColor(setColor(vp.getOColor()));
            theme.setBColor(setColor(vp.getBColor()));
            theme.setAColor(setColor(vp.getAColor()));
            theme.setFColor(setColor(vp.getFColor()));
            theme.setGColor(setColor(vp.getGColor()));
            theme.setKColor(setColor(vp.getKColor()));
            theme.setMColor(setColor(vp.getMColor()));
            theme.setXColor(setColor(vp.getXColor()));

            theme.setORad(vp.getORadius());
            theme.setBRad(vp.getBRadius());
            theme.setARad(vp.getARadius());
            theme.setFRad(vp.getFRadius());
            theme.setGRad(vp.getGRadius());
            theme.setKRad(vp.getKRadius());
            theme.setMRad(vp.getMRadius());
            theme.setXRad(vp.getXRadius());
            theme.setDwarfRad(vp.getDwarfRadius());
            theme.setGiantRad(vp.getGiantRadius());
            theme.setSuperGiantRad(vp.getSuperGiantRadius());

            theme.setLinkList(createLinks(chViewFile));

            theme.setRouteDescriptorList(createRouteDescriptorMap(chViewFile));

            theme.setPolities(createPolities(chViewFile));

            return theme;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Color getColor(double[] colors) {
        return Color.color(colors[0], colors[1], colors[2]);
    }

    public static double[] setColor(Color color) {
        double[] colors = new double[3];
        colors[0] = color.getRed();
        colors[1] = color.getGreen();
        colors[2] = color.getBlue();
        return colors;
    }

    /**
     * initial loading
     *
     * @param chViewFile the file with CHView data
     * @return a linked list of so sort of data
     */
    public static List<Link> createLinks(ChViewFile chViewFile) {
        List<Link> linkList = new ArrayList<>();

        return linkList;
    }


    private static Map<UUID, RouteDescriptor> createRouteDescriptorMap(ChViewFile chViewFile) {
        Map<UUID, RouteDescriptor> routeDescriptorMap = new HashMap<>();
        List<com.teamgannon.trips.file.chview.model.RouteDescriptor> routeDescriptors = new ArrayList<>();
        for (com.teamgannon.trips.file.chview.model.RouteDescriptor routeDescriptor : routeDescriptors) {
            RouteDescriptor routeDescriptor1 = new RouteDescriptor();

            // the rest are set by default
            routeDescriptor1.setRouteColor(routeDescriptor.getColor());

            routeDescriptorMap.put(UUID.randomUUID(), routeDescriptor1);
        }

        return routeDescriptorMap;
    }


    private static List<Polity> createPolities(ChViewFile chViewFile) {
        List<Polity> polities = new ArrayList<>();

        return polities;
    }


}
