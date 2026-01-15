package com.teamgannon.trips.solarsystem.nightsky;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.time.Instant;

/**
 * Right-handed inertial frame: +X, +Y (up), +Z.
 * Planet equator starts in the XZ plane before applying obliquity.
 * Longitude is positive east; azimuth is 0=N, 90=E.
 */
public final class NightSkyMath {

    private NightSkyMath() {
    }

    public static HorizonBasis computeHorizonBasis(PlanetRotationModel model,
                                                   ObserverLocation obs,
                                                   double tSeconds) {
        double obliquityRad = Math.toRadians(model.getObliquityDeg());
        Rotation obliquityRot = new Rotation(Vector3D.PLUS_I, obliquityRad);

        Vector3D spinAxis = obliquityRot.applyTo(Vector3D.PLUS_J);
        Vector3D xPrime = obliquityRot.applyTo(Vector3D.PLUS_I);
        Vector3D zPrime = obliquityRot.applyTo(Vector3D.PLUS_K);

        double meridianDeg = model.getPrimeMeridianAtEpochDeg();
        double period = model.getRotationPeriodSeconds();
        if (period != 0) {
            meridianDeg += (tSeconds / period) * 360.0;
        }
        Rotation spinRot = new Rotation(spinAxis, Math.toRadians(meridianDeg));
        Vector3D xBody = spinRot.applyTo(xPrime);
        Vector3D zBody = spinRot.applyTo(zPrime);

        double latRad = Math.toRadians(obs.getLatitudeDeg());
        double lonRad = Math.toRadians(obs.getLongitudeDeg());
        double cosLat = Math.cos(latRad);
        double sinLat = Math.sin(latRad);
        double cosLon = Math.cos(lonRad);
        double sinLon = Math.sin(lonRad);

        Vector3D up = new Vector3D(
                cosLat * cosLon, xBody,
                sinLat, spinAxis,
                cosLat * sinLon, zBody
        ).normalize();

        Vector3D east = up.crossProduct(spinAxis).normalize();
        Vector3D north = east.crossProduct(up).normalize();

        return new HorizonBasis(east, north, up);
    }

    public static Vector3D toHorizonCoords(Vector3D inertialDirectionUnit, HorizonBasis basis) {
        double east = inertialDirectionUnit.dotProduct(basis.getEastUnit());
        double north = inertialDirectionUnit.dotProduct(basis.getNorthUnit());
        double up = inertialDirectionUnit.dotProduct(basis.getUpUnit());
        return new Vector3D(east, north, up);
    }

    public static double altitudeDeg(Vector3D horizonCoords) {
        double up = horizonCoords.getZ();
        return Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, up))));
    }

    public static double azimuthDeg(Vector3D horizonCoords) {
        double az = Math.toDegrees(Math.atan2(horizonCoords.getX(), horizonCoords.getY()));
        return az < 0 ? az + 360.0 : az;
    }

    public static AltAz equatorialToAltAz(EquatorialCoordinates eq, ObserverLocation obs, Instant t) {
        double jd = AstroTime.julianDate(t);
        double lst = AstroTime.lstRadians(jd, obs.getLongitudeDeg());
        double ra = eq.getRaRadians();
        double dec = eq.getDecRadians();
        double lat = Math.toRadians(obs.getLatitudeDeg());

        double hourAngle = AstroTime.normalizeRadians(lst - ra);

        double sinDec = Math.sin(dec);
        double cosDec = Math.cos(dec);
        double sinLat = Math.sin(lat);
        double cosLat = Math.cos(lat);
        double sinH = Math.sin(hourAngle);
        double cosH = Math.cos(hourAngle);

        double east = cosDec * sinH;
        double north = sinDec * cosLat - cosDec * cosH * sinLat;
        double up = sinDec * sinLat + cosDec * cosH * cosLat;

        Vector3D horizon = new Vector3D(east, north, up);
        double alt = altitudeDeg(horizon);
        double az = azimuthDeg(horizon);
        return new AltAz(alt, az);
    }
}
