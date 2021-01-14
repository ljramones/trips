package com.teamgannon.trips.jpa.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class TripsPrefs {

    @Id
    private String id;

    private boolean showWelcomeDataReq;

}
