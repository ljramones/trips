package com.teamgannon.trips.astrogation.referenceframes;

import lombok.extern.slf4j.Slf4j;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.TimeStampedPVCoordinates;


/**
 * Inertial Frames:
 * <p>
 * Do not rotate with respect to distant stars.
 * Primarily used for deep space navigation and celestial mechanics.
 * Examples:
 * GCRF (Geocentric Celestial Reference Frame): Centered at Earth's center of mass; aligned with the
 * quasi-inertial ICRF.
 * ICRF (International Celestial Reference Frame): Based on the positions of distant quasars, used for
 * very long baseline interferometry.
 * <p>
 * Inertial frames, also known as inertial reference frames, are coordinate systems in which objects are not
 * subjected to acceleration unless acted upon by an external force. In the context of space and
 * astrodynamics, several inertial frames are used to describe the motion of celestial bodies and spacecraft.
 * Here are some of the commonly used inertial frames:
 * <p>
 * GCRF (Geocentric Celestial Reference Frame):
 * <p>
 * An Earth-centered inertial frame.
 * It does not rotate with the Earth but remains fixed relative to distant quasars.
 * Useful for satellite motion studies, deep space navigation, and celestial mechanics.
 * ICRF (International Celestial Reference Frame):
 * <p>
 * Based on the positions of distant quasars and is used for very long baseline interferometry.
 * It is essentially an updated and more precise version of the older FK5/J2000 system.
 * The ICRF axes are aligned with the mean equator and equinox at epoch J2000.0.
 * EME2000 (Earth Mean Equator of J2000.0):
 * <p>
 * Also known as J2000 or simply the equatorial frame.
 * Defined by the Earth's mean equator and equinox at epoch J2000.0.
 * Historically used for many satellite operations and space missions.
 * TEME (True Equator, Mean Equinox):
 * <p>
 * Used by NORAD for their two-line element set (TLE) satellite data.
 * It's not tied to a fixed epoch, so the equator is the instantaneous equator.
 * MOD (Mean of Date):
 * <p>
 * Based on the mean equator and equinox of a specific date.
 * Used in some astronomical and satellite applications.
 * TOD (True of Date):
 * <p>
 * Based on the true equator and equinox of a specific date.
 * Accounts for short-periodic (daily) variations.
 * PEF (Pseudo Earth Fixed):
 * <p>
 * Rotates with the Earth but does not account for the minor wobbles (nutation and precession) of the Earth's rotation.
 * CIF (Celestial Intermediate Frame):
 * <p>
 * Introduced by IAU 2000 resolutions, serving as an intermediate frame for transformations.
 * Galactic Frame:
 * <p>
 * Centered on the Sun, with the plane of the frame aligned with the plane of the Milky Way.
 * Not technically an inertial frame due to the motion of the Sun around the galactic center, but it's often
 * treated as such for galactic-scale studies.
 * For most satellite operations, the GCRF, ICRF, or EME2000 frames are commonly used. The choice of frame
 * often depends on the specific requirements of a mission, the historical conventions of an organization,
 * or the context of a particular study. It's also worth noting that while these frames are considered "inertial"
 * for most practical purposes, no frame is perfectly inertial due to the influence of various gravitational
 * sources in the universe.
 */
@Slf4j
public class InertialReferenceFrame {

    public void getGCRF() {
        Frame gcrf = FramesFactory.getGCRF();
        System.out.println("Using the GCRF frame: " + gcrf);
    }


    public void getICRF() {
        Frame icrf = FramesFactory.getICRF();
        System.out.println("Using the ICRF frame: " + icrf);
    }


    public TimeStampedPVCoordinates transformGCRFtoICRF(TimeStampedPVCoordinates gcrfCoords) {
        Frame gcrf = FramesFactory.getGCRF();
        Frame icrf = FramesFactory.getICRF();
        Frame eme = FramesFactory.getEME2000();

        Transform gcrfToIcrf = gcrf.getTransformTo(icrf, AbsoluteDate.J2000_EPOCH);
        TimeStampedPVCoordinates icrfCoords = gcrfToIcrf.transformPVCoordinates(gcrfCoords);

        return icrfCoords;
    }


}
