package com.teamgannon.trips.file.chview;

import com.teamgannon.trips.file.chview.model.CHViewPreferences;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.chview.model.PseudoString;
import com.teamgannon.trips.file.chview.model.StringResult;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import com.teamgannon.trips.stellarmodelling.StarColor;
import com.teamgannon.trips.stellarmodelling.StellarClassification;
import com.teamgannon.trips.stellarmodelling.StellarFactory;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.EndianUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Used to read the CHView file format
 * <p>
 * Created by larrymitchell on 2017-02-07.
 */
@Slf4j
@Component
public class ChviewReader {

    /**
     * the stellar factory
     */
    private final @NotNull StellarFactory stellarFactory;


    /**
     * the complete file content
     */
    private byte[] fileContent;

    /**
     * the current index in the file
     */
    private int currentIndex = 0;

    /**
     * the number of records parsed so far
     */
    private int recordNumber = 0;

    /**
     * dependency injection for component
     */
    public ChviewReader() {
        this.stellarFactory = new StellarFactory();
    }

    /**
     * load a ch view file
     *
     * @param file the chview file
     * @return a chview file
     */
    public @Nullable ChViewFile loadFile(@NotNull ProgressUpdater progressUpdater, @NotNull File file) {
        return readCompleteFile(progressUpdater, file);
    }

    /**
     * read the complete file
     *
     * @param progressUpdater the updater
     * @param inputFile       the file name to read
     */
    private @Nullable ChViewFile readCompleteFile(@NotNull ProgressUpdater progressUpdater, @NotNull File inputFile) {

        try {
            fileContent = Files.readAllBytes(inputFile.toPath());
            return parsefile(progressUpdater, inputFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("load failed for file because of:" + e);
            return null;
        }
    }


    /**
     * relies on the file being read into a byte buffer
     */
    private @Nullable ChViewFile parsefile(@NotNull ProgressUpdater progressUpdater, String fileName) {
        progressUpdater.updateTaskInfo("reading front preamble");
        ChViewFile chViewFile = new ChViewFile();
        currentIndex = 0;
        chViewFile.setOriginalFileName(fileName);

        // get the file version
        int versionNumber = readInt(fileContent, currentIndex);
        chViewFile.setFileVersion(versionNumber);
        currentIndex += 4;

        // get the view preferences
        CHViewPreferences CHViewPreferences = parseViewPreferences(fileContent);
        chViewFile.setCHViewPreferences(CHViewPreferences);

        // get the number of records
        short numberOfRecords = readShort(fileContent, currentIndex);
        currentIndex += 2;
        chViewFile.setNumberOfRecords(numberOfRecords);

        // read each file record
        progressUpdater.updateTaskInfo("reading records");
        for (int i = 0; i < numberOfRecords; i++) {
            ChViewRecord chViewRecord = parseRecord(fileContent, currentIndex, progressUpdater);
            if (chViewRecord == null) {
                continue;
            }
            chViewFile.addRecord(chViewRecord);
            if (i % 100 == 0) {
                progressUpdater.updateTaskInfo("read " + i + " records");
            }
        }

        parseLinks(fileContent, currentIndex);

        chViewFile.setComments(parsePreamble(fileContent, currentIndex));

        // return the JSON file
        progressUpdater.updateTaskInfo("file read complete");
        return chViewFile;
    }

    private void parseLinks(byte @NotNull [] fileContent, int index) {

        if (fileContent.length < index) {
            short numberOfLinks = readShort(fileContent, index);
            currentIndex += 2;

            for (int i = 0; i < numberOfLinks; i++) {
                // read the link type
                short linkType = readShort(fileContent, currentIndex);
                currentIndex += 2;

                // the destination
                StringResult destination = readString(fileContent, currentIndex);
                currentIndex += destination.getIndexAdd();
            }
        } else {
            log.debug("Reached end of file ->  no links");
        }

    }

    /**
     * parse the preamble
     *
     * @param fileContent the file content
     * @param index       the current index
     */

    private String parsePreamble(byte @NotNull [] fileContent, int index) {
        if (fileContent.length < index) {
            StringResult fileComments = readString(fileContent, currentIndex);
            return fileComments.getValue();
        } else {
            log.debug("Reached end of file ->  no links");
            return "no file comments";
        }
    }

    /**
     * parse the preferences section
     *
     * @param buffer the file content buffer
     * @return the View Preferences object
     */
    private @NotNull CHViewPreferences parseViewPreferences(byte[] buffer) {
        CHViewPreferences CHViewPreferences = new CHViewPreferences();

        // boolean
        boolean grid = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setGridOn(grid);

        StringResult gridSize = readString(buffer, currentIndex);
        currentIndex += gridSize.getIndexAdd();
        CHViewPreferences.setGridSize(Double.parseDouble(gridSize.getValue()));

        // boolean value
        boolean link = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setLinkOn(link);

        // boolean value
        boolean linkNumbers = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setDisplayLinkOn(linkNumbers);

        StringResult linkSize1 = readString(buffer, currentIndex);
        currentIndex += linkSize1.getIndexAdd();
        StringResult linkSize2 = readString(buffer, currentIndex);
        currentIndex += linkSize2.getIndexAdd();
        StringResult linkSize3 = readString(buffer, currentIndex);
        currentIndex += linkSize3.getIndexAdd();
        StringResult linkSize4 = readString(buffer, currentIndex);
        currentIndex += linkSize4.getIndexAdd();
        CHViewPreferences.setLinkSizes(
                Double.parseDouble(linkSize1.getValue()),
                Double.parseDouble(linkSize2.getValue()),
                Double.parseDouble(linkSize3.getValue()),
                Double.parseDouble(linkSize4.getValue())
        );

        // boolean
        boolean starName = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setStarNameOn(starName);

        StringResult radius = readString(buffer, currentIndex);
        currentIndex += radius.getIndexAdd();
        CHViewPreferences.setRadius(Double.parseDouble(radius.getValue()));

        // boolean
        boolean scale = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setScaleOn(scale);

        short gridStyle = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setGridStyle(gridStyle);

        short linkStyle1 = readShort(buffer, currentIndex);
        currentIndex += 2;
        short linkStyle2 = readShort(buffer, currentIndex);
        currentIndex += 2;
        short linkStyle3 = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setLinkStyles(linkStyle1, linkStyle2, linkStyle3);

        short stemStyle = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setStemStyle(stemStyle);

        // boolean
        boolean starOutline = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setStarOutlineOn(starOutline);

        //boolean
        boolean routeDisplay = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setRouteDisplayOn(routeDisplay);

        // we throw away the next 8 bytes since it is just 0's
        currentIndex += 8;

        // get rid of 24 extra bytes
        currentIndex += 24;


        byte[] oColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setOColor(oColor);

        byte[] bColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setBColor(bColor);

        byte[] aColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setAColor(aColor);

        byte[] fColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setFColor(fColor);

        byte[] gColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setGColor(gColor);

        byte[] kColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setKColor(kColor);

        byte[] mColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setMColor(mColor);

        byte[] xColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setXColor(xColor);

        byte[] backColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setBackgroudColor(backColor);

        byte[] textColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setTextColor(textColor);

        byte[] linkNumberColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setLinkNumberColor(linkNumberColor);

        byte[] linkColor1 = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        byte[] linkColor2 = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        byte[] linkColor3 = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setLinkColors(linkColor1, linkColor2, linkColor3);

        byte[] gridColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setGridColor(gridColor);

        byte[] stemColor = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        CHViewPreferences.setStemColor(stemColor);

        short oRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setORadius(oRadius);

        short bRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setBRadius(bRadius);

        short aRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setARadius(aRadius);

        short fRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setFRadius(fRadius);

        short gRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setGRadius(gridStyle);

        short kRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setKRadius(kRadius);

        short mRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setMRadius(mRadius);

        short xRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setXRadius(xRadius);

        short dwarfRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setDwarfRadius(dwarfRadius);

        short giantRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setGiantRadius(giantRadius);

        short superGiantRadius = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setSuperGiantRadius(superGiantRadius);

        StringResult centreOrds1 = readString(buffer, currentIndex);
        currentIndex += centreOrds1.getIndexAdd();
        StringResult centreOrds2 = readString(buffer, currentIndex);
        currentIndex += centreOrds2.getIndexAdd();
        StringResult centreOrds3 = readString(buffer, currentIndex);
        currentIndex += centreOrds3.getIndexAdd();
        CHViewPreferences.setCentreOrdinate(centreOrds1.getValue(), centreOrds2.getValue(), centreOrds3.getValue());

        StringResult theta = readString(buffer, currentIndex);
        currentIndex += theta.getIndexAdd();
        CHViewPreferences.setTheta(Double.parseDouble(theta.getValue()));

        StringResult phi = readString(buffer, currentIndex);
        currentIndex += phi.getIndexAdd();
        CHViewPreferences.setPhi(Double.parseDouble(phi.getValue()));

        StringResult rho = readString(buffer, currentIndex);
        currentIndex += rho.getIndexAdd();
        CHViewPreferences.setRho(Double.parseDouble(rho.getValue()));

        StringResult tScale = readString(buffer, currentIndex);
        currentIndex += tScale.getIndexAdd();
        CHViewPreferences.setTScale(Double.parseDouble(tScale.getValue()));

        StringResult pScale = readString(buffer, currentIndex);
        currentIndex += pScale.getIndexAdd();
        CHViewPreferences.setPScale(Double.parseDouble(pScale.getValue()));

        StringResult rScale = readString(buffer, currentIndex);
        currentIndex += rScale.getIndexAdd();
        CHViewPreferences.setTheta(Double.parseDouble(theta.getValue()));

        StringResult xScale = readString(buffer, currentIndex);
        currentIndex += xScale.getIndexAdd();
        CHViewPreferences.setTheta(Double.parseDouble(theta.getValue()));

        StringResult yScale = readString(buffer, currentIndex);
        currentIndex += yScale.getIndexAdd();
        CHViewPreferences.setTheta(Double.parseDouble(theta.getValue()));

        StringResult group1 = readString(buffer, currentIndex);
        currentIndex += group1.getIndexAdd();
        StringResult group2 = readString(buffer, currentIndex);
        currentIndex += group2.getIndexAdd();
        StringResult group3 = readString(buffer, currentIndex);
        currentIndex += group3.getIndexAdd();
        StringResult group4 = readString(buffer, currentIndex);
        currentIndex += group4.getIndexAdd();
        CHViewPreferences.setNamesOfGroups(group1.getValue(), group2.getValue(), group3.getValue(), group4.getValue());

        boolean displayGroup1 = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        boolean displayGroup2 = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        boolean displayGroup3 = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        boolean displayGroup4 = readBoolean(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.setDisplayFlagGroupOn(displayGroup1, displayGroup2, displayGroup3, displayGroup4);

        StringResult routeLabel1 = readString(buffer, currentIndex);
        currentIndex += routeLabel1.getIndexAdd();
        byte[] routeColor1 = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        short routeStyle1 = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.addRoute(1, routeLabel1.getValue(), routeColor1, routeStyle1);

        StringResult routeLabel2 = readString(buffer, currentIndex);
        currentIndex += routeLabel2.getIndexAdd();
        byte[] routeColor2 = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        short routeStyle2 = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.addRoute(2, routeLabel2.getValue(), routeColor2, routeStyle2);

        StringResult routeLabel3 = readString(buffer, currentIndex);
        currentIndex += routeLabel3.getIndexAdd();
        byte[] routeColor3 = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        short routeStyle3 = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.addRoute(3, routeLabel3.getValue(), routeColor3, routeStyle3);

        StringResult routeLabel4 = readString(buffer, currentIndex);
        currentIndex += routeLabel4.getIndexAdd();
        byte[] routeColor4 = copyBytes(buffer, currentIndex, 3);
        currentIndex += 4;
        short routeStyle4 = readShort(buffer, currentIndex);
        currentIndex += 2;
        CHViewPreferences.addRoute(4, routeLabel4.getValue(), routeColor4, routeStyle4);

        currentIndex += 50;

        currentIndex += 50;

        currentIndex += 50;

        StringResult galCoordinatesx = readString(buffer, currentIndex);
        currentIndex += galCoordinatesx.getIndexAdd();

        StringResult galCoordinatesy = readString(buffer, currentIndex);
        currentIndex += galCoordinatesy.getIndexAdd();

        StringResult galCoordinatesz = readString(buffer, currentIndex);
        currentIndex += galCoordinatesz.getIndexAdd();
        CHViewPreferences.setGalCoordinates(
                galCoordinatesx.getValue(),
                galCoordinatesy.getValue(),
                galCoordinatesz.getValue()
        );

        return CHViewPreferences;
    }

    /**
     * copy an array of byte form a start point
     *
     * @param buffer the buffer
     * @param index  the start
     * @param length the length
     * @return the byte array
     */
    private byte @NotNull [] copyBytes(byte[] buffer, int index, int length) {
        byte[] byteArray = new byte[length];
        for (int i = 0; i < length; ++i) {
            int position = index + i;
            byteArray[i] = buffer[position];
        }
        return byteArray;
    }

    /**
     * parse a single ChView star record
     *
     * @param buffer          the file content
     * @param progressUpdater a thread safe updater control
     * @return the parsed record
     */
    private @Nullable ChViewRecord parseRecord(byte[] buffer, int index, @NotNull ProgressUpdater progressUpdater) {

        ChViewRecord chViewRecord = new ChViewRecord();

        try {
            // set the record number
            chViewRecord.setRecordNumber(recordNumber++);

            // first problem is to find boundary of where star record actially starts
            // there might be a proper place name  followed by a star name
            //  OR ... there might not be a star name
            // the stupid developer who write the original program seems to have had
            // convention where both names and strings were stored as strings
            // a name starts with a length followed by a 00 byte then the actual name.
            // a float/double (does he even know the difference??) is a length followed by
            // the value,no 00

            // so we need to figure out whether there is a place and a star or just a star
            // we scan until we see the dToEarth which should be a float/double.
            // if we encounter two string link objects prior to that then we have
            // a place and a star followed by a distance
            // else if only one prior to the dToEarth then the place is null and
            // there is only a star name.
            // Damn, who programs like this??

            PseudoString string1 = findString(buffer, currentIndex);
            currentIndex += string1.getLength();

            PseudoString string2 = findString(buffer, currentIndex);
            currentIndex += string2.getLength();

            if (string1.isName() && string2.isName()) {
                // found place name and star name
                chViewRecord.setProperPlaceName(string1.getValue());
                chViewRecord.setStarName(string2.getValue());

                // now parse for the distance to earth value
                PseudoString dToEarth = readStarParameter(buffer, currentIndex);
                currentIndex += dToEarth.getLength();
                chViewRecord.setDistanceToEarth(dToEarth.getValue());

            } else {
                // this is the only other valid case, or we really screwed up the pointers
                if (!string2.isName()) {
                    chViewRecord.setStarName(string1.getValue());
                    chViewRecord.setDistanceToEarth(string2.getValue());
                } else {
                    throw new IllegalArgumentException("File parsing is really screwed");
                }
            }

            PseudoString spectra = readStarParameter(buffer, currentIndex);
            currentIndex += spectra.getLength();
            chViewRecord.setSpectra(spectra.getValue());

            PseudoString mass = readStarParameter(buffer, currentIndex);
            currentIndex += mass.getLength();
            chViewRecord.setCollapsedMass(Double.parseDouble(mass.getValue()));

            PseudoString actualMass = readStarParameter(buffer, currentIndex);
            currentIndex += actualMass.getLength();
            chViewRecord.setUncollapsedMass(Double.parseDouble(actualMass.getValue()));

            PseudoString ords1 = readStarParameter(buffer, currentIndex);
            currentIndex += ords1.getLength();

            PseudoString ords2 = readStarParameter(buffer, currentIndex);
            currentIndex += ords2.getLength();

            PseudoString ords3 = readStarParameter(buffer, currentIndex);
            currentIndex += ords3.getLength();
            chViewRecord.setOrdinates(
                    Double.parseDouble(ords1.getValue()),
                    Double.parseDouble(ords2.getValue()),
                    Double.parseDouble(ords3.getValue())
            );

            PseudoString constellation = readStarParameter(buffer, currentIndex);
            currentIndex += constellation.getLength();
            chViewRecord.setConstellation(constellation.getValue());

            PseudoString comment = readStarParameter(buffer, currentIndex);
            currentIndex += comment.getLength();
            chViewRecord.setComment(comment.getValue());

            boolean selected = readBoolean(buffer, currentIndex);
            currentIndex += 2;
            chViewRecord.setSelected(selected);

            short indexInFile = readShort(buffer, currentIndex);
            currentIndex += 2;
            chViewRecord.setIndex(indexInFile);

            short group = readShort(buffer, currentIndex);

            currentIndex += 2;
            chViewRecord.setGroupNumber(group);

            if (buffer[currentIndex] != 0) {
                currentIndex++;
                log.debug(chViewRecord.toString());
                chViewRecord.setSubsidiaryStar(parseRecord(buffer, currentIndex, progressUpdater));
            }

            // get the stellar class
            String stellarClass = spectra.getValue().substring(0, 1);
            if (stellarClass.equals("\"")) {
                if (spectra.getValue().length() >= 2) {
                    stellarClass = spectra.getValue().substring(1, 2);
                }
            }

            chViewRecord.setOrthoSpectra(stellarClass);

            // if it's an Q or d, then make it and M
            if (stellarClass.equals("Q") ||
                    stellarClass.equals("d") ||
                    stellarClass.equals("p") ||
                    stellarClass.equals("D") ||
                    stellarClass.equals("R") ||
                    stellarClass.equals("P") ||
                    stellarClass.equals("X") ||
                    stellarClass.equals("W") ||
                    stellarClass.equals("S") ||
                    stellarClass.equals("N") ||
                    stellarClass.equals("Z") ||
                    stellarClass.equals("C")
            ) {
                // these are not defined in standard spectral classification so force them to be M class.
                chViewRecord.setOrthoSpectra("DC");
            }

            if (stellarClass.equals("F")) {
                log.info("found F");
            }

            if (stellarClass.equals("?")) {
                // these are not defined in standard spectral classification so force them to be M class.
                chViewRecord.setOrthoSpectra("Unk");
            }


            if (stellarFactory.classes(chViewRecord.getOrthoSpectra())) {

                // the stellar classification
                StellarClassification stellarClassification = stellarFactory.getStellarClass(chViewRecord.getOrthoSpectra());
                if (stellarClassification == null) {
                    log.error("Could not figure out stellar classification: spectra ={}", chViewRecord.getOrthoSpectra());
                    return null;
                }

                // get the star color and store it
                Color starColor = getColor(stellarClassification.getStarColor());
                chViewRecord.setStarColor(starColor);

                // the star radius
                double radius = stellarClassification.getAverageRadius();
                chViewRecord.setRadius(radius);
            } else {
                chViewRecord.setStarColor(getColor(StarColor.M));
                chViewRecord.setRadius(0.5);
            }

            log.debug(chViewRecord.toString());


        } catch (Exception e) {
            log.error("format corruption in file:" + e.getMessage());
            progressUpdater.updateTaskInfo("Loading CHV file:Encountered a corruption in file and was not about to continue");
            return null;
        }
        return chViewRecord;
    }

    private @NotNull Color getColor(@NotNull StarColor starColor) {
        String[] colorRGB = starColor.color().split(",");
        return Color.rgb(
                Integer.parseInt(colorRGB[0]),
                Integer.parseInt(colorRGB[1]),
                Integer.parseInt(colorRGB[2])
        );
    }

    private @NotNull PseudoString readStarParameter(byte[] buffer, int index) {
        // initialization
        PseudoString pseudoString = new PseudoString();
        int i = index;
        int lengthDiscovered = 0;
        int paddingCounter = 0;

        // handle the condition where the length is > 255
        if (buffer[i] != 0xFF) {
            // simple length
            lengthDiscovered = buffer[i];
            i++;  // move pointer to just past length
            paddingCounter++;
        } else {
            // complex length
            i++; // skip past special length marker
            lengthDiscovered = readShort(buffer, i);
            paddingCounter += 3;
            i += 2;
        }

        if (buffer[i] == 0) {
            // this is a name
            i++;
            StringBuilder name = readString(buffer, i, lengthDiscovered);
            pseudoString.setName(true);
            pseudoString.setValue(name.toString());
            paddingCounter++;
        } else {
            // this is a number (double/float, whatever)
            pseudoString.setName(false);
            StringBuilder name = readString(buffer, i, lengthDiscovered);
            pseudoString.setValue(name.toString());
        }

        // now set the actual length traversed
        lengthDiscovered += paddingCounter;
        pseudoString.setLength(lengthDiscovered);
        return pseudoString;

    }

    /**
     * scan and parse for a pseudo string and determine
     * what it is (number of actual name)
     * Note that this is a special parser and meant to find
     * the first valid string and where it ends.
     * <p>
     * We use it to help find the start of a star record
     *
     * @param buffer the buffer to read
     * @param index  the point to start from
     * @return the result of what was found
     */
    private @NotNull PseudoString findString(byte[] buffer, int index) {
        // initialization
        PseudoString pseudoString = new PseudoString();
        int i = index;
        int lengthDiscovered = 0;
        int paddingCounter = 0;

        // scan for first non-zero
        int j = scanNonZero(buffer, i);
        paddingCounter += Math.abs(j - i);
        i = j;

        // handle the condition where the length is > 255
        if (buffer[i] != 0xFF) {
            // simple length
            lengthDiscovered = buffer[i];
            i++;  // move pointer to just past length
            paddingCounter++;
        } else {
            // complex length
            i++; // skip past special length marker
            lengthDiscovered = readShort(buffer, i);
            paddingCounter += 3;
            i += 2;
        }

        if (buffer[i] == 0) {
            // this is a name
            i++;
            StringBuilder name = readString(buffer, i, lengthDiscovered);
            pseudoString.setName(true);
            pseudoString.setValue(name.toString());
            paddingCounter++;
        } else {
            // this is a number (double/float, whatever)
            int k = scanNonZero(buffer, i);
            pseudoString.setName(false);
            paddingCounter += Math.abs(k - i);
            i = k;
            StringBuilder name = readString(buffer, i, lengthDiscovered);
            pseudoString.setValue(name.toString());
        }

        // now set the actual length traversed
        lengthDiscovered += paddingCounter;
        pseudoString.setLength(lengthDiscovered);
        return pseudoString;
    }


    // ---------------------- Primitive Parsers ------------------------ //

    /**
     * scan for non zero elements
     *
     * @param buffer the buffer to read
     * @param i      the beginning point
     * @return a description of what was read
     */
    private int scanNonZero(byte[] buffer, int i) {
        while (buffer[i] == 0) {
            i++;
        }
        return i;
    }

    /**
     * read a short from the buffer
     *
     * @param buffer the buffer
     * @param index  the index
     * @return the short value
     */
    private short readShort(byte[] buffer, int index) {
        return EndianUtils.readSwappedShort(buffer, index);
    }

    /**
     * read a boolean from the array
     *
     * @param buffer the buffer
     * @param index  the index point
     * @return a boolean
     */
    private boolean readBoolean(byte[] buffer, int index) {
        int value = EndianUtils.readSwappedShort(buffer, index);
        return value != 0;
    }

    /**
     * read an int from buffer
     *
     * @param buffer the buffer
     * @param index  the index
     * @return the int value
     */
    private int readInt(byte[] buffer, int index) {
        return EndianUtils.readSwappedInteger(buffer, index);
    }

    /**
     * Read a String from the buffer
     * <p>
     * Strings: are saved a 1 byte length determinator followed by that many bytes of data. This does not
     * include the null terminator. If the string is longer than 254 then a byte containing FF is stored with
     * two bytes following containing the actual length.
     *
     * @param buffer the buffer to read from
     * @param index  the index from which to read
     * @return the string
     */
    private @NotNull StringResult readString(byte[] buffer, int index) {

        StringResult result = new StringResult();
        StringBuilder stringBuilder = new StringBuilder();

        short length = getStringLength(buffer, index);

        if (length < 255) {
            int skipCount = skipWeirdEmpties(buffer, index + 1);
            stringBuilder.append(readString(buffer, index + 1 + skipCount, length));
            result.setIndexAdd(length + 1 + skipCount);
        } else {
            // recalculate length from following bytes
            length = readShort(buffer, index + 1);
            int skipCount = skipWeirdEmpties(buffer, index + 3);
            stringBuilder.append(readString(buffer, index + 3 + skipCount, length));
            result.setIndexAdd(length + 3 + skipCount);
        }

        result.setLength(length);
        result.setValue(stringBuilder.toString());

        return result;
    }

    private int skipWeirdEmpties(byte[] buffer, int i) {
        int skipCount = 0;
        while (buffer[i + skipCount] == 0) {
            skipCount++;
        }
        return skipCount;
    }

    private short getStringLength(byte[] buffer, int index) {
        short value = buffer[index];
        if (value != 0xff) {
            // length is in 1 byte
            return value;
        } else {
            // length is longer than one byte
            return readShort(buffer, index + 1);
        }
    }

    private @NotNull StringBuilder readString(byte[] buffer, int index, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            if (buffer[index + i] < 0) {
                log.error("Bad ASCII character at index=" + index + i);
                // I threw this error as a help to parsing, so I would know why the parser failed
                throw new IllegalArgumentException();
            }
            stringBuilder.append((char) buffer[index + i]);
        }
        return stringBuilder;
    }


}
