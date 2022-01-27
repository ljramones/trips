package com.teamgannon.trips.dynamics;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.io.File;
import java.util.Locale;

public class DynamicsCalculator {

    private Frame frame;

    private TimeScale utc;

    private AbsoluteDate startDate;

    /**
     * the calculated orbital parameters
     */
    private KeplerianOrbit orbit;

    private KeplerianPropagator kepler;

    public DynamicsCalculator(OrbitDescriptor orbitDescriptor, double mu, int dayNumOffsetFrom2000) {

        // use a base frame to define the system
        frame = FramesFactory.getGCRF();

        // use a UTC time scale
        utc = TimeScalesFactory.getUTC();

        // setup the absolute date
        startDate = new AbsoluteDate(new DateComponents(dayNumOffsetFrom2000), utc);

        // define the Keplerian orbit for an object
        this.orbit = new KeplerianOrbit(orbitDescriptor.semiMajorAxis, orbitDescriptor.getEccentricity(),
                orbitDescriptor.inclination,
                orbitDescriptor.getPerigreeArgument(), orbitDescriptor.rightAscensionOfAscendingNode,
                orbitDescriptor.meanAnomaly,
                PositionAngle.TRUE, frame, startDate, mu);

        // setup the propagator for this orbit - provides the changing dynamical positions
        kepler = new KeplerianPropagator(orbit);

        // this is default mode
//        kepler.setSlaveMode();
    }


    public OrbitalPosition moveOrbitByTimeStep(OrbitalPosition position, double timeStep) {
        AbsoluteDate shiftTime = startDate.shiftedBy(timeStep);
        SpacecraftState currentState = kepler.propagate(shiftTime);
        Orbit currentOrbitPosition = currentState.getOrbit();
        TimeStampedPVCoordinates pvCoordinates = currentOrbitPosition.getPVCoordinates();

        return OrbitalPosition
                .builder()
                .object(position.getObject())
                .positionTime(timeStep)
                .orbitCoordinates(pvCoordinates)
                .build();
    }

    private static void initializeOrekitData() {
        final File home = new File(System.getProperty("user.home"));
        final File orekitData = new File(home, "orekit-data");
        if (!orekitData.exists()) {
            System.err.format(Locale.US, "Failed to find %s folder%n",
                    orekitData.getAbsolutePath());
            System.err.format(Locale.US, "You need to download %s from %s, unzip it in %s and rename it 'orekit-data' for this tutorial to work%n",
                    "orekit-data-master.zip", "https://gitlab.orekit.org/orekit/orekit-data/-/archive/master/orekit-data-master.zip",
                    home.getAbsolutePath());
            System.exit(1);
        }
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));
    }


    public static void main(String[] args) {

        initializeOrekitData();

        double mu = DynamicsFactory.mu(DynamicsFactory.SUN_MASS);

        double semiMajorAxis = 24396159;                    // semi major axis in meters
        double eccentricity = 0.5831215;                    // eccentricity
        double inclination = FastMath.toRadians(7);      // inclination
        double omega = FastMath.toRadians(180);          // perigee argument
        double raan = FastMath.toRadians(261);           // right ascension of ascending node
        double meanAnomaly = 0;                            // mean anomaly

        OrbitDescriptor orbitDescriptor = OrbitDescriptor
                .builder()
                .semiMajorAxis(semiMajorAxis)
                .eccentricity(eccentricity)
                .inclination(inclination)
                .perigreeArgument(omega)
                .rightAscensionOfAscendingNode(raan)
                .meanAnomaly(meanAnomaly)
                .build();

        DynamicsCalculator earthSatellite = new DynamicsCalculator(orbitDescriptor, mu, 2);
        for (int i = 0; i < 600; i++) {
            OrbitalPosition newPosition = earthSatellite.moveOrbitByTimeStep(OrbitalPosition.builder().build(), i);
            Vector3D pos = newPosition.getOrbitCoordinates().getPosition();
            Vector3D vel = newPosition.getOrbitCoordinates().getVelocity();
            System.out.printf("%.2f::pos(X=%.2f, Y=%.2f, Z=%.2f); vel(dX=%.2f, dY=%.2f, dZ=%.2f)%n",
                    newPosition.getPositionTime(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    vel.getX(), vel.getY(), vel.getZ()
            );
        }


    }


}
