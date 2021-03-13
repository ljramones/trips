package com.teamgannon.trips.dataset.factories;


import com.teamgannon.trips.dataset.model.Link;
import com.teamgannon.trips.dataset.model.Polity;
import com.teamgannon.trips.dataset.model.Theme;
import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.chview.ChViewRecord;
import com.teamgannon.trips.file.chview.model.CHViewPreferences;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.csvin.RegCSVFile;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.DataSetDescriptorRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.routing.RouteDefinition;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
     * @param progressUpdater             an updater for any long loading progress
     * @param dataset                     the descriptor from the user for this dataset
     * @param dataSetDescriptorRepository the data set repo to save this in
     * @param starObjectRepository        the astrographic repo to save it in
     * @param chViewFile                  the ch view files
     * @return a dataset descriptor
     */
    public static @NotNull DataSetDescriptor createDataSetDescriptor(
            @NotNull ProgressUpdater progressUpdater,
            @NotNull Dataset dataset,
            @NotNull DataSetDescriptorRepository dataSetDescriptorRepository,
            @NotNull StarObjectRepository starObjectRepository,
            @NotNull ChViewFile chViewFile) throws Exception {

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
        Map<UUID, StarObject> astrographicObjectMap = new HashMap<>();
        double maxDistance = 0;
        progressUpdater.updateTaskInfo("Saving records in database");
        for (Integer recordId : chViewRecordMap.keySet()) {
            try {

                ChViewRecord chViewRecord = chViewRecordMap.get(recordId);
                // distance check
                double distance = Double.parseDouble(chViewRecord.getDistanceToEarth());
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
                StarObject starObject = new StarObject();
                starObject.fromChvRecord(dataset, chViewRecord);
                astrographicObjectMap.put(starObject.getId(), starObject);
            } catch (Exception e) {
                log.error("failed to translate to star object: {}", chViewRecordMap.get(recordId));
            }
        }

        // save the astrographic records
        starObjectRepository.saveAll(astrographicObjectMap.values());
        String saveMessage = String.format("Number of records loaded for file:%s is %d",
                chViewFile.getOriginalFileName(),
                astrographicObjectMap.size());
        log.info(saveMessage);
        progressUpdater.updateTaskInfo(saveMessage);

        // set the records for this
        dataSetDescriptor.setNumberStars((long) astrographicObjectMap.keySet().size());
        String message = String.format("Dataset :%s has %d stars are range of %.3f",
                dataSetDescriptor.getDataSetName(),
                dataSetDescriptor.getNumberStars(),
                dataSetDescriptor.getDistanceRange());
        log.info(message);
        dataSetDescriptor.setDistanceRange(maxDistance);

        // save the data set which is cross referenced to the star records
        dataSetDescriptorRepository.save(dataSetDescriptor);

        log.info("Saved the data set descriptor named: {}", dataset);
        progressUpdater.updateTaskInfo(message);

        return dataSetDescriptor;
    }

    public static DataSetDescriptor createDataSetDescriptor(@NotNull DataSetDescriptorRepository dataSetDescriptorRepository,
                                                            @NotNull RegCSVFile regCSVFile) throws Exception {

        DataSetDescriptor dataSetDescriptor = regCSVFile.getDataSetDescriptor();

        Theme theme = new Theme();
        theme.setThemeName("csv");
        dataSetDescriptor.setTheme(theme);

        // now validate whether the dataset actually exists already
        if (dataSetDescriptorRepository.existsById(dataSetDescriptor.getDataSetName())) {
            throw new Exception("This dataset:{" + dataSetDescriptor.getDataSetName() + "} already exists");
        }

        log.info("Number of records load for file:{} is {}",
                regCSVFile.getDataset().getFileSelected(),
                regCSVFile.getSize());


        // save the data set which is cross referenced to the star records
        dataSetDescriptorRepository.save(dataSetDescriptor);

        return dataSetDescriptor;
    }

    /**
     * create a theme for CHV files
     *
     * @param themeName  the theme name
     * @param chViewFile the CHV file
     * @return the created theme
     */
    public static @Nullable Theme createTheme(String themeName, @NotNull ChViewFile chViewFile) {

        CHViewPreferences vp = chViewFile.getCHViewPreferences();
        return extractTheme(themeName, chViewFile, vp);
    }

    private static @Nullable Theme extractTheme(String themeName, ChViewFile chViewFile, @NotNull CHViewPreferences vp) {
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


    public static @NotNull Color getColor(double[] colors) {
        return Color.color(colors[0], colors[1], colors[2]);
    }

    public static double @NotNull [] setColor(@NotNull Color color) {
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
    public static @NotNull List<Link> createLinks(ChViewFile chViewFile) {
        List<Link> linkList = new ArrayList<>();

        return linkList;
    }


    private static @NotNull Map<UUID, RouteDefinition> createRouteDescriptorMap(ChViewFile chViewFile) {
        Map<UUID, RouteDefinition> routeDescriptorMap = new HashMap<>();
        List<com.teamgannon.trips.file.chview.model.RouteDescriptor> routeDescriptors = new ArrayList<>();
        for (com.teamgannon.trips.file.chview.model.RouteDescriptor routeDescriptor : routeDescriptors) {
            RouteDefinition routeDefinition1 = new RouteDefinition();

            // the rest are set by default
            routeDefinition1.setRouteColor(routeDescriptor.getColor());

            routeDescriptorMap.put(UUID.randomUUID(), routeDefinition1);
        }

        return routeDescriptorMap;
    }


    private static @NotNull List<Polity> createPolities(ChViewFile chViewFile) {
        List<Polity> polities = new ArrayList<>();

        return polities;
    }


}
