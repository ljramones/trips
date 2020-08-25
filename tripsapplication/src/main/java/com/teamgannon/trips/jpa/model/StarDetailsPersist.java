package com.teamgannon.trips.jpa.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Entity
public class StarDetailsPersist implements Serializable {

    @Id
    private String id;

    /**
     * the stelaar class
     */
    @Column(unique = true)
    private String stellarClass;

    /**
     * the star color
     */
    private String starColor;

    /**
     * defined in SOL units (x Sol)
     */
    private float radius = 10;

}
