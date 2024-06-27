package com.teamgannon.trips.controller.toolbar;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.controller.shared.SharedUIFunctions;
import com.teamgannon.trips.controller.shared.SharedUIState;
import com.teamgannon.trips.events.UIStateChangeEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.zondicons.Zondicons;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ToolbarController {

    private final SharedUIState sharedUIState;
    private final SharedUIFunctions sharedUIFunctions;

    @FXML
    public ToolBar toolBar;
    @FXML
    public ToggleButton togglePolityBtn;
    @FXML
    public ToggleButton toggleRoutesBtn;
    @FXML
    public ToggleButton toggleTransitsBtn;
    @FXML
    public ToggleButton toggleStarBtn;
    @FXML
    public ToggleButton toggleGridBtn;
    @FXML
    public ToggleButton toggleStemBtn;
    @FXML
    public ToggleButton toggleScaleBtn;
    @FXML
    public ToggleButton toggleSettings;
    @FXML
    public ToggleButton toggleZoomInBtn;
    @FXML
    private Button plotButton;
    @FXML
    private ToggleButton toggleLabelsBtn;
    @FXML
    private ToggleButton toggleZoomOutBtn;


    public ToolbarController(SharedUIState sharedUIState, SharedUIFunctions sharedUIFunctions) {
        this.sharedUIState = sharedUIState;
        this.sharedUIFunctions = sharedUIFunctions;
    }

    @FXML
    public void initialize() {
        setButtons();
        this.toolBar.setPrefWidth(Universe.boxWidth + 20);
    }

    private void setButtons() {

        final Image toggleRoutesBtnGraphic = new Image("/images/buttons/tb_routes.gif");
        final ImageView toggleRoutesBtnImage = new ImageView(toggleRoutesBtnGraphic);
        toggleRoutesBtn.setGraphic(toggleRoutesBtnImage);

        FontIcon fontIconZoomIn = new FontIcon(Zondicons.ZOOM_IN);
        toggleZoomInBtn.setGraphic(fontIconZoomIn);

        FontIcon fontIconZoomOut = new FontIcon(Zondicons.ZOOM_OUT);
        toggleZoomOutBtn.setGraphic(fontIconZoomOut);

        final Image toggleGridBtnGraphic = new Image("/images/buttons/tb_grid.gif");
        final ImageView toggleGridBtnImage = new ImageView(toggleGridBtnGraphic);
        toggleGridBtn.setGraphic(toggleGridBtnImage);

        plotButton.setDisable(true);
    }


    @FXML
    private void plotStars() {
        sharedUIFunctions.plotStars();
    }

    @FXML
    private void toggleLabels() {
        sharedUIFunctions.toggleLabels();
    }

    @FXML
    private void togglePolities() {
        sharedUIFunctions.togglePolities();
    }

    @FXML
    public void toggleRoutes(ActionEvent actionEvent) {
        sharedUIFunctions.toggleRoutes();
    }

    @FXML
    public void toggleTransitAction(ActionEvent actionEvent) {
        sharedUIFunctions.toggleTransit();
    }

    @FXML
    public void toggleStars(ActionEvent actionEvent) {
        sharedUIFunctions.toggleStars();
    }

    @FXML
    private void zoomOut() {
        sharedUIFunctions.zoomOut();
    }

    @FXML
    private void zoomIn() {
        sharedUIFunctions.zoomIn();
    }


    @FXML
    public void toggleGrid(ActionEvent actionEvent) {
        sharedUIFunctions.toggleGrid();
    }

    @FXML
    public void toggleGridExtensions(ActionEvent actionEvent) {
        sharedUIFunctions.toggleGridExtensions();
    }

    @FXML
    public void toggleScale(ActionEvent actionEvent) {
        sharedUIFunctions.toggleScale();
    }

    @FXML
    public void toggleSidePane(ActionEvent actionEvent) {
        sharedUIFunctions.toggleSidePane();
    }

    @EventListener
    public void handleUIStateChangeEvent(UIStateChangeEvent event) {
        switch (event.getElement()) {
            case GRID -> toggleGridBtn.setSelected(event.isState());
            case LABELS -> toggleLabelsBtn.setSelected(event.isState());
            case POLITIES -> togglePolityBtn.setSelected(event.isState());
            case STARS -> toggleStarBtn.setSelected(event.isState());
            case ROUTES -> toggleRoutesBtn.setSelected(event.isState());
            case TRANSITS -> toggleTransitsBtn.setSelected(event.isState());
            case TRANSIT_LENGTHS -> {
            }
            case ROUTE_LENGTHS -> {
            }
            case SIDE_PANE -> {
            }
            case TOOLBAR -> {
            }
            case STATUS_BAR -> {
            }
            case SCALE -> toggleScaleBtn.setSelected(event.isState());
            case EXTENSIONS -> toggleStemBtn.setSelected(event.isState());
            default -> log.error("Unexpected value: {}", event.getElement());
        }

    }


}