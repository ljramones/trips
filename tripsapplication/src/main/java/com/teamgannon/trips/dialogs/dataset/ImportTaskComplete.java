package com.teamgannon.trips.dialogs.dataset;

public interface ImportTaskComplete {
    /**
     * signal completion of JavaFx Service
     *
     * @param status            whether it passed or failed
     * @param dataset           the dataset definition to load
     * @param fileProcessResult the end result
     * @param errorMessage      whether there is an error message
     */
    void complete(boolean status, Dataset dataset, FileProcessResult fileProcessResult, String errorMessage);
}
