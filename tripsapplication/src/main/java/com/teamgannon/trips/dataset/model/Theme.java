package com.teamgannon.trips.dataset.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dataset.enums.GridLines;
import com.teamgannon.trips.dataset.enums.GridShape;
import com.teamgannon.trips.routing.RouteDefinition;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;


/**
 * Describes a theme for a dataset
 * <p>
 * Each user of TRIPS has preferences for things like colors, sizes, fonts, etc.  That user personalization
 * information should be stored in a settings object that controls the appearance and actions of the program
 * for any given user/installation.  Call this set of options a “Theme” – that name works as well as anything
 * for me to describe the function of this data object.
 * <p>
 * However, different data-sets can serve very different purposes while displaying the same or similar data.
 * Consider if someone has a set of stellar data that documents a particular gaming universe of a particular
 * set of players.  The person could also have a stellar data set, with the same stars that documents the
 * Caine Riordan universe, and a third set that documents David Weber’s Honorverse.  If it was me, I would
 * want the three data sets to LOOK DIFFERENT so that I could decide which one I was   playing with at any
 * given moment by just looking at it.  Therefore, the TRIPS data-set object needs to contain a Theme object.
 * The programmer can give the user the option to “ignore embedded themes and use my settings always” in
 * the settings dialog for the program, but the default should allow a theme to be carried along with the
 * rest of the data set.
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Slf4j
@Data
public class Theme {

    /**
     * used for JSON serialization
     */
    private final static ObjectMapper mapper = new ObjectMapper();

    /**
     * the name of the theme
     */
    private String themeName;

    /**
     * Boolean Display Star Names or not
     */
    private boolean dispStarName = true;

    /**
     * View Radius in light years
     */
    private double viewRadius = 20.0;

    /**
     * Boolean Display Scale ruler bars or not
     */
    private boolean displayScale = true;

    /**
     * Dist between hash marks on X ruler bar in LY
     */
    private double xscale = 1.5;

    /**
     * Dist between hash marks on Y ruler bar in LY
     */
    private double yscale = 1.5;

    /**
     * Color of background
     */
    private double @NotNull [] backColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Color of text
     */
    private double @NotNull [] textColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * StarName font
     */
    private @NotNull FontDescriptor starFont = new FontDescriptor("Arial", 8);

    /**
     * Galactic X coordinates of center of view in LY
     */
    private double centerX = 123.456;

    /**
     * Galactic Y coordinates of center of view in LY
     */
    private double centerY = 22.33445;

    /**
     * Galactic Z coordinates of center of view in LY
     */
    private double centerZ = -32.1098;

    /**
     * Rotation of view in the X/Y plane in degrees
     */
    private double theta = 90.543;

    /**
     * Rotation of view in the X/Z plane in degrees
     */
    private double phi = 15.678;

    /**
     * Rotation of view in the Y/Z plane in degrees
     */
    private double rho = -20.345;

    /**
     * display flag for grid
     */
    private boolean displayGrid = true;

    /**
     * Granularity of grid in LY
     */
    private int gridSize = 5;

    /**
     * Shape of grid, rectangular or polar
     */
    private @NotNull GridShape gridShape = GridShape.Rectangular;

    /**
     * Line style, solid or dotted for the grid lines
     */
    private @NotNull GridLines gridLines = GridLines.Solid;

    /**
     * Line style, solid or dotted for the stem lines
     */
    private @NotNull GridLines stemLines = GridLines.Solid;

    /**
     * Hexadecimal Color value of grid lines
     */
    private double @NotNull [] gridLineColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Hexadecimal Color value of stem lines
     */
    private double @NotNull [] stemColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Display star outlines or not
     */
    private boolean starOutline = true;

    /**
     * Color of spectral type O stars
     */
    private double @NotNull [] oColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Color of spectral type B stars
     */
    private double @NotNull [] bColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Color of spectral type A stars
     */
    private double @NotNull [] aColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Color of spectral type F stars
     */
    private double @NotNull [] fColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Color of spectral type G stars
     */
    private double @NotNull [] gColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Color of spectral type K stars
     */
    private double @NotNull [] kColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Color of spectral type M stars
     */
    private double @NotNull [] mColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Color of spectral type X stars
     */
    private double @NotNull [] xColor = new double[]{0xAA, 0xBB, 0xCC};

    /**
     * Radius of spectral class O stars in pixels
     */
    private int oRad = 9;

    /**
     * Radius of spectral class B stars in pixels
     */
    private int bRad = 8;

    /**
     * Radius of spectral class A stars in pixels
     */
    private int aRad = 7;

    /**
     * Radius of spectral class F stars in pixels
     */
    private int fRad = 6;

    /**
     * Radius of spectral class G stars in pixels
     */
    private int gRad = 5;

    /**
     * Radius of spectral class K stars in pixels
     */
    private int kRad = 4;

    /**
     * Radius of spectral class M stars in pixels
     */
    private int mRad = 3;

    /**
     * Radius of spectral class X stars in pixels
     */
    private int xRad = 3;

    /**
     * Radius of spectral class Dwwarf stars in pixels
     */
    private int dwarfRad = 1;

    /**
     * Radius of spectral class Giant stars in pixels
     */
    private int giantRad = 10;

    /**
     * Radius of spectral class Super Giant stars in pixels
     */
    private int superGiantRad = 12;

    /**
     * An array of Link objects which define links to be
     * drawn between stars depending on their distance from
     * each other. UI must enforce no overlapping max and
     * min distances.
     */
    private @NotNull List<Link> linkList = new ArrayList<>();

    /**
     * Contains an array of route display definitions
     * Programmer determines max number allowed
     * This merely controls the display of routes of type, not
     * the storage of the routes themselves.
     */
    private @NotNull Map<UUID, RouteDefinition> routeDescriptorList = new HashMap<>();

    /**
     * list of political entities
     * <p>
     * Possible Polities which objects may belong to
     */
    private @NotNull List<Polity> polities = new ArrayList<>();

    //////////////////////////

    public String convertToJson() {
        return convertToJson(this);
    }

    public String convertToJson(Theme theme) {
        try {
            String themeStr = mapper.writeValueAsString(theme);
            log.debug("serialized as:" + themeStr);
            return themeStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", theme, e.getMessage());
            return "";
        }
    }

    public @Nullable Theme toTheme(String parametersStr) {
        try {
            return mapper.readValue(parametersStr, Theme.class);
        } catch (IOException e) {
            log.error("couldn't deserialize this {} because of {}:", parametersStr, e.getMessage());
            return null;
        }
    }

}























