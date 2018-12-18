package com.mapbox.services.android.navigation.v5.eh.osm;

import java.util.Optional;

/**
 * Relevant Way types.
 *
 * @see <a href="https://wiki.openstreetmap.org/wiki/Key:highway#Values">OSM Wiki</a>
 */
public enum WayType {
    //CHECKSTYLE:OFF
    // MAIN Types
    MOTORWAY(1, true),
    TRUNK(2),
    PRIMARY(3),
    SECONDARY(4),
    TERTIARY(5),
    UNCLASSIFIED(6),
    RESIDENTIAL(7),
    SERVICE(8),

    // Link types
    MOTORWAY_LINK(2, true),
    TRUNK_LINK(3),
    PRIMARY_LINK(4),
    SECONDARY_LINK(5),
    TERTIARY_LINK(6),

    // Special types
    LIVING_STREET(8),

    // For type safety
    UNKNOWN(10);

    //CHECKSTYLE:ON

    private final int order;
    private final boolean oneWayImplied;

    WayType(final int order) {
        this(order, false);
    }

    WayType(final int order, final boolean oneWayImplied) {
        this.order = order;
        this.oneWayImplied = oneWayImplied;
    }

    /**
     * Is this way type relevant for us?
     *
     * @param input the way type, case insensitive
     * @return true if relevant
     */
    public static boolean isRelevant(final String input) {
        return valueOfOptional(input).isPresent();
    }

    /**
     * Is oneway implied for this waytype?
     *
     * @param input the way type, case insensitive
     * @return true if oneway is implied
     */
    public static Boolean impliesOneWay(final String input) {
        return valueOfOptional(input)
                .map(WayType::isOneWayImplied)
                .orElse(false);
    }

    /**
     * Exception free valueOf.
     *
     * @param input name, case insensitive
     * @return an optional
     */
    public static Optional<WayType> valueOfOptional(final String input) {
        if (input != null) {
            try {
                return Optional.of(valueOf(WayType.class, input.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return Optional.empty();
    }

    /**
     * @return true if one way is implied
     */
    public boolean isOneWayImplied() {
        return oneWayImplied;
    }

    /**
     * @return precedence of this way
     */
    public int getOrder() {
        return order;
    }
}
