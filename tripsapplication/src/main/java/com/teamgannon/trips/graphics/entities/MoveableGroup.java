package com.teamgannon.trips.graphics.entities;


import javafx.scene.Group;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import lombok.Data;

@Data
public class MoveableGroup extends Group {

    protected String whatAmI;

    /**
     * used to translate an object around the system
     */
    protected Translate t = new Translate();

    /**
     * used to rescale the object
     */
    public Scale s = new Scale();

    public MoveableGroup() {
        super();
        getTransforms().addAll(t, s);
    }

    public void setTranslate(double x, double y, double z) {
        t.setX(x);
        t.setY(y);
        t.setZ(z);
    }

    public void setTranslate(double x, double y) {
        t.setX(x);
        t.setY(y);
    }

    public void setTx(double x) {
        t.setX(x);
    }

    public void setTy(double y) {
        t.setY(y);
    }

    public void setTz(double z) {
        t.setZ(z);
    }


    public void setScale(double scaleFactor) {
        s.setX(scaleFactor);
        s.setY(scaleFactor);
        s.setZ(scaleFactor);
    }

    public void setScale(double x, double y, double z) {
        s.setX(x);
        s.setY(y);
        s.setZ(z);
    }

    // Use these methods instead:
    public void setSx(double x) {
        s.setX(x);
    }

    public void setSy(double y) {
        s.setY(y);
    }

    public void setSz(double z) {
        s.setZ(z);
    }

}

