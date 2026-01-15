package com.teamgannon.trips.solarsystem.nightsky;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NightSkyMathTest {

    private static final double EPS = 1e-9;

    @Test
    void horizonBasisIsOrthonormal() {
        PlanetRotationModel model = new PlanetRotationModel(23.5, 86400.0, 0.0);
        ObserverLocation obs = new ObserverLocation(45.0, 10.0);

        HorizonBasis basis = NightSkyMath.computeHorizonBasis(model, obs, 1234.0);
        Vector3D east = basis.getEastUnit();
        Vector3D north = basis.getNorthUnit();
        Vector3D up = basis.getUpUnit();

        assertEquals(0.0, east.dotProduct(north), EPS);
        assertEquals(0.0, east.dotProduct(up), EPS);
        assertEquals(0.0, north.dotProduct(up), EPS);
        assertEquals(1.0, east.getNorm(), EPS);
        assertEquals(1.0, north.getNorm(), EPS);
        assertEquals(1.0, up.getNorm(), EPS);
    }

    @Test
    void horizonBasisIsPeriodic() {
        PlanetRotationModel model = new PlanetRotationModel(10.0, 5000.0, 15.0);
        ObserverLocation obs = new ObserverLocation(-20.0, 30.0);

        HorizonBasis basis1 = NightSkyMath.computeHorizonBasis(model, obs, 123.0);
        HorizonBasis basis2 = NightSkyMath.computeHorizonBasis(model, obs, 123.0 + model.getRotationPeriodSeconds());

        assertVectorClose(basis1.getEastUnit(), basis2.getEastUnit());
        assertVectorClose(basis1.getNorthUnit(), basis2.getNorthUnit());
        assertVectorClose(basis1.getUpUnit(), basis2.getUpUnit());
    }

    @Test
    void upAtPoleAlignsWithSpinAxis() {
        PlanetRotationModel model = new PlanetRotationModel(25.0, 86400.0, 0.0);
        ObserverLocation obs = new ObserverLocation(90.0, 0.0);

        HorizonBasis basis = NightSkyMath.computeHorizonBasis(model, obs, 0.0);
        Rotation obliquityRot = new Rotation(Vector3D.PLUS_I, Math.toRadians(model.getObliquityDeg()));
        Vector3D spinAxis = obliquityRot.applyTo(Vector3D.PLUS_J);

        assertVectorClose(spinAxis, basis.getUpUnit());
    }

    @Test
    void horizonConversionMatchesCardinalDirections() {
        PlanetRotationModel model = new PlanetRotationModel(0.0, 86400.0, 0.0);
        ObserverLocation obs = new ObserverLocation(0.0, 0.0);
        HorizonBasis basis = NightSkyMath.computeHorizonBasis(model, obs, 0.0);

        Vector3D upInertial = Vector3D.PLUS_I;
        Vector3D eastInertial = Vector3D.PLUS_K;

        Vector3D upHorizon = NightSkyMath.toHorizonCoords(upInertial, basis);
        Vector3D eastHorizon = NightSkyMath.toHorizonCoords(eastInertial, basis);

        assertEquals(1.0, upHorizon.getZ(), EPS);
        assertEquals(90.0, NightSkyMath.altitudeDeg(upHorizon), EPS);
        assertEquals(0.0, NightSkyMath.altitudeDeg(eastHorizon), EPS);
        assertEquals(90.0, NightSkyMath.azimuthDeg(eastHorizon), EPS);
    }

    private void assertVectorClose(Vector3D a, Vector3D b) {
        assertEquals(a.getX(), b.getX(), EPS);
        assertEquals(a.getY(), b.getY(), EPS);
        assertEquals(a.getZ(), b.getZ(), EPS);
    }
}
