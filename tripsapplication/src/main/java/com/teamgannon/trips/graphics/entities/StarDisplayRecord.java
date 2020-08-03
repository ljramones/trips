package com.teamgannon.trips.graphics.entities;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class StarDisplayRecord {

    /**
     * name of the star
     */
    private String starName;
    /**
     * database Id
     */
    private UUID recordId;
    /**
     * star color
     */
    private Color starColor;
    /**
     * star radius
     */
    private double radius;
    /**
     * actual location of the star
     */
    @Builder.Default
    private double[] actualCoordinates = new double[3];
    /**
     * the x,y,z for the star in screen coordinates - scaled to fit on screen
     */
    private Point3D coordinates;

    public static StarDisplayRecord fromProperties(Map<String, String> properties) {
        String starName = properties.get("name");
        UUID recordId = UUID.fromString(properties.get("recordNumber"));
        double radius = Double.parseDouble(properties.get("radius"));
        Color color = fromRGB(properties.get("color"));

        double xAct = Double.parseDouble(properties.get("xAct"));
        double yAct = Double.parseDouble(properties.get("yAct"));
        double zAct = Double.parseDouble(properties.get("zAct"));
        double[] coordinates = new double[3];
        coordinates[0] = xAct;
        coordinates[1] = yAct;
        coordinates[2] = zAct;

        double x = Double.parseDouble(properties.get("x"));
        double y = Double.parseDouble(properties.get("y"));
        double z = Double.parseDouble(properties.get("z"));
        Point3D point3D = new Point3D(x, y, z);

        return StarDisplayRecord
                .builder()
                .starName(starName)
                .starColor(color)
                .recordId(recordId)
                .radius(radius)
                .actualCoordinates(coordinates)
                .coordinates(point3D)
                .build();
    }

    private static Color fromRGB(String colorStr) {
        String[] parts = colorStr.split(",");
        double red = Double.parseDouble(parts[0]);
        double green = Double.parseDouble(parts[1]);
        double blue = Double.parseDouble(parts[2]);
        return Color.color(red, green, blue);
    }

    public Map<String, String> toProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("name", getStarName());
        properties.put("recordNumber", getRecordId().toString());
        properties.put("radius", String.format("%4.1f", getRadius()));
        properties.put("color", toRGB(starColor));

        properties.put("xAct", String.format("%5.1f", actualCoordinates[0]));
        properties.put("yAct", String.format("%5.1f", actualCoordinates[1]));
        properties.put("zAct", String.format("%5.1f", actualCoordinates[2]));

        properties.put("x", String.format("%5.1f", getCoordinates().getX()));
        properties.put("y", String.format("%5.1f", getCoordinates().getY()));
        properties.put("z", String.format("%5.1f", getCoordinates().getZ()));

        return properties;
    }

    private String toRGB(Color starColor) {
        return starColor.getRed() + "," + starColor.getGreen() + "," + starColor.getBlue();
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
