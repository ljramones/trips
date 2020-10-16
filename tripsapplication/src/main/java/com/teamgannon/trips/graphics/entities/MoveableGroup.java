package com.teamgannon.trips.graphics.entities;


import javafx.scene.Group;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import lombok.Data;

@Data
public class MoveableGroup extends Group {

    private String whatAmI;

    /**
     * used to translate an object around the system
     */
    private Translate t = new Translate();

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


}

