package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Suit;

import java.util.Map;

/**
 * Constraints for a single hand. {@code minPoints}/{@code maxPoints} may be null (unbounded).
 * If neither {@code handDistribution} nor {@code condition} is given the shape is unconstrained;
 * suits missing from {@code handDistribution} are unconstrained as well.
 */
public record HandGenerationParameters(
        Integer minPoints,
        Integer maxPoints,
        HandDistribution handDistribution,
        HandGenerationCondition condition,
        Map<Suit,SuitQualityRequirement> suitQualityRequirements) {

    public HandGenerationParameters(int minPoints, int maxPoints, HandDistribution handDistribution) {
        this(minPoints, maxPoints, handDistribution, null,null);
    }

    public HandGenerationParameters {
           assert minPoints == null || minPoints >= 0;
           assert minPoints == null || maxPoints == null || maxPoints >= minPoints;
    }
}
