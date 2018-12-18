package com.mapbox.services.android.navigation.v5.eh;

import java.util.Objects;

/**
 * Main configuration options.
 */
public class Configuration {
    private Integer horizonDistance;
    private HorizonExpansion horizonExpansion;
    private Integer updateFrequency;

    /**
     * @param distance the EHorizon distance
     * @return this
     */
    public Configuration withHorizonDistance(final int distance) {
        this.horizonDistance = distance;
        return this;
    }

    /**
     * @param expansion the {@link HorizonExpansion}
     * @return this
     */
    public Configuration withHorizonExpansion(final HorizonExpansion expansion) {
        this.horizonExpansion = expansion;
        return this;
    }

    /**
     * @param frequency the update frequency
     * @return this
     */
    public Configuration withUpdateFrequency(final int frequency) {
        this.updateFrequency = frequency;
        return this;
    }

    /**
     * @return the horizon distance
     */
    public Integer horizonDistance() {
        return horizonDistance;
    }

    /**
     * @return the {@link HorizonExpansion}
     */
    public HorizonExpansion horizonExpansion() {
        return horizonExpansion;
    }

    /**
     * @return the update frequency
     */
    public Integer updateFrequency() {
        return updateFrequency;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Configuration that = (Configuration) o;
        return Objects.equals(horizonDistance, that.horizonDistance)
                && horizonExpansion == that.horizonExpansion
                && Objects.equals(updateFrequency, that.updateFrequency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(horizonDistance, horizonExpansion, updateFrequency);
    }
}
