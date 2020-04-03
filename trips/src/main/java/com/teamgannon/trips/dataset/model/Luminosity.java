package com.teamgannon.trips.dataset.model;

import com.teamgannon.trips.dataset.enums.LuminosityCode;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.UUID;

/**
 * A definition of the luminosity
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Data
public class Luminosity {

    @Id
    private UUID id;

    private LuminosityCode luminosityCode;

    private double flux;

    private String bandName;

    /**
     * band center in nm
     */
    private int bandCenter;

    /**
     * bandwith in nm
     */
    private int bandwidth;

}
