package com.teamgannon.trips.file.chview.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * The link descriptor
 * <p>
 * Each link is then represented by a integer containing the type of link and a string containing the starName
 * of the destination.
 * <p>
 * Created by larrymitchell on 2017-02-07.
 */
@Data
@Builder
public class CHLinkDescriptor implements Serializable {

    /**
     * the link type
     */
    private int linkType;

    /**
     * the star name
     */
    private String starName;

}
