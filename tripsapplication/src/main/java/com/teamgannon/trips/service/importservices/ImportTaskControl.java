package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.Dataset;

public interface ImportTaskControl {

    boolean cancelImport();

    String whoAmI();

    Dataset getCurrentDataSet();

}
