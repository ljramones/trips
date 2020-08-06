package com.teamgannon.trips.dialogs.support;

import com.teamgannon.trips.tableviews.StarEditRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TableEditResult {

    private EditTypeEnum editType;

    private StarEditRecord starEditRecord;

}
