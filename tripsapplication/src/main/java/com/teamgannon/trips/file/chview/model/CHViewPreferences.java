package com.teamgannon.trips.file.chview.model;

import javafx.scene.paint.Color;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The view preferences read from the ChView file
 * <p>
 * Integers: are saved as 2 byte bitfields.
 * <p>
 * Longs: are saved as 4 byte bitfields.
 * <p>
 * Strings: are saved a a 1 byte length determinator followed by that many bytes of data. This does not include
 * the null terminator. If the string is longer that 254 then a byte containing FF is stored with two bytes following
 * containing the actual length.
 * <p>
 * Floats: are unusual. They are stored as string representations. This is done because most of the floating
 * point numbers we wish to store are nice rounded numbers. It is more efficient to store them as strings
 * than 8-byte floating point values.
 * <p>
 * Colorref: this is a Windows RGB value. It is promoted to a long and saved as that.
 * <p>
 * LogFont: a binary representation of the Windows structure.
 * <p>
 * Created by larrymitchell on 2017-02-07.
 */
@Data
public class CHViewPreferences implements Serializable {

    public final static int LINKSIZE1 = 0;
    public final static int LINKSIZE2 = 1;
    public final static int LINKSIZE3 = 2;
    public final static int LINKSIZE4 = 3;

    public final static int GRIDSTYLE1 = 0;
    public final static int GRIDSTYLE2 = 1;
    public final static int GRIDSTYLE3 = 2;
    public final static int GRIDSTYLE4 = 3;

    public final static int LINKSTYLE1 = 0;
    public final static int LINKSTYLE2 = 1;
    public final static int LINKSTYLE3 = 2;
    /**
     * LinkColour x 3 |Colorref |Colour of link gradations
     */
    private final Color[] linkColor = new Color[3];
    /**
     * CentreOrds x 3 |double |Galactic coordinates of centre
     */
    private final double[] centreOrdinates = new double[3];
    /**
     * not sure what this one is for
     */
    private final double[] galCoordinates = new double[3];
    /**
     * GroupLabel x 4 |string |Names of groups
     */
    private final String[] namesOfGroups = new String[4];
    /**
     * DisplayGroup x 4 |int |Display each group or not
     */
    private final boolean[] displayFlagGroupOn = new boolean[4];
    /**
     * the routes
     */
    private final List<RouteDescriptor> routes = new ArrayList<>();
    /**
     * Grid |int |Display grid or not
     * <p>
     * first
     */
    private boolean gridOn = false;
    /**
     * GridSize |double |Granularity of grid
     * <p>
     * second
     */
    private double gridSize;
    /**
     * Link |int |Display Links or not
     * <p>
     * third
     */
    private boolean linkOn = false;
    /**
     * LinkNumbers |int |Display Link Numbers or not
     * <p>
     * fourth
     */
    private boolean displayLinkOn = false;
    /**
     * LinkSize x 4 |double |Size of each link graduation
     * <p>
     * sixth
     */
    private double @NotNull [] linksize = new double[4];
    /**
     * StarName |int |Display Star Names or not
     * <p>
     * seventh
     */
    private boolean starNameOn = false;
    /**
     * Radius |double |View Radius
     * <p>
     * eighth
     */
    private double radius;
    /**
     * Scale |int |Display Scope or note
     * <p>
     * ninth
     */
    private boolean scaleOn;
    /**
     * GridStyle |int |Style of grid lines
     * <p>
     * tenth
     */
    private int gridStyle;
    /**
     * LinkStyle x 3 |int |Style of link lines
     * <p>
     * eleven
     */
    private short @NotNull [] linkStyle = new short[3];
    /**
     * StemStyle |int |Style of stems
     * <p>
     * twelve
     */
    private short stemStyle;
    /**
     * StarOutline |int |Display star outline or not
     * <p>
     * thirteen
     */
    private boolean starOutlineOn = false;
    /**
     * RouteDisp |int |Display routes or not
     */
    private boolean routeDisplayOn = false;
    /**
     * OColour |Colorref |Colour of spectral type O stars
     * (long)
     */
    private Color oColor;
    /**
     * BColour |Colorref |
     */
    private Color bColor;
    /**
     * AColour |Colorref |
     */
    private Color aColor;
    /**
     * FColour |Colorref |
     */
    private Color fColor;
    /**
     * GColour |Colorref |
     */
    private Color gColor;
    /**
     * KColour |Colorref |
     */
    private Color kColor;
    /**
     * MColour |Colorref |
     */
    private Color mColor;
    /**
     * XColour |Colorref |
     */
    private Color xColor;
    /**
     * BackColour |Colorref |Colour of background
     */
    private Color backgroundColor;
    /**
     * TextColour |Colorref |Colour of text
     */
    private Color textColor;
    /**
     * LinkNumColour |Colorref |Colour of link numbers
     */
    private Color linkNumberColor;
    /**
     * GridColour |Colorref |Colour of grid
     */
    private Color gridColor;
    /**
     * StemColour |Colorref |Colour of stems
     */
    private Color stemColor;
    /**
     * ORad |int |Radius of spectral class O stars
     */
    private short oRadius;
    /**
     * BRad |int |
     */
    private short bRadius;
    /**
     * ARad |int |
     */
    private short aRadius;
    /**
     * FRad |int |
     */
    private short fRadius;
    /**
     * GRad |int |
     */
    private short gRadius;
    /**
     * KRad |int |
     */
    private short kRadius;
    /**
     * MRad |int |
     */
    private short mRadius;
    /**
     * XRad |int |
     */
    private short xRadius;
    /**
     * DwarfRad |int |
     */
    private short dwarfRadius;
    /**
     * GiantRad |int |
     */
    private short giantRadius;
    /**
     * SuperGiantRad |int |
     */
    private short superGiantRadius;
    /**
     * theta |double |Rotational position
     */
    private double theta;
    /**
     * phi |double |
     */
    private double phi;
    /**
     * rho |double |
     */
    private double rho;
    /**
     * tscale |double |Angular scales
     */
    private double tScale;
    /**
     * pscale |double |
     */
    private double pScale;
    /**
     * rscale |double |
     */
    private double rScale;
    /**
     * xscale |double |Display scales
     */
    private double xScale;
    /**
     * yscale |double |
     */
    private double yScale;


    //  -------------------- public special accessors ------------------- //

    public static @NotNull CHViewPreferences earthNormal() {
        CHViewPreferences vp = new CHViewPreferences();

        return vp;
    }

    public void addRoute(int number, String name, byte[] rawColor, short style) {
        RouteDescriptor routeDescriptor = new RouteDescriptor();
        routeDescriptor.setNumber(number);
        routeDescriptor.setName(name);
        routeDescriptor.setStyle(style);
        routeDescriptor.setColor(getColor(rawColor));
        routes.add(routeDescriptor);
    }

    public void setLinkSizes(double linkSize1, double linkSize2, double linkSize3, double linkSize4) {
        this.linksize[LINKSIZE1] = linkSize1;
        this.linksize[LINKSIZE2] = linkSize2;
        this.linksize[LINKSIZE3] = linkSize3;
        this.linksize[LINKSIZE4] = linkSize4;
    }

    public void setLinkStyles(short linkStyle1, short linkStyle2, short linkStyle3) {
        this.linkStyle[LINKSTYLE1] = linkStyle1;
        this.linkStyle[LINKSTYLE2] = linkStyle2;
        this.linkStyle[LINKSTYLE3] = linkStyle3;
    }

    public void setOColor(byte[] colorBytes) {
        oColor = getColor(colorBytes);
    }

    public void setBColor(byte[] colorBytes) {
        bColor = getColor(colorBytes);
    }

    public void setAColor(byte[] colorBytes) {
        aColor = getColor(colorBytes);
    }

    public void setFColor(byte[] colorBytes) {
        fColor = getColor(colorBytes);
    }

    public void setGColor(byte[] colorBytes) {
        gColor = getColor(colorBytes);
    }

    public void setKColor(byte[] colorBytes) {
        kColor = getColor(colorBytes);
    }

    public void setMColor(byte[] colorBytes) {
        mColor = getColor(colorBytes);
    }

    public void setXColor(byte[] colorBytes) {
        xColor = getColor(colorBytes);
    }

    public void setBackgroudColor(byte[] colorBytes) {
        backgroundColor = getColor(colorBytes);
    }

    public void setTextColor(byte[] colorBytes) {
        textColor = getColor(colorBytes);
    }

    public void setLinkNumberColor(byte[] colorBytes) {
        linkNumberColor = getColor(colorBytes);
    }

    public void setLinkColors(byte[] colorBytes1, byte[] colorBytes2, byte[] colorBytes3) {
        linkColor[0] = getColor(colorBytes1);
        linkColor[1] = getColor(colorBytes2);
        linkColor[2] = getColor(colorBytes3);
    }

    public void setGridColor(byte[] colorBytes) {
        gridColor = getColor(colorBytes);
    }

    public void setStemColor(byte[] colorBytes) {
        stemColor = getColor(colorBytes);
    }

    public void setCentreOrdinate(@NotNull String ord1, @NotNull String ord2, @NotNull String ord3) {
        centreOrdinates[0] = Double.parseDouble(ord1);
        centreOrdinates[1] = Double.parseDouble(ord2);
        centreOrdinates[2] = Double.parseDouble(ord3);
    }

    public void setGalCoordinates(@NotNull String gal1, @NotNull String gal2, @NotNull String gal3) {
        galCoordinates[0] = Double.parseDouble(gal1);
        galCoordinates[1] = Double.parseDouble(gal2);
        galCoordinates[2] = Double.parseDouble(gal3);
    }

    public void setNamesOfGroups(String group1, String group2, String group3, String group4) {
        namesOfGroups[0] = group1;
        namesOfGroups[1] = group2;
        namesOfGroups[2] = group3;
        namesOfGroups[3] = group4;
    }


    //  -------------------- private helpers ------------------- //

    public void setDisplayFlagGroupOn(boolean group1, boolean group2, boolean group3, boolean group4) {
        displayFlagGroupOn[0] = group1;
        displayFlagGroupOn[1] = group2;
        displayFlagGroupOn[2] = group3;
        displayFlagGroupOn[3] = group4;
    }

    public @NotNull Color getColor(byte[] colorInBytes) {

        // int i2 = b & 0xFF;
        int red = (colorInBytes[0] & 0xFF);
        int green = (colorInBytes[1] & 0xFF);
        int blue = (colorInBytes[2] & 0xFF);

        return Color.rgb(red, green, blue);
    }

}
