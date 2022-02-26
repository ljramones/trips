package com.teamgannon.trips.galacticmodelling;

import lombok.Data;

@Data
public class PointMassBody {

    /**
     * the mass
     */
    private double mass;

    /**
     * the coordinates of the position
     */
    private double[] position;

    /**
     * the velocity
     */
    private double[] velocity;

    /**
     * the force on the point mass
     */
    private double[] force;

    /**
     * the previous force
     */
    private double[] prevForce;

    /**
     * get the inverse of the mass
     *
     * @return the inverse and if the mass is zero then return 0
     */
    public double getInvMass() {
        if (mass != 0.0) {
            return 1 / mass;
        } else {
            return 0;
        }
    }

}
