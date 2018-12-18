package com.mapbox.services.android.navigation.v5.eh;

/**
 * EHorizon expansion options.
 */
public enum HorizonExpansion {
    /**
     * Limited expansion. MPP is fully expanded, branches are limited to a single Edge.
     */
    LIMITED,

    /**
     * Full expansion. All paths are fully expanded.
     */
    FULL;

    /**
     * Exception free valueOf.
     *
     * @param input the input string, case insensitive
     * @return the {@link HorizonExpansion} or null
     */
    public static HorizonExpansion valueOfOrNull(final String input) {
        if (input != null) {
            try {
                return valueOf(HorizonExpansion.class, input.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
