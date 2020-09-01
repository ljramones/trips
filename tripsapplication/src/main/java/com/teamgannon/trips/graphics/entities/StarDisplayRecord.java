package com.teamgannon.trips.graphics.entities;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class StarDisplayRecord {

    /**
     * database Id
     */
    private UUID recordId;

    /**
     * the dataset name which we are guaranteeing to be unique
     */
    private String dataSetName;

    /**
     * name of the star
     */
    private String starName;

    /**
     * star color
     */
    private Color starColor;
    /**
     * star radius
     */
    private double radius;

    /**
     * expressed as right ascension in HHMMSS
     */
    private double ra;

    /**
     * proper motion in right ascension)
     */
    private double pmra;

    /**
     * expressed Declination in DDMMSS
     */
    private double declination;

    /**
     * proper motion in declination)
     */
    private double pmdec;

    private double dec_deg;

    private double rs_cdeg;

    /**
     * the parallax measurement
     */
    private double parallax;

    /**
     * distance in LY
     */
    private double distance;

    /**
     * the star's radial velocity
     */
    private double radialVelocity;

    /**
     * From Simbad, We’re only storing ONE per object, and in reality we’re only interested in the first character
     * (OBAFGKMLTY) Objects which are sub-stellar such as planets should have a value of NULL.  We do not want
     * to try to use this field to code both spec class AND if something’s a planet or a black hole, or whatever.
     * That’s what the object type field is for. In the object type there will be types like “Planet” and
     * “Dust Disk” and “Nebula” etc as well as the various star types.
     * <p>
     * Also, we will need  to decide if we want to use Simbad’s spec types that contain extra info.  For example,
     * Simbad reports dM1.5  for a red dwarf, the initial d indicating a dwarf.  There are other such.  I don’t
     * like it.  The spectral class should be the temp and color of the star, not trying to code more than one
     * thing.  A stars dwarfness or giantness in my opinion goes in the Type array.
     */
    private String spectralClass = "G";

    /**
     * the temperature of the star in K
     */
    private double temperature;

    private double bprp;

    private double bpg;

    private double grp;

    /**
     * actual location of the star
     */
    private double[] actualCoordinates = new double[3];

    /**
     * the x,y,z for the star in screen coordinates - scaled to fit on screen
     */
    private Point3D coordinates;

    /**
     * A free form text field for any notes we want.  Preferentially DATA will be stored in data fields, even
     * if we have to add custom fields in the custom object, but sometimes text notes make sense.
     */
    private String notes;

    /**
     * this is a generic marker that means that this is marked by other
     * <p>
     * user defined
     */
    private boolean other = false;

    /**
     * this is a flag for whether there is an anomaly
     */
    private boolean anomaly = false;

    /**
     * What polity does this object belong to.  Obviously, it has to be null or one of the polities listed in
     * the theme above.
     */
    private String polity;

    /**
     * the type of world
     */
    private String worldType;

    /**
     * the type of fuel
     */
    private String fuelType;

    /**
     * the type of port
     */
    private String portType;

    /**
     * the type of population
     */
    private String populationType;

    /**
     * the tech type
     */
    private String techType;

    /**
     * the product type
     */
    private String productType;

    /**
     * the type of military in space
     */
    private String milSpaceType;

    /**
     * the type of military on the planet
     */
    private String milPlanType;


    /**
     * We choose to store the standard luminosity bands. Luminosity is published based on a set of standard
     * filters that define the spectral bands. The luminosity in each band is a poor mans spectrum of the star.
     * Fluxes are measured in watts/m2 Not every object will have a measurement in every band.  Some objects will
     * have no flux measurements. The standard bands are:
     */
    private String luminosity;

    public StarDisplayRecord() {
        init();
    }

    public void init() {
        recordId = UUID.randomUUID();
        dataSetName = " ";
        starName = " ";
        starColor = Color.WHITE;
        radius = 0.1;
        ra = 0;
        pmra = 0;
        declination = 0;
        pmdec = 0;
        dec_deg = 0;
        rs_cdeg = 0;
        parallax = 0;
        distance = 0;
        radialVelocity = 0;
        spectralClass = "X";
        temperature = 0;
        bprp = 0;
        bpg = 0;
        grp = 0;
        notes = " ";
        other = false;
        anomaly = false;
        polity = " ";
        worldType = " ";
        fuelType = " ";
        portType = " ";
        populationType = " ";
        techType = " ";
        productType = " ";
        milSpaceType = " ";
        milPlanType = " ";
        luminosity = " ";
    }


    public static StarDisplayRecord fromProperties(Map<String, String> properties) {
        StarDisplayRecord record = new StarDisplayRecord();

        String starName = properties.get("starName");
        record.setStarName(starName);
        UUID recordId = UUID.fromString(properties.get("recordId"));
        record.setRecordId(recordId);
        String dataSetName = properties.get("dataSetName");
        record.setDataSetName(dataSetName);
        double radius = Double.parseDouble(properties.get("radius"));
        record.setRadius(radius);
        double ra = Double.parseDouble(properties.get("ra"));
        record.setRa(ra);
        double pmra = Double.parseDouble(properties.get("pmra"));
        record.setPmra(pmra);
        double declination = Double.parseDouble(properties.get("declination"));
        record.setDeclination(declination);
        double pmdec = Double.parseDouble(properties.get("pmdec"));
        record.setPmdec(pmdec);
        double dec_deg = Double.parseDouble(properties.get("dec_deg"));
        record.setDec_deg(dec_deg);
        double rs_cdeg = Double.parseDouble(properties.get("rs_cdeg"));
        record.setRs_cdeg(rs_cdeg);
        double parallax = Double.parseDouble(properties.get("parallax"));
        record.setParallax(parallax);
        double distance = Double.parseDouble(properties.get("distance"));
        record.setDistance(distance);
        double radialVelocity = Double.parseDouble(properties.get("radialVelocity"));
        record.setRadialVelocity(radialVelocity);

        Color starColor = fromRGB(properties.get("starColor"));
        record.setStarColor(starColor);

        String spectralClass = properties.get("spectralClass");
        record.setSpectralClass(spectralClass);
        String notes = properties.get("notes");
        record.setNotes(notes);

        double temperature = Double.parseDouble(properties.get("temperature"));
        record.setTemperature(temperature);
        double bprp = Double.parseDouble(properties.get("bprp"));
        record.setBprp(bprp);
        double bpg = Double.parseDouble(properties.get("bpg"));
        record.setBpg(bpg);
        double grp = Double.parseDouble(properties.get("grp"));
        record.setGrp(grp);

        boolean other = Boolean.parseBoolean(properties.get("other"));
        record.setOther(other);
        boolean anomaly = Boolean.parseBoolean(properties.get("anomaly"));
        record.setAnomaly(anomaly);

        String polity = properties.get("polity");
        record.setPolity(polity);
        String worldType = properties.get("worldType");
        record.setWorldType(worldType);
        String fuelType = properties.get("fuelType");
        record.setFuelType(fuelType);
        String portType = properties.get("portType");
        record.setPortType(portType);

        String populationType = properties.get("populationType");
        record.setPopulationType(populationType);
        String techType = properties.get("techType");
        record.setTechType(techType);
        String productType = properties.get("productType");
        record.setProductType(productType);
        String milSpaceType = properties.get("milSpaceType");
        record.setMilSpaceType(milSpaceType);
        String milPlanType = properties.get("milPlanType");
        record.setMilPlanType(milPlanType);
        String luminosity = properties.get("luminosity");
        record.setLuminosity(luminosity);

        double xAct = Double.parseDouble(properties.get("xAct"));
        double yAct = Double.parseDouble(properties.get("yAct"));
        double zAct = Double.parseDouble(properties.get("zAct"));
        double[] coordinates = new double[3];
        coordinates[0] = xAct;
        coordinates[1] = yAct;
        coordinates[2] = zAct;
        record.setActualCoordinates(coordinates);

        double x = Double.parseDouble(properties.get("x"));
        double y = Double.parseDouble(properties.get("y"));
        double z = Double.parseDouble(properties.get("z"));
        Point3D point3D = new Point3D(x, y, z);
        record.setCoordinates(point3D);

        return record;
    }

    private static Color fromRGB(String colorStr) {
        String[] parts = colorStr.split(",");
        double red = Double.parseDouble(parts[0]);
        double green = Double.parseDouble(parts[1]);
        double blue = Double.parseDouble(parts[2]);
        return Color.color(red, green, blue);
    }

    public static StarDisplayRecord fromAstrographicObject(AstrographicObject astrographicObject) {
        StarDisplayRecord record = new StarDisplayRecord();

        record.setRecordId(astrographicObject.getId());
        record.setStarName(astrographicObject.getDisplayName());
        record.setDataSetName(astrographicObject.getDataSetName());
        record.setRadius(astrographicObject.getRadius());
        record.setRa(astrographicObject.getRa());
        record.setDeclination(astrographicObject.getDeclination());
        record.setPmra(astrographicObject.getPmra());
        record.setPmdec(astrographicObject.getPmdec());
        record.setDec_deg(astrographicObject.getDec_deg());
        record.setRs_cdeg(astrographicObject.getRs_cdeg());
        record.setParallax(astrographicObject.getParallax());
        record.setDistance(astrographicObject.getDistance());
        record.setRadialVelocity(astrographicObject.getRadialVelocity());
        record.setSpectralClass(astrographicObject.getSpectralClass());
        record.setNotes(astrographicObject.getNotes());
        record.setTemperature(astrographicObject.getTemperature());
        record.setBprp(astrographicObject.getBprp());
        record.setBpg(astrographicObject.getBpg());
        record.setGrp(astrographicObject.getGrp());
        record.setOther(astrographicObject.isOther());
        record.setAnomaly(astrographicObject.isAnomaly());
        record.setPolity(astrographicObject.getPolity());
        record.setWorldType(astrographicObject.getWorldType());
        record.setFuelType(astrographicObject.getFuelType());
        record.setPortType(astrographicObject.getPortType());
        record.setPopulationType(astrographicObject.getPopulationType());
        record.setTechType(astrographicObject.getTechType());
        record.setProductType(astrographicObject.getProductType());
        record.setMilSpaceType(astrographicObject.getMilSpaceType());
        record.setMilPlanType(astrographicObject.getMilPlanType());
        record.setLuminosity(astrographicObject.getLuminosity());
        record.setStarColor(astrographicObject.getStarColor());
        record.setActualCoordinates(astrographicObject.getCoordinates());

        return record;
    }

    public Map<String, String> toProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("starName", starName);
        properties.put("recordId", recordId.toString());
        properties.put("radius", String.format("%4.1f", radius));
        properties.put("ra", String.format("%4.1f", ra));
        properties.put("pmra", String.format("%4.1f", pmra));
        properties.put("pmdec", String.format("%4.1f", pmdec));
        properties.put("declination", String.format("%4.1f", declination));
        properties.put("dec_deg", String.format("%4.1f", dec_deg));
        properties.put("rs_cdeg", String.format("%4.1f", rs_cdeg));
        properties.put("parallax", String.format("%4.1f", parallax));
        properties.put("distance", String.format("%4.1f", distance));
        properties.put("radialVelocity", String.format("%4.1f", radius));
        properties.put("spectralClass", spectralClass);
        properties.put("notes", notes);
        properties.put("temperature", String.format("%4.1f", temperature));
        properties.put("bprp", String.format("%4.1f", bprp));
        properties.put("bpg", String.format("%4.1f", bpg));
        properties.put("grp", String.format("%4.1f", grp));

        properties.put("other", Boolean.toString(other));
        properties.put("anomaly", Boolean.toString(anomaly));

        properties.put("polity", polity);
        properties.put("worldType", worldType);
        properties.put("fuelType", fuelType);
        properties.put("portType", portType);
        properties.put("populationType", populationType);
        properties.put("techType", techType);
        properties.put("productType", productType);
        properties.put("milSpaceType", milSpaceType);
        properties.put("milPlanType", milPlanType);
        properties.put("luminosity", luminosity);

        properties.put("starColor", toRGB(starColor));

        properties.put("xAct", String.format("%5.1f", actualCoordinates[0]));
        properties.put("yAct", String.format("%5.1f", actualCoordinates[1]));
        properties.put("zAct", String.format("%5.1f", actualCoordinates[2]));

        properties.put("x", String.format("%5.1f", getCoordinates().getX()));
        properties.put("y", String.format("%5.1f", getCoordinates().getY()));
        properties.put("z", String.format("%5.1f", getCoordinates().getZ()));

        return properties;
    }

    private String toRGB(Color starColor) {
        return starColor.getRed() + "," + starColor.getGreen() + "," + starColor.getBlue();
    }

    /**
     * is this the center point of the diagram
     *
     * @return tru is (x,y,z) == (0,0,0)
     */
    public boolean isCenter() {
        return (Math.abs(coordinates.getX()) <= 1)
                & (Math.abs(coordinates.getY()) <= 1)
                & (Math.abs(coordinates.getZ()) <= 1);
    }

}
