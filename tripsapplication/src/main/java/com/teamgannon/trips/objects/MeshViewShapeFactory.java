package com.teamgannon.trips.objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import java.io.IOException;

public class MeshViewShapeFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MeshViewShapeFactory.class);

    public Group starCentral() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("centralStar.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("failed to load the star:" + e.getMessage());
            return null;
        }
    }

    public MeshView star4pt() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("star4pt.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("failed to load the star:" + e.getMessage());
            return null;
        }
    }

    public Group star5pt() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("star5pt.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("failed to load the star:" + e.getMessage());
            return null;
        }
    }

    public Group starMoravian() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("moravian.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("failed to load the star:" + e.getMessage());
            return null;
        }
    }

    public MeshView pyramid() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("pyramid.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView cube() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("cube.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView dodecahedron() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("dodecahedron.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView icosahedron() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("icosahedron.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView octahedron() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("octahedron.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }


    public MeshView tetrahedron() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("tetrahedron.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView geometric0() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("geometric0.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView geometric1() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("geometric1.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView geometric2() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("geometric2.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView geometric3() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("geometric3.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView geometric4() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("geometric4.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView geometric5() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("geometric5.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView geometric6() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("geometric6.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView geometric7() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("geometric7.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

    public MeshView geometric8() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("geometric8.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

}
