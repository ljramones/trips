package com.teamgannon.trips.jpa.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Data
@Entity
public class Moon {

    @Id
    private UUID id;

}
