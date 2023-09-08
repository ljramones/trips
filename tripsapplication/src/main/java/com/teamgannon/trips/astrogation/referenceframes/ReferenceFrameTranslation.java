package com.teamgannon.trips.astrogation.referenceframes;

import lombok.extern.slf4j.Slf4j;

import org.orekit.frames.*;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.TimeStampedPVCoordinates;

import org.orekit.frames.*;


@Slf4j
public class ReferenceFrameTranslation {


//  G A L A T I C
//    public TimeStampedPVCoordinates equatorialToGalactic(TimeStampedPVCoordinates equatorialCoords) {
//        // Create frames
//        Frame icrf = FramesFactory.getICRF();
//
//        Frame galactic = FramesFactory.getGalactic();
//
//        // Transform coordinates
//        TimeStampedPVCoordinates galacticCoords = equatorialCoords.transformedTo(galactic);
//
//        return galacticCoords;
//    }
//
//    public TimeStampedPVCoordinates galacticToEquatorial(TimeStampedPVCoordinates galacticCoords) {
//        // Create frames
//        Frame icrf = FramesFactory.getICRF();
//        Frame galactic = FramesFactory.getGalactic();
//
//        // Transform coordinates
//        TimeStampedPVCoordinates equatorialCoords = galacticCoords.transformedTo(icrf);
//
//        return equatorialCoords;
//    }
//
//    public TimeStampedPVCoordinates galacticToEquatorial(TimeStampedPVCoordinates galacticCoords) {
//        // Create frames
//        Frame icrf = FramesFactory.getICRF();
//        Frame galactic = FramesFactory.getGalactic();
//
//        // Transform coordinates
//        TimeStampedPVCoordinates equatorialCoords = galacticCoords.transformedTo(icrf);
//
//        return equatorialCoords;
//    }


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

        Transform gcrfToIcrf = gcrf.getTransformTo(icrf, AbsoluteDate.J2000_EPOCH);
        TimeStampedPVCoordinates icrfCoords = gcrfToIcrf.transformPVCoordinates(gcrfCoords);

        return icrfCoords;
    }




}
