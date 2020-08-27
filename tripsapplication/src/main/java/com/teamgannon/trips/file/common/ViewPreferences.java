package com.teamgannon.trips.file.common;

import javafx.scene.paint.Color;
import lombok.Data;

@Data
public class ViewPreferences {

    /**
     * Display grid or not
     */
    private boolean gridOn = true;

    /**
     * Route Display or not
     */
    private boolean routeDisplayOn = true;

    /**
     * Display Star Names or not
     */
    private boolean starNameOn = false;

    /**
     * Scale on or off
     */
    private boolean scaleOn;

    /**
     * OColour
     * Colour of spectral type O stars
     */
    private Color oColor;

    /**
     * BColour
     */
    private Color bColor;


    /**
     * AColour
     */
    private Color aColor;

    /**
     * FColour
     */
    private Color fColor;

    /**
     * GColour
     */
    private Color gColor;

    /**
     * KColour
     */
    private Color kColor;

    /**
     * MColour
     */
    private Color mColor;

    /**
     * XColour
     */
    private Color xColor;

    /**
     * BackColour
     */
    private Color backgroundColor;

    /**
     * TextColour
     */
    private Color textColor;

    /**
     * GridColour
     */
    private Color gridColor;

    /**
     * ORad Radius of spectral class O stars
     */
    private short oRadius;

    /**
     * BRad
     */
    private short bRadius;

    /**
     * ARad
     */
    private short aRadius;

    /**
     * FRad
     */
    private short fRadius;

    /**
     * GRad
     */
    private short gRadius;

    /**
     * KRad
     */
    private short kRadius;

    /**
     * MRad
     */
    private short mRadius;

    /**
     * XRad
     */
    private short xRadius;

    /**
     * DwarfRad
     */
    private short dwarfRadius;

    /**
     * GiantRad
     */
    private short giantRadius;

    /**
     * SuperGiantRad
     */
    private short superGiantRadius;

}
