package com.teamgannon.trips.dialogs.dataset.model;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;

public interface LoadUpdateListener {

    void update(DataSetDescriptor descriptor);

}
