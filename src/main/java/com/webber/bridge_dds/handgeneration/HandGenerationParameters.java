package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Suit;

import java.util.Map;

public record HandGenerationParameters(
        int minPoints,
        int maxPoints,
        HandDistribution handDistribution,
        HandGenerationCondition condition,
        Map<Suit,SuitQualityRequirement> suitQualityRequirements) {

    public HandGenerationParameters(int minPoints, int maxPoints, HandDistribution handDistribution) {
        this(minPoints, maxPoints, handDistribution, null,null);
    }

    public HandGenerationParameters {
           assert minPoints >= 0;
           assert maxPoints >= minPoints;
           assert handDistribution != null || condition != null;
    }
}
