package com.teamgannon.trips.dataset.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.UUID;

/**
 * Stores the values of the custom data for various star objects.
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Data
public class CustomDataValue {

    @Id
    private UUID id;

    private String customFieldName;

    private String customFieldValue;

}
