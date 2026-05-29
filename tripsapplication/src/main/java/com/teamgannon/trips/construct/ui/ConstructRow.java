package com.teamgannon.trips.construct.ui;

import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.WeaponInstallation;
import com.terranrepublic.infrastructure.SpaceInfrastructure;
import com.terranrepublic.infrastructure.TransportNode;

/**
 * Display-model for one row in the {@link InstallationDesignerPanel} table.
 *
 * <p>Wraps a {@link Cataloged} construct (a {@link SpaceAsset} or {@link SpaceInfrastructure})
 * and exposes its cross-cutting attributes as plain getters for the JavaFX
 * {@code PropertyValueFactory}. The underlying construct is preserved so the details pane can
 * pattern-match on its concrete subtype.
 */
public final class ConstructRow {

    private final Cataloged construct;

    public ConstructRow(Cataloged construct) {
        this.construct = construct;
    }

    public Cataloged getConstruct() {
        return construct;
    }

    public String getName() {
        return construct.name();
    }

    public String getDesignation() {
        return switch (construct) {
            case StationDesign s -> s.designation();
            case WeaponInstallation w -> w.designation();
            case Megastructure m -> m.designation();
            case TransportNode ignored -> "";
            default -> "";
        };
    }

    /**
     * Stable user-facing label for the construct's top-level Kind. Returns the enum constant name
     * so the filter combo can compare strings without a separate display map. Conduits and Ships
     * are not surfaced by the Installations Designer; the {@code default} branch is defensive.
     */
    public String getKind() {
        return switch (construct) {
            case StationDesign ignored -> "STATION";
            case WeaponInstallation ignored -> "WEAPON_INSTALLATION";
            case Megastructure ignored -> "MEGASTRUCTURE";
            case TransportNode ignored -> "TRANSPORT_NODE";
            default -> "UNKNOWN";
        };
    }

    /** The subtype-discriminator enum value as a string (StationType / InstallationType / NodeType). */
    public String getSubtype() {
        return switch (construct) {
            case StationDesign s -> s.stationType().name();
            case WeaponInstallation w -> w.installationType().name();
            case Megastructure m -> m.archetype().name();
            case TransportNode t -> t.type().name();
            default -> "";
        };
    }

    public String getFaction() {
        return construct.faction();
    }

    public String getSource() {
        return construct.source();
    }

    public boolean isConcealed() {
        return construct.concealed();
    }

    /**
     * Operational state label. {@link TransportNode} has no operational-state concept; surface
     * it as an em dash so the table column shows the field is N/A rather than blank.
     */
    public String getOperationalState() {
        return switch (construct) {
            case StationDesign s -> s.operationalState().name();
            case WeaponInstallation w -> w.operationalState().name();
            case Megastructure m -> m.operationalState().name();
            case TransportNode ignored -> "—";
            default -> "";
        };
    }

    /**
     * Free-form "category" / classification field. Stations and weapons carry one explicitly;
     * transport nodes don't, so they surface as an empty string and the Category filter ignores
     * them.
     */
    public String getCategory() {
        return switch (construct) {
            case StationDesign s -> s.category();
            case WeaponInstallation w -> w.category();
            case Megastructure m -> m.category();
            case TransportNode ignored -> "";
            default -> "";
        };
    }
}
