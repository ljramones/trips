package com.teamgannon.trips.dataset.model;

import com.teamgannon.trips.dataset.enums.CustomFieldType;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The custom data definition
 * <p>
 * End-users will want to store data not in the basic database about objects.  In order to do that, they can
 * define custom data fields which are stored as meta-data and linked to individual star objects.
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Data
public class CustomDataDefinition {

    @Id
    private UUID id;

    /**
     * The name to display when entering or editing a custom field
     */
    private String customFieldName;

    /**
     * Text, numeric, multiple picklist or single picklist.
     */
    private CustomFieldType customFieldType;

    /**
     * Width of a text or numeric type.
     */
    private String customFieldWidth;

    /**
     * An array of the choices for custom fields that are picklists.
     * <p>
     * If a field is of type “multiple” then the user can select several of the items. For example, a multiple
     * could be “Installations: Military, Antimatter, Civilian, Jovian Fuel Station, Shopping Mall”
     * <p>
     * If a picklist field is of type single, the user can select only one option out of the list. An example
     * of a single could be “Population class: <1 mill, <100 mill, <1 billion, >1billion”
     */
    private List<String> customFieldOptions = new ArrayList<>();

}
