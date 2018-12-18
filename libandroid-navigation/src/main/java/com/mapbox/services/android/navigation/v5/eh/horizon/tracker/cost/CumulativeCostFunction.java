package com.mapbox.services.android.navigation.v5.eh.horizon.tracker.cost;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cumulative cost over a set of cost functions with associated weights.
 */
public class CumulativeCostFunction implements CostFunction {
    private static final class WeightedCostFunction implements CostFunction {
        private final double weight;
        private final CostFunction cost;

        private WeightedCostFunction(final double weight, final CostFunction cost) {
            this.weight = weight;
            this.cost = cost;
        }

        @Override
        public double cost() {
            return weight * cost.cost();
        }
    }

    private List<WeightedCostFunction> costFunctions = new ArrayList<>();

    @Override
    public double cost() {
        return costFunctions.stream()
                .mapToDouble(CostFunction::cost)
                .sum();
    }

    /**
     * Add a cost function with a weight.
     *
     * @param weight the weight
     * @param cost   the cost function
     * @return this
     */
    public CumulativeCostFunction withCost(final double weight, final CostFunction cost) {
        costFunctions.add(new WeightedCostFunction(weight, cost));
        return this;
    }

    /**
     * Chain with other {@link CumulativeCostFunction}.
     *
     * @param other the other
     * @return this
     */
    public CumulativeCostFunction with(final CumulativeCostFunction other) {
        costFunctions.addAll(other.costFunctions);
        return this;
    }

    @Override
    public String toString() {
        List<String> subs = costFunctions.stream()
                .map(c -> String.format("\t%s x %s = %s", c.weight, c.cost.cost(), c.cost()))
                .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder("\n");
        for (final String s : subs) {
            builder.append(s);
            builder.append("\n");
        }

        builder.append("\tSum: ").append(cost());
        return builder.toString();
    }
}
