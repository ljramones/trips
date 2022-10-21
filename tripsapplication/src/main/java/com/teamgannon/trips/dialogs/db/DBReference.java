package com.teamgannon.trips.dialogs.db;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DBReference {

    /**
     * the id reference to the star
     */
    private String id;

    /**
     * the display name
     */
    private String displayName;

}
