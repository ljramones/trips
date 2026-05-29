package com.terranrepublic.assets;

/**
 * Stable discriminator for catalogued space assets.
 *
 * <p>v2 Phase D.7 extended this enum to four values, adding {@code MEGASTRUCTURE}
 * for scale-class, self-contained-setting objects (Troy, Death Star, Dahak, Rama,
 * Ringworld). The original three pre-D.7 values remain at their original ordinals
 * (SHIP=0, STATION=1, WEAPON_INSTALLATION=2) so persisted catalog entries that
 * stored the ordinal continue to resolve correctly; MEGASTRUCTURE is appended at
 * ordinal 3.
 *
 * <p>Every {@link SpaceAsset} subtype overrides {@code kind()} to return its
 * stable discriminator value; downstream code (registry buckets, panel filters,
 * mapper dispatch) keys off the result.
 */
public enum AssetKind {
    SHIP,
    STATION,
    WEAPON_INSTALLATION,
    MEGASTRUCTURE
}
