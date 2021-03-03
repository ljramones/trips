package com.teamgannon.trips.solarsysmodelling.accrete;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static java.lang.Math.pow;

@Slf4j
class Utils {
    public final static double ECCENTRICITY_COEFF = 0.077;
    public final static double EARTH_SURF_PRES_IN_MILLIBARS = 1013.25;
    public final static double EARTH_SURF_PRES_IN_MMHG = 760.0;
    public final static double MMHG_TO_MILLIBARS = EARTH_SURF_PRES_IN_MILLIBARS / EARTH_SURF_PRES_IN_MMHG;
    public final static double PPM_PRESSURE = EARTH_SURF_PRES_IN_MILLIBARS / 1000000.0;
    private static Utils instance;
    private static Random random;
    private static List<SimStar> MSimStars;
    private static List<SimStar> KSimStars;
    private static List<SimStar> GSimStars;
    private static List<SimStar> FSimStars;
    private static List<SimStar> ASimStars;
    private static List<SimStar> BSimStars;
    private static List<SimStar> OSimStars;
    private static List<SimStar> WSimStars;
    private static List<SimStar> CSimStars;
    private static List<SimStar> SSimStars;
    private static List<SimStar> White;
    private static List<SimStar> Giant;
    private static Chemical[] Chemtable;
    private static long seed;

    private Utils() {
        seed = System.currentTimeMillis(); // 1528144920680 generates an Earthlike
        random = new Random(seed);
        loadChemicals();
        loadStars();
    }

    public static synchronized Utils instance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    private static void loadStars() {
        MSimStars = loadStarType("planetsim/MV_Stars.csv");
        KSimStars = loadStarType("planetsim/KV_Stars.csv");
        GSimStars = loadStarType("planetsim/GV_Stars.csv");
        FSimStars = loadStarType("planetsim/FV_Stars.csv");
        ASimStars = loadStarType("planetsim/AV_Stars.csv");
        BSimStars = loadStarType("planetsim/B_Stars.csv");
        OSimStars = loadStarType("planetsim/O_Stars.csv");
        WSimStars = loadStarType("planetsim/WR_Stars.csv");
        CSimStars = loadStarType("planetsim/C_Stars.csv");
        SSimStars = loadStarType("planetsim/S_Stars.csv");
        White = loadStarType("planetsim/White_Dwarves.csv");
        Giant = loadStarType("planetsim/Giants.csv");
    }

    public static BufferedReader loadFile(String fileName) {
        try {
            File file = ResourceUtils.getFile("classpath:" + fileName);
            return new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<SimStar> loadStarType(String filename) {
        List<SimStar> simStars = new ArrayList<>();
        SimStar s;
        String line;
        String[] split;

        try {
            BufferedReader input = loadFile(filename);
            while ((line = Objects.requireNonNull(input).readLine()) != null) {
                split = line.split(",");
                s = new SimStar(
                        Double.parseDouble(split[1]),
                        Double.parseDouble(split[2]),
                        Double.parseDouble(split[3]),
                        Double.parseDouble(split[4]),
                        Double.parseDouble(split[6])
                );
                s.stellarType = split[0];
                s.red = Integer.parseInt(split[9]);
                s.green = Integer.parseInt(split[10]);
                s.blue = Integer.parseInt(split[11]);
                simStars.add(s);
            }
            input.close();
        } catch (Exception e) {
            log.error(filename);
            log.error(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return simStars;
    }

    private static void loadChemicals() {
        Chemtable = new Chemical[15];
        Chemtable[0] = new Chemical(1, "H", "Hydrogen", 1.0079, 14.06, 20.40, 8.99e-05, 0.00125893, 27925.4, 1, 0.0);
        Chemtable[1] = new Chemical(2, "He", "Helium", 4.0026, 3.46, 4.20, 0.0001787, 7.94328e-09, 2722.7, 0, 61000.0 * MMHG_TO_MILLIBARS);
        Chemtable[2] = new Chemical(7, "N", "Nitrogen", 14.0067, 63.34, 77.40, 0.0012506, 1.99526e-05, 3.13329, 0, 2330.0 * MMHG_TO_MILLIBARS);
        Chemtable[3] = new Chemical(8, "O", "Oxygen", 15.9994, 54.80, 90.20, 0.001429, 0.501187, 23.8232, 10, 400.0 * MMHG_TO_MILLIBARS);
        Chemtable[4] = new Chemical(10, "Ne", "Neon", 20.1700, 24.53, 27.10, 0.0009, 5.01187e-09, 3.4435e-5, 0, 3900.0 * MMHG_TO_MILLIBARS);
        Chemtable[5] = new Chemical(18, "Ar", "Argon", 39.9480, 84.00, 87.30, 0.0017824, 3.16228e-06, 0.100925, 0, 1220.0 * MMHG_TO_MILLIBARS);
        Chemtable[6] = new Chemical(36, "Kr", "Krypton", 83.8000, 116.60, 119.70, 0.003708, 1e-10, 4.4978e-05, 0, 350.0 * MMHG_TO_MILLIBARS);
        Chemtable[7] = new Chemical(54, "Xe", "Xenon", 131.3000, 161.30, 165.00, 0.00588, 3.16228e-11, 4.69894e-06, 0, 160.0 * MMHG_TO_MILLIBARS);
        Chemtable[8] = new Chemical(900, "NH3", "Ammonia", 17.0000, 195.46, 239.66, 0.001, 0.002, 0.0001, 1, 100.0 * PPM_PRESSURE);
        Chemtable[9] = new Chemical(901, "H2O", "Water", 18.0000, 273.16, 373.16, 1.000, 0.03, 0.001, 0, 0.0);
        Chemtable[10] = new Chemical(902, "CO2", "CarbonDioxide", 44.0000, 194.66, 194.66, 0.001, 0.01, 0.0005, 0, 7.0 * MMHG_TO_MILLIBARS);
        Chemtable[11] = new Chemical(903, "O3", "Ozone", 48.0000, 80.16, 161.16, 0.001, 0.001, 0.000001, 2, 0.1 * PPM_PRESSURE);
        Chemtable[12] = new Chemical(904, "CH4", "Methane", 16.0000, 90.16, 109.16, 0.010, 0.005, 0.0001, 1, 50000.0 * PPM_PRESSURE);
// TODO: Format this correctly in case they can be added later? Some good stuff here.
        Chemtable[13] = new Chemical(9, "F", "Fluorine", 18.9984, 53.58, 85.10, 0.001696, 0.000630957, 0.000843335, 50, 0.1 * PPM_PRESSURE);
        Chemtable[14] = new Chemical(17, "Cl", "Chlorine", 35.4530, 172.22, 239.20, 0.003214, 0.000125893, 0.005236, 40, 1.0 * PPM_PRESSURE);
//        Chemtable[12] = new Chemical(910, "H2", "H2", 2, 14.06, 20.40, 8.99e-05,  0.00125893, 27925.4
//        Chemtable[12] = new Chemical(911, "N2", "N2", 28, 63.34, 77.40, 0.0012506, 1.99526e-05,3.13329
//        Chemtable[12] = new Chemical(912, "O2", "O2", 32, 54.80, 90.20, 0.001429,  0.501187, 23.8232, 10, 0.0);
//        Chemtable[12] = new Chemical(905, "CH3CH2OH", "Ethanol", 46.0000, 159.06, 351.66, 0.895, 0.001, 0.001, 0
    }

    public long getSeed() {
        return seed;
    }

    public double randomNumber(double inner, double outer) {
        return random.nextDouble() * (outer - inner) + inner;
    }

    public double randomEccentricity() {
        return (1.0 - pow(random.nextDouble(), ECCENTRICITY_COEFF));
    }

    public double about(double value, double variation) {
        return value + (value * randomNumber(-variation, variation));
    }

    public SimStar randomStar() {
        double roll = random.nextDouble();
        if (roll <= 0.907) { // Main sequence stars
            roll = random.nextDouble();
            if (roll <= 0.751) { // M type main sequence stars
                return MSimStars.get(random.nextInt(MSimStars.size())).deviate();
            } else if (roll <= 0.887) { // K type main sequence stars
                return KSimStars.get(random.nextInt(KSimStars.size())).deviate();
            } else if (roll <= 0.960) { // G type main sequence stars
                return GSimStars.get(random.nextInt(GSimStars.size())).deviate();
            } else if (roll <= 0.991) { // F type main sequence stars
                return FSimStars.get(random.nextInt(FSimStars.size())).deviate();
            } else { // A type main sequence stars
                return ASimStars.get(random.nextInt(ASimStars.size())).deviate();
            }
        } else if (roll <= 0.969) { // White dwarves
            return White.get(random.nextInt(White.size())).deviate();
        } else if (roll <= 0.998) { // Giants
            return Giant.get(random.nextInt(Giant.size())).deviate();
        } else { // Other stars
            roll = random.nextDouble();
            if (roll <= 0.785) { // B type stars
                return BSimStars.get(random.nextInt(BSimStars.size())).deviate();
            } else if (roll <= 0.999) { // O type stars
                return OSimStars.get(random.nextInt(OSimStars.size())).deviate();
            } else { // Specials and "odd" stars
                roll = random.nextDouble();
                if (roll <= 0.997) { // O type stars
                    return OSimStars.get(random.nextInt(OSimStars.size())).deviate();
                } else if (roll <= 0.998) { // Wolf Rayet type stars
                    return WSimStars.get(random.nextInt(WSimStars.size())).deviate();
                } else if (roll <= 0.999) { // C type stars
                    return CSimStars.get(random.nextInt(CSimStars.size())).deviate();
                } else { // S type stars
                    return SSimStars.get(random.nextInt(SSimStars.size())).deviate();
                }
            }
        }
    }

    public SimStar randomMStar() {
        return MSimStars.get(random.nextInt(MSimStars.size())).deviate();
    }

    public SimStar randomKStar() {
        return KSimStars.get(random.nextInt(KSimStars.size())).deviate();
    }

    public SimStar randomGStar() {
        return GSimStars.get(random.nextInt(GSimStars.size())).deviate();
    }

    public SimStar randomFStar() {
        return FSimStars.get(random.nextInt(FSimStars.size())).deviate();
    }

    public Chemical[] getChemicals() {
        return Chemtable;
    }
}
