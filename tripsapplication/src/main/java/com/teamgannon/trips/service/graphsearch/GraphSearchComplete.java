package com.teamgannon.trips.service.graphsearch;


public interface GraphSearchComplete {

    /**
     * signals completion of the graph search
     *
     * @param status       the status
     * @param errorMessage the error message
     */
    void complete(boolean status, String errorMessage);

}
