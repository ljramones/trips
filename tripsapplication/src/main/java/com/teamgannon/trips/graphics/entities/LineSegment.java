package com.teamgannon.trips.graphics.entities;

import com.teamgannon.trips.graphics.AstrographicTransformer;
import javafx.geometry.Point3D;
import lombok.Data;

@Data
public class LineSegment {

    /**
     * the actual form point in equatorial coordinates
     */
    private double[] actualFrom;

    /**
     * the actual to point in equatorial coordinates
     */
    private double[] actualTo;

    /**
     * the translated To in the new coordinates
     */
    private double[] translatedFrom;

    /**
     * the translated From in the new coordinates
     */
    private double[] translatedTo;

    /**
     * get a 3D From point in the translated coordinates
     * @return the from point
     */
    public Point3D getFrom() {
        return new Point3D(translatedFrom[0], translatedFrom[2], translatedFrom[3]);
    }

    /**
     * get a 3D To point in the translated coordinates
     * @return the to point
     */
    public Point3D getTo() {
        return new Point3D(translatedTo[0], translatedTo[2], translatedTo[3]);
    }

    /**
     * create a transformed line segment
     *
     * @param transformer the transformer
     * @param pointFrom   the point from
     * @param pointTo     the point to
     * @return the line segment
     */
    public static LineSegment getTransformedLine(AstrographicTransformer transformer, double[] pointFrom, double[] pointTo) {
        LineSegment lineSegment = new LineSegment();
        lineSegment.actualFrom = pointFrom;
        lineSegment.actualTo = pointTo;
        lineSegment.translatedFrom = transformer.transformOrds(lineSegment.actualFrom);
        lineSegment.translatedTo = transformer.transformOrds(lineSegment.actualTo);
        return lineSegment;
    }

}
