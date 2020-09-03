package com.teamgannon.trips.listener;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;

public interface DataSetChangeListener {


    /**
     * add the data set descriptor
     *
     * @param dataSetDescriptor the dataset descriptor
     */
    void addDataSet(DataSetDescriptor dataSetDescriptor);

    /**
     * remove the data set descriptor
     *
     * @param dataSetDescriptor the dataset descriptor
     */
    void removeDataSet(DataSetDescriptor dataSetDescriptor);

    /**
     * set the contextual dataset
     *
     * @param descriptor the dataset descript that is in context
     */
    void setContextDataSet(DataSetDescriptor descriptor);

}
