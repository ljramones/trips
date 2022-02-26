package com.teamgannon.trips.galacticmodelling;


import java.util.List;

public class VerletIntegrator {

    public void positionStep(List<PointMassBody> pointMassBodies, double dt) {
        double[] velocities = new double[]{0.0, 0.0, 0.0};
        for (PointMassBody body : pointMassBodies) {
            double accelerationFactor = body.getInvMass() * dt * 0.5;
            double[] position = body.getPosition();
            double[] velocity = body.getVelocity();
            double[] force = body.getForce();
            double[] prevForce = body.getPrevForce();

            velocities[0] = velocity[0];
            velocities[1] = velocity[1];
            velocities[2] = velocity[2];

            velocities[0] += force[0] * accelerationFactor;
            velocities[1] += force[1] * accelerationFactor;
            velocities[2] += force[2] * accelerationFactor;
            body.setVelocity(velocity);

            position[0] += velocities[0] * dt;
            position[1] += velocities[1] * dt;
            position[2] += velocities[2] * dt;
            body.setPosition(position);

            prevForce[0] = force[0];
            prevForce[1] = force[1];
            prevForce[2] = force[2];
            body.setPrevForce(prevForce);
        }
    }

    public void accelerationStep(List<PointMassBody> pointMassBodies, double dt) {
        for (PointMassBody body : pointMassBodies) {
            double[] velocity = body.getVelocity();
            double[] force = body.getForce();
            double[] prevForce = body.getPrevForce();

        }


    }

    public void velocityStep(List<PointMassBody> pointMassBodies, double dt){
        for (PointMassBody body : pointMassBodies) {
            double[] velocity = body.getVelocity();
            double[] force = body.getForce();
            double[] prevForce = body.getPrevForce();

        }

    }

}
