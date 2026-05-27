package com.teamgannon.trips.screenobjects;

import lombok.Data;

/**
 * Result of a {@link StarEditDialog} interaction.
 * <p>
 * Carries the edited {@link StarEditViewModel} back to the caller. The
 * caller is responsible for converting back to an entity + persisting
 * via the service layer (typically `StarEditMapper.applyToEntity` +
 * `starService.starBulkSave(...)` or similar).
 * <p>
 * The {@link #isChanged() changed} flag distinguishes "user clicked Save"
 * (true) from "user clicked Cancel or closed the window" (false).
 */
@Data
public class StarEditStatus {

    /** The (possibly-edited) view-model. Set even when {@link #changed} is false so the caller can still inspect it. */
    private StarEditViewModel viewModel;

    private boolean changed = false;
}
