package com.teamgannon.trips.routeplanning;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
import com.teamgannon.trips.routeplanning.model.NavNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Routeplanner {

    /**
     * the list of nodes, used to look up in main database
     */
    private List<NavNode> navNodeList = new ArrayList<>();

    /**
     * the backing data store for routes
     */
    private TriangularMatrix triangularMatrix;

    /**
     * true if this was initialized
     */
    private boolean initialized = false;

    /**
     * the max number of dimensions
     */
    private int numDims;

    /**
     * load the screen data
     *
     * @param astroObjects the objects
     * @param maxDistance  the max distance
     */
    public void loadData(List<AstrographicObject> astroObjects, double maxDistance) {
        numDims = astroObjects.size();
        triangularMatrix = new TriangularMatrix(numDims);

        // mass convert the objects into a smaller no
        int i = 0;
        for (AstrographicObject astrographicObject : astroObjects) {
            NavNode build = NavNode.builder()
                    .index(i++)
                    .nodeId(astrographicObject.getId())
                    .nodeName(astrographicObject.getDisplayName())
                    .coordinates(astrographicObject.getCoordinates())
                    .build();
            navNodeList.add(build);
        }


        // this is a costly calculation
        // in reality this is a fold
        try {
            createRoutes(maxDistance);
        } catch (Exception e) {
            log.error("Could not calculate the routes because some of the coordinates are bad");
        }

        // ready for usage
        initialized = true;
    }

    /**
     * pick a distance to recalculate routes over
     *
     * @param distanceLimit the distance limit for routes
     */
    public void recalcRoutes(double distanceLimit) throws Exception {
        // this is a costly calculation
        // in reality this is a fold
        createRoutes(distanceLimit);
    }

    private void createRoutes(double distanceLimit) throws Exception {
        for (NavNode navNodeFrom : navNodeList) {
            for (NavNode navNodeTo : navNodeList) {
                double distance = StarMath.getDistance(navNodeFrom.getCoordinates(), navNodeTo.getCoordinates());
                if (distance < distanceLimit) {
                    triangularMatrix.set(navNodeFrom.getIndex(), navNodeTo.getIndex(), distance);
                } else {
                    // mark this as non routable
                    triangularMatrix.set(navNodeFrom.getIndex(), navNodeTo.getIndex(), -1);
                }
            }
        }
    }

    public double getDistance(int starA, int starB) {
        if (initialized) {
            return triangularMatrix.get(starA, starB);
        } else {
            return -1;
        }
    }

    public Double[] getDistances(int starA) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < numDims / 2 + 1; i++) {
            double distance = getDistance(starA, i);
            if (distance != -1) {
                values.add(distance);
            }
        }
        return (Double[]) values.toArray();
    }


    /////////////////////////////// helpers  ///////////////////////////////////


}
