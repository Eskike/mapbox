package com.mapbox.services.android.navigation.v5.eh.osm;

/**
 * OSM Junction Type.
 */
public enum JunctionType {
    //CHECKSTYLE:OFF
    ROUNDABOUT(true);
    //CHECKSTYLE:ON

    private final boolean oneWayImplied;

    JunctionType(final boolean oneWayImplied) {
        this.oneWayImplied = oneWayImplied;
    }

    /**
     * Is oneway implied for this junction type?
     *
     * @param input the junction type, case insensitive
     * @return true if oneway is implied
     */
    public static Boolean impliesOneWay(final String input) {
        if (input != null) {
            try {
                return valueOf(JunctionType.class, input.toUpperCase()).isOneWayImplied();
            } catch (IllegalArgumentException ignored) {
            }
        }
        return false;
    }

    /**
     * @return true if one way is implied
     */
    public boolean isOneWayImplied() {
        return oneWayImplied;
    }
}
