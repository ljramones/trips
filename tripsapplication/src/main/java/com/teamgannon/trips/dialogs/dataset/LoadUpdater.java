package com.teamgannon.trips.dialogs.dataset;

public interface LoadUpdater {

    void updateLoad(String message);

    void loadComplete(boolean status, Dataset dataset, String errorMessage);

}
