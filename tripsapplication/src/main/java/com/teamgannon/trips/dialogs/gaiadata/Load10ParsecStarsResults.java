package com.teamgannon.trips.dialogs.gaiadata;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Load10ParsecStarsResults {

    private boolean starsLoaded;

    private List<StarRecord> records;

}
