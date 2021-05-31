package com.teamgannon.trips.experiments;

public class Account implements OnlineAccount{

    private Integer noOfRegularMovies;

    private Integer noOfExclusiveMovies;

    private String ownerName;

    public Account(Integer noOfRegularMovies, Integer noOfExclusiveMovies, String ownerName) {
        this.noOfRegularMovies = noOfRegularMovies;
        this.noOfExclusiveMovies = noOfExclusiveMovies;
        this.ownerName = ownerName;
    }
}
