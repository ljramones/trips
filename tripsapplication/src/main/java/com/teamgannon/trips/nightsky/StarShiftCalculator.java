package com.teamgannon.trips.nightsky;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * This class calculates the shift in RA and Dec for a list of stars given a remote star.
 */
@Slf4j
public class StarShiftCalculator {

    public static List<StarPositionDescriptor> computeShiftedPositions(StarPositionDescriptor remoteStarPositionDescriptor, List<StarPositionDescriptor> starPositionDescriptors) {
        List<StarPositionDescriptor> shiftedStarPositionDescriptors = new ArrayList<>();

        for (StarPositionDescriptor starPositionDescriptor : starPositionDescriptors) {
            // Calculate parallax angle in radians
            double p = Math.atan(1.0 / starPositionDescriptor.getDistance()) - Math.atan(1.0 / remoteStarPositionDescriptor.getDistance());

            // Shift in declination is straightforward
            double deltaDec = Math.toDegrees(p);

            // Shift in RA depends on the declination of the star and the remote star
            double deltaRa = Math.toDegrees(p * Math.cos(Math.toRadians(starPositionDescriptor.getDec())) / Math.cos(Math.toRadians(remoteStarPositionDescriptor.getDec())));

            // Add shifts to original coordinates
            double shiftedRa = starPositionDescriptor.getRa() + deltaRa;
            double shiftedDec = starPositionDescriptor.getDec() + deltaDec;

            // Ensure RA is within [0, 360] range
            if (shiftedRa < 0) shiftedRa += 360;
            if (shiftedRa > 360) shiftedRa -= 360;

            shiftedStarPositionDescriptors.add(new StarPositionDescriptor(shiftedRa, shiftedDec, starPositionDescriptor.getDistance()));
        }

        return shiftedStarPositionDescriptors;
    }

    public static void main(String[] args) {
        StarPositionDescriptor remoteStarPositionDescriptor = new StarPositionDescriptor(5.5, 23.44, 4.24); // Example values for Proxima Centauri

        List<StarPositionDescriptor> starPositionDescriptors = new ArrayList<>();
        starPositionDescriptors.add(new StarPositionDescriptor(14.5, -60.83, 4.37)); // Example star (Alpha Centauri A)
        starPositionDescriptors.add(new StarPositionDescriptor(21.3, 45.25, 11.4));  // Another example star

        List<StarPositionDescriptor> shiftedStarPositionDescriptors = computeShiftedPositions(remoteStarPositionDescriptor, starPositionDescriptors);

        for (int i = 0; i < starPositionDescriptors.size(); i++) {
            System.out.println("Original Star RA: " + starPositionDescriptors.get(i).getRa() + ", Dec: " + starPositionDescriptors.get(i).getDec());
            System.out.println("Shifted Star RA: " + shiftedStarPositionDescriptors.get(i).getRa() + ", Dec: " + shiftedStarPositionDescriptors.get(i).getDec());
            System.out.println("------");
        }
    }
}

