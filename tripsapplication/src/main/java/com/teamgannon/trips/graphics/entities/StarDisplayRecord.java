package com.teamgannon.trips.graphics.entities;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private String spectralClass = "G";

    /**
     * actual location of the star
     */
    private double[] actualCoordinates = new double[3];

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


    public static StarDisplayRecord fromProperties(Map<String, String> properties) {
        StarDisplayRecord record = new StarDisplayRecord();

        String starName = properties.get("starName");
        record.setStarName(starName);
        UUID recordId = UUID.fromString(properties.get("recordId"));
        record.setRecordId(recordId);
        String dataSetName = properties.get("dataSetName");
        record.setDataSetName(dataSetName);
        double radius = Double.parseDouble(properties.get("radius"));
        record.setRadius(radius);

        double distance = Double.parseDouble(properties.get("distance"));
        record.setDistance(distance);

        Color starColor = fromRGB(properties.get("starColor"));
        record.setStarColor(starColor);

        String spectralClass = properties.get("spectralClass");
        record.setSpectralClass(spectralClass);
        String notes = properties.get("notes");
        record.setNotes(notes);

        double xAct = Double.parseDouble(properties.get("xAct"));
        double yAct = Double.parseDouble(properties.get("yAct"));
        double zAct = Double.parseDouble(properties.get("zAct"));
        double[] coordinates = new double[3];
        coordinates[0] = xAct;
        coordinates[1] = yAct;
        coordinates[2] = zAct;
        record.setActualCoordinates(coordinates);

        double x = Double.parseDouble(properties.get("x"));
        double y = Double.parseDouble(properties.get("y"));
        double z = Double.parseDouble(properties.get("z"));
        Point3D point3D = new Point3D(x, y, z);
        record.setCoordinates(point3D);

        return record;
    }

    private static Color fromRGB(String colorStr) {
        String[] parts = colorStr.split(",");
        double red = Double.parseDouble(parts[0]);
        double green = Double.parseDouble(parts[1]);
        double blue = Double.parseDouble(parts[2]);
        return Color.color(red, green, blue);
    }

    public static AstrographicObject toAstrographicObject(StarDisplayRecord displayRecord) {
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
        object.setStarColor(displayRecord.getStarColor());

        return object;
    }

    public static StarDisplayRecord fromAstrographicObject(AstrographicObject astrographicObject) {
        StarDisplayRecord record = new StarDisplayRecord();

        record.setRecordId(astrographicObject.getId());
        record.setStarName(astrographicObject.getDisplayName());
        record.setDataSetName(astrographicObject.getDataSetName());
        record.setRadius(astrographicObject.getRadius());
        record.setDistance(astrographicObject.getDistance());
        record.setSpectralClass(astrographicObject.getSpectralClass());
        record.setNotes(astrographicObject.getNotes());
        record.setStarColor(astrographicObject.getStarColor());
        record.setActualCoordinates(astrographicObject.getCoordinates());

        return record;
    }

    public Map<String, String> toProperties() {
        Map<String, String> properties = new HashMap<>();

        properties.put("starName", starName);
        properties.put("recordId", recordId.toString());
        properties.put("dataSetName", dataSetName);
        properties.put("radius", Double.toString(radius));
        properties.put("distance", Double.toString(distance));
        properties.put("radialVelocity", Double.toString(radius));
        properties.put("spectralClass", spectralClass);
        properties.put("notes", notes);
        properties.put("starColor", toRGB(starColor));

        properties.put("xAct", Double.toString(actualCoordinates[0]));
        properties.put("yAct", Double.toString(actualCoordinates[1]));
        properties.put("zAct", Double.toString(actualCoordinates[2]));

        properties.put("x", Double.toString(getCoordinates().getX()));
        properties.put("y", Double.toString(getCoordinates().getY()));
        properties.put("z", Double.toString(getCoordinates().getZ()));

        return properties;
    }

    private String toRGB(Color starColor) {
        return starColor.getRed() + "," + starColor.getGreen() + "," + starColor.getBlue();
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
