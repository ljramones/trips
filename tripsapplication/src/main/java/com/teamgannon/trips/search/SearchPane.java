package com.teamgannon.trips.search;

import com.teamgannon.trips.events.ExportQueryEvent;
import com.teamgannon.trips.events.ShowStellarDataEvent;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.components.*;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class SearchPane extends VBox {

    private final SearchContext searchContext;
    private final ApplicationEventPublisher eventPublisher;
    private final DistanceSelectionPanel d2EarthSlider;
    private final StellarClassSelectionPanel stellarClassSelectionPanel = new StellarClassSelectionPanel();
    private final CategorySelectionPanel categorySelectionPanel = new CategorySelectionPanel();
    private final SpectralComponentSelectionPanel spectralComponentSelectionPanel = new SpectralComponentSelectionPanel();
    private DataSetPanel dataSetChoicePanel;

    @FXML
    private GridPane queryBox;
    @FXML
    private Pane datasetBox;
    @FXML
    private Pane distanceBox;
    @FXML
    private Pane stellarClassBox;
    @FXML
    private Pane categoryBox;
    @FXML
    private Pane spectralComponentBox;

    /**
     * constructor
     *
     * @param searchContext  the search context
     * @param eventPublisher the event publisher
     */
    public SearchPane(@NotNull SearchContext searchContext,
                      ApplicationEventPublisher eventPublisher) {
        this.searchContext = searchContext;
        this.eventPublisher = eventPublisher;

        DistanceRange distanceRange = DistanceRange
                .builder()
                .min(0)
                .lowValue(0)
                .highValue(20)
                .max(20)
                .build();

        DataSetDescriptor descriptor = searchContext.getDataSetDescriptor();
        if (searchContext.getDataSetDescriptor() != null) {
            log.info("Dataset distance range:{}", descriptor.getDistanceRange());
            distanceRange.setMax(descriptor.getDistanceRange());
        }

        d2EarthSlider = new DistanceSelectionPanel(searchContext.getAstroSearchQuery().getUpperDistanceLimit(), distanceRange);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SearchPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load SearchPane.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        dataSetChoicePanel = new DataSetPanel(searchContext, eventPublisher);
        // Dataset and Distance are essential - keep expanded
        datasetBox.getChildren().add(createTitledPane("Dataset", dataSetChoicePanel.getPane(), true));
        distanceBox.getChildren().add(createTitledPane("Distance Range", d2EarthSlider.getPane(), true));

        // Stellar filters - collapsed by default
        stellarClassBox.getChildren().add(createTitledPane("Stellar Class", stellarClassSelectionPanel.getPane(), false));
        categoryBox.getChildren().add(createTitledPane("Star Category", categorySelectionPanel.getPane(), false));

        // Advanced spectral component filter
        spectralComponentBox.getChildren().add(createTitledPane("Advanced Spectral Filter", spectralComponentSelectionPanel, false));
    }

    /**
     * Create a collapsible TitledPane wrapper for a panel.
     *
     * @param title    the title for the section
     * @param content  the content node to wrap
     * @param expanded whether the section starts expanded
     * @return the TitledPane wrapper
     */
    private TitledPane createTitledPane(String title, Node content, boolean expanded) {
        TitledPane titledPane = new TitledPane(title, content);
        titledPane.setExpanded(expanded);
        titledPane.setCollapsible(true);
        titledPane.setAnimated(true);
        return titledPane;
    }

    public void setDataSetContext(@NotNull DataSetDescriptor descriptor) {
        dataSetChoicePanel.setDataSetContext(descriptor);
        d2EarthSlider.setDataSetDescriptor(descriptor);
    }

    public void refreshDataSets() {
        dataSetChoicePanel.refreshDatasetChoices();
        DataSetDescriptor descriptor = dataSetChoicePanel.getSelected();
        if (descriptor != null) {
            d2EarthSlider.setDataSetDescriptor(descriptor);
        }
    }


    public void updateDataContext(@NotNull DataSetDescriptor dataSetDescriptor) {
        dataSetChoicePanel.updateDataContext(dataSetDescriptor);
    }


    public void removeDataset(DataSetDescriptor dataSetDescriptor) {
        dataSetChoicePanel.removeDataset(dataSetDescriptor);
    }

    public AstroSearchQuery runQuery(boolean showPlot, boolean showTable, boolean doExport) {

        DataSetDescriptor descriptor = dataSetChoicePanel.getSelected();

        if (descriptor == null) {
            showErrorAlert("Run Query", "Please select a dataset first");
        } else {
            // pull derived query
            AstroSearchQuery newQuery = createSearchQuery();
            log.info("New search request:{}", newQuery);

            // process file location for export if selected
            if (doExport) {
                eventPublisher.publishEvent(new ExportQueryEvent(this, newQuery));
            }

            if (newQuery.getDataSetContext().getDescriptor() != null) {
                // update main screen
                eventPublisher.publishEvent(new ShowStellarDataEvent(this, newQuery, showPlot, showTable));
            } else {
                showErrorAlert("Query Dialog", "You must specify a dataset!");
            }
            return newQuery;
        }
        return searchContext.getAstroSearchQuery();
    }

    ///////////////   Query Construction   //////////////////

    /**
     * construct the query
     *
     * @return the search query to feed to elasticsearch
     */
    private AstroSearchQuery createSearchQuery() {
        AstroSearchQuery astroSearchQuery = searchContext.getAstroSearchQuery();

        DataSetDescriptor descriptor = dataSetChoicePanel.getSelected();
        astroSearchQuery.setDescriptor(descriptor);

        astroSearchQuery.setLowerDistanceLimit(d2EarthSlider.getDistance().getLowValue());
        astroSearchQuery.setUpperDistanceLimit(d2EarthSlider.getDistance().getHighValue());
        astroSearchQuery.setRealStars(categorySelectionPanel.isRealStars());

        getStellarTypes(astroSearchQuery);
        getSpectralComponentFilter(astroSearchQuery);

        return astroSearchQuery;
    }

    private void getStellarTypes(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearStellarTypes();
        List<String> stellarTypes = stellarClassSelectionPanel.getSelection();
        if (stellarClassSelectionPanel.isSelected()) {
            astroSearchQuery.addStellarTypes(stellarTypes);
        }
    }

    private void getSpectralComponentFilter(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearSpectralComponentFilter();
        if (spectralComponentSelectionPanel.hasSelections()) {
            astroSearchQuery.setSpectralComponentFilter(
                    spectralComponentSelectionPanel.getSelectedClasses(),
                    spectralComponentSelectionPanel.getSelectedSubtypes(),
                    spectralComponentSelectionPanel.getSelectedLuminosityClasses()
            );
        }
    }

}
