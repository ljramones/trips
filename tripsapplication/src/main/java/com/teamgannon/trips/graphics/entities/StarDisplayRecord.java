package com.teamgannon.trips.graphics.entities;

import com.teamgannon.trips.config.application.StarDescriptionPreference;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.stardata.StellarType;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Slf4j
@Data
public class StarDisplayRecord {

    /**
     * database Id
     */
    private UUID recordId;

    /**
     * the dataset name which we are guaranteeing to be unique
     */
    private String dataSetName;

    /**
     * name of the star
     */
    private String starName;

    /**
     * star color
     */
    private Color starColor;
    /**
     * star radius
     */
    private double radius;

    /**
     * distance in LY
     */
    private double distance;

    /**
     * From Simbad, We’re only storing ONE per object, and in reality we’re only interested in the first character
     * (OBAFGKMLTY) Objects which are sub-stellar such as planets should have a value of NULL.  We do not want
     * to try to use this field to code both spec class AND if something’s a planet or a black hole, or whatever.
     * That’s what the object type field is for. In the object type there will be types like “Planet” and
     * “Dust Disk” and “Nebula” etc as well as the various star types.
     * <p>
     * Also, we will need  to decide if we want to use Simbad’s spec types that contain extra info.  For example,
     * Simbad reports dM1.5  for a red dwarf, the initial d indicating a dwarf.  There are other such.  I don’t
     * like it.  The spectral class should be the temp and color of the star, not trying to code more than one
     * thing.  A stars dwarfness or giantness in my opinion goes in the Type array.
     */
    private @NotNull String spectralClass = "G";

    /**
     * the polity, NA means not applicatible
     */
    private @NotNull String polity = "NA";

    /**
     * actual location of the star
     */
    private double @NotNull [] actualCoordinates = new double[3];

    /**
     * the x,y,z for the star in screen coordinates - scaled to fit on screen
     */
    private Point3D coordinates;

    /**
     * A free form text field for any notes we want.  Preferentially DATA will be stored in data fields, even
     * if we have to add custom fields in the custom object, but sometimes text notes make sense.
     */
    private String notes;


    public StarDisplayRecord() {
        init();
    }

    public void init() {
        dataSetName = " ";
        starName = " ";
        starColor = Color.WHITE;
        radius = 0.1;
        distance = 0;
        spectralClass = "X";
        notes = " ";
    }

    public @NotNull StarDisplayRecord copy() {
        StarDisplayRecord record = new StarDisplayRecord();

        record.setNotes(notes);
        double[] newCoordinate = new double[3];
        newCoordinate[0] = actualCoordinates[0];
        newCoordinate[1] = actualCoordinates[1];
        newCoordinate[2] = actualCoordinates[2];
        record.setActualCoordinates(newCoordinate);
        Point3D newPoint = new Point3D(coordinates.getX(), coordinates.getY(), coordinates.getZ());
        record.setCoordinates(newPoint);
        record.setDataSetName(dataSetName);
        record.setDistance(distance);
        record.setRadius(radius);
        record.setRecordId(recordId);
        record.setSpectralClass(spectralClass);
        record.setStarColor(new Color(starColor.getRed(), starColor.getGreen(), starColor.getBlue(), starColor.getOpacity()));
        record.setStarName(starName);
        record.setPolity(polity);

        return record;
    }

    public static @NotNull AstrographicObject toAstrographicObject(@NotNull StarDisplayRecord displayRecord) {
        AstrographicObject object = new AstrographicObject();

        object.setId(displayRecord.getRecordId());
        object.setDataSetName(displayRecord.getDataSetName());
        object.setDisplayName(displayRecord.getStarName());
        object.setNotes(displayRecord.getNotes());
        object.setX(displayRecord.getX());
        object.setY(displayRecord.getY());
        object.setZ(displayRecord.getZ());
        object.setRadius(displayRecord.getRadius());
        object.setDistance(displayRecord.getDistance());
        object.setSpectralClass(displayRecord.getSpectralClass());
        object.setPolity(displayRecord.getPolity());

        return object;
    }

    public static @Nullable StarDisplayRecord fromAstrographicObject(@NotNull AstrographicObject astrographicObject,
                                                                     @NotNull StarDisplayPreferences starDisplayPreferences) {
        StarDisplayRecord record = new StarDisplayRecord();

        StellarType stellarType;
        try {
            stellarType = StellarType.valueOf(astrographicObject.getOrthoSpectralClass());
        } catch (Exception e) {
            stellarType = StellarType.M;
        }

        StarDescriptionPreference starDescriptionPreference = starDisplayPreferences.get(stellarType);
        if (starDescriptionPreference != null) {
            record.setRadius(starDescriptionPreference.getSize());
            record.setStarColor(starDescriptionPreference.getColor());

            record.setRecordId(astrographicObject.getId());
            record.setStarName(astrographicObject.getDisplayName());
            record.setDataSetName(astrographicObject.getDataSetName());
            record.setDistance(astrographicObject.getDistance());
            record.setSpectralClass(astrographicObject.getSpectralClass());
            record.setNotes(astrographicObject.getNotes());
            double[] coords = astrographicObject.getCoordinates();
            record.setActualCoordinates(coords);
            record.setPolity(astrographicObject.getPolity());
        } else {
            log.error("unable to find stellar type for:{}, record ={}", stellarType, record);
            return null;
        }

        return record;
    }


    public void setX(double x) {
        actualCoordinates[0] = x;
    }

    public double getX() {
        return actualCoordinates[0];
    }

    public void setY(double y) {
        actualCoordinates[1] = y;
    }

    public double getY() {
        return actualCoordinates[1];
    }

    public void setZ(double z) {
        actualCoordinates[2] = z;
    }

    public double getZ() {
        return actualCoordinates[2];
    }

    /**
     * is this the center point of the diagram
     *
     * @return tru is (x,y,z) == (0,0,0)
     */
    public boolean isCenter() {
        return (Math.abs(coordinates.getX()) <= 1)
                & (Math.abs(coordinates.getY()) <= 1)
                & (Math.abs(coordinates.getZ()) <= 1);
    }

}
