package com.teamgannon.trips.graphics.entities;

import javafx.scene.transform.Rotate;
import lombok.Data;

@Data
public class RxTxGroup extends MoveableGroup {

    public Rotate rx = new Rotate();
    public Rotate ry = new Rotate();
    public Rotate rz = new Rotate();

    {
        rx.setAxis(Rotate.X_AXIS);
    }

    {
        ry.setAxis(Rotate.Y_AXIS);
    }

    {
        rz.setAxis(Rotate.Z_AXIS);
    }

    public RxTxGroup() {
        super();
        getTransforms().addAll(rz, ry, rx);
    }

    public void setRotate(double x, double y, double z) {
        rx.setAngle(x);
        ry.setAngle(y);
        rz.setAngle(z);
    }

    public void setRotateX(double x) {
        rx.setAngle(x);
    }

    public void setRotateY(double y) {
        ry.setAngle(y);
    }

    public void setRotateZ(double z) {
        rz.setAngle(z);
    }

    public void setRx(double x) {
        rx.setAngle(x);
    }

    public void setRy(double y) {
        ry.setAngle(y);
    }

    public void setRz(double z) {
        rz.setAngle(z);
    }

}
