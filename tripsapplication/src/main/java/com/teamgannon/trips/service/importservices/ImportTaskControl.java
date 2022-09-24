package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;

public interface ImportTaskControl {

    boolean cancelImport();

    String whoAmI();

    Dataset getCurrentDataSet();

}
