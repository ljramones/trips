package com.teamgannon.trips.search;

import lombok.extern.slf4j.Slf4j;

/**
 * Used to keep track of what we have searched for
 * <p>
 * Created by larrymitchell on 2017-04-19.
 */
@Slf4j
public class SearchContext {

    private String dataset;

    private AstroSearchQuery astroSearchQuery = new AstroSearchQuery();

    public AstroSearchQuery getAstroSearchQuery() {
        return astroSearchQuery;
    }

    public void setAstroSearchQuery(AstroSearchQuery astroSearchQuery) {
        this.astroSearchQuery = astroSearchQuery;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public boolean isDatasetPresent() {
        return dataset != null;
    }


}
