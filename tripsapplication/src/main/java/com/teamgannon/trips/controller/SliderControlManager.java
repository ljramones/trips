package com.teamgannon.trips.controller;

import com.teamgannon.trips.controller.shared.SharedUIState;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.SplitPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.teamgannon.trips.controller.MainPane.SIDE_PANEL_SIZE;

@Slf4j
@Component
public class SliderControlManager {
    private SplitPane mainSplitPane;
    @Getter
    private ChangeListener<Number> sliderChangeListener;
    private final SharedUIState sharedUIState;

    public SliderControlManager(SharedUIState sharedUIState) {
        this.sharedUIState = sharedUIState;
    }

    public void initialize(SplitPane mainSplitPane) {
        this.mainSplitPane = mainSplitPane;
        this.sliderChangeListener = (obs, oldPos, newPos) -> adjustSliderForSidePanel(newPos.doubleValue());
        setSliderControl();
    }

    private void setSliderControl() {
        DoubleProperty splitPaneDividerPosition = mainSplitPane.getDividers().get(0).positionProperty();
        splitPaneDividerPosition.addListener(sliderChangeListener);
    }

    private void adjustSliderForSidePanel(double spPosition) {
        if (!sharedUIState.isSidePaneOn()) {
            mainSplitPane.setDividerPosition(0, 1);
        } else {
            if (spPosition > .95) {
                mainSplitPane.setDividerPosition(0, 1);
            } else {
                double currentWidth = mainSplitPane.getWidth();
                double exposedSettingsWidth = (1 - spPosition) * currentWidth;
                if (exposedSettingsWidth > SIDE_PANEL_SIZE || exposedSettingsWidth < SIDE_PANEL_SIZE) {
                    double adjustedWidthRatio = SIDE_PANEL_SIZE / currentWidth;
                    mainSplitPane.setDividerPosition(0, 1 - adjustedWidthRatio);
                }
            }
        }
    }

    public void removeSliderChangeListener() {
        DoubleProperty splitPaneDividerPosition = mainSplitPane.getDividers().get(0).positionProperty();
        splitPaneDividerPosition.removeListener(sliderChangeListener);
    }

    public void addSliderChangeListener() {
        DoubleProperty splitPaneDividerPosition = mainSplitPane.getDividers().get(0).positionProperty();
        splitPaneDividerPosition.addListener(sliderChangeListener);
    }

}

