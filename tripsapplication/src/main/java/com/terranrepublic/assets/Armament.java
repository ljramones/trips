package com.terranrepublic.assets;

/**
 * A weapon or weapon battery attached to a ship, station, or standalone installation.
 */
public record Armament(
        String name,
        WeaponType type,
        int quantity,
        double yieldOrPowerMW,
        double effectiveRangeKm,
        String role,
        String notes
) {

    public Armament {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Armament name must be provided");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Armament quantity must not be negative");
        }
        if (yieldOrPowerMW < 0) {
            throw new IllegalArgumentException("Armament yieldOrPowerMW must not be negative");
        }
        if (effectiveRangeKm < 0) {
            throw new IllegalArgumentException("Armament effectiveRangeKm must not be negative");
        }
        type = type == null ? WeaponType.OTHER : type;
        role = role == null ? "" : role;
        notes = notes == null ? "" : notes;
    }
}
