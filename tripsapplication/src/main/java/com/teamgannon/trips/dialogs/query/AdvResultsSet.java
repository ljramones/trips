package com.teamgannon.trips.dialogs.query;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class AdvResultsSet {

    @Builder.Default
    private boolean dismissed = false;

    /**
     * false is the query is not valid
     */
    @Builder.Default
    private boolean queryValid = false;

    /**
     * list of validation errors if the query was found to be invalid
     */
    @Builder.Default
    private List<String> validationErrors = new ArrayList<>();

    /**
     * true if the query returned results
     */
    @Builder.Default
    private boolean resultsFound = false;

    /**
     * if true, view stars in table
     */
    @Builder.Default
    private boolean plotStars = false;

    /**
     * if true, plot stars
     */
    @Builder.Default
    private boolean viewStars = false;

    /**
     * the list of results found
     */
    @Builder.Default
    private List<AstrographicObject> starsFound = new ArrayList<>();

    /**
     * the dataset descriptor that we selected
     */
    private DataSetDescriptor dataSetDescriptor;

}
