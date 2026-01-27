package com.teamgannon.trips.screenobjects.planetary;

import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Shows a summary of what's visible in the sky from the planet's surface.
 * Displays visible star count, host star position, and sibling planets.
 */
@Slf4j
@Component
public class SkyOverviewPane extends VBox {

    private final Label visibleStarsLabel = new Label("-");
    private final Label magnitudeLimitLabel = new Label("-");
    private final Label hostStarPositionLabel = new Label("-");
    private final Label brightestStarLabel = new Label("-");
    private final Label siblingPlanetsLabel = new Label("-");

    public SkyOverviewPane() {
        setPadding(new Insets(10));
        setSpacing(5);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        int row = 0;

        // Visible stars count
        grid.add(new Label("Visible Stars:"), 0, row);
        grid.add(visibleStarsLabel, 1, row++);

        // Magnitude limit
        grid.add(new Label("Mag Limit:"), 0, row);
        grid.add(magnitudeLimitLabel, 1, row++);

        // Separator
        grid.add(new Separator(), 0, row++, 2, 1);

        // Host star (sun) position
        grid.add(new Label("Host Star:"), 0, row);
        grid.add(hostStarPositionLabel, 1, row++);

        // Brightest star
        grid.add(new Label("Brightest:"), 0, row);
        grid.add(brightestStarLabel, 1, row++);

        // Sibling planets
        grid.add(new Label("Planets Visible:"), 0, row);
        grid.add(siblingPlanetsLabel, 1, row++);

        getChildren().add(grid);
    }

    /**
     * Set the planetary context and brightest stars list.
     */
    public void setContext(PlanetaryContext context, List<PlanetarySkyRenderer.BrightStarEntry> brightestStars) {
        int starCount = brightestStars != null ? brightestStars.size() : 0;
        setContext(context, brightestStars, starCount);
    }

    public void setContext(PlanetaryContext context,
                           List<PlanetarySkyRenderer.BrightStarEntry> brightestStars,
                           int visibleStarCount) {
        if (context == null) {
            clear();
            return;
        }

        // Magnitude limit
        magnitudeLimitLabel.setText(String.format("%.1f", context.getMagnitudeLimit()));

        // Visible stars count
        visibleStarsLabel.setText(String.valueOf(Math.max(visibleStarCount, 0)));

        // Host star position (computed)
        String dayNight = context.isDaylight() ? "Day" : "Night";
        double altitude = context.getHostStarAltitudeDeg();
        if (Double.isNaN(altitude)) {
            hostStarPositionLabel.setText(dayNight);
        } else {
            hostStarPositionLabel.setText("%s (alt %.1f deg)".formatted(dayNight, altitude));
        }

        // Brightest star
        if (brightestStars != null && !brightestStars.isEmpty()) {
            PlanetarySkyRenderer.BrightStarEntry brightest = brightestStars.get(0);
            brightestStarLabel.setText("%s (%.1f)".formatted(
                    brightest.getName(), brightest.getApparentMagnitude()));
        } else {
            brightestStarLabel.setText("-");
        }

        // Sibling planets (TODO: implement)
        siblingPlanetsLabel.setText("0");

        log.debug("Updated sky overview: {} visible stars", visibleStarCount);
    }

    /**
     * Clear all displayed information.
     */
    public void clear() {
        visibleStarsLabel.setText("-");
        magnitudeLimitLabel.setText("-");
        hostStarPositionLabel.setText("-");
        brightestStarLabel.setText("-");
        siblingPlanetsLabel.setText("-");
    }

    // Host star position is computed via nightsky math in PlanetarySkyModelBuilder.
}
