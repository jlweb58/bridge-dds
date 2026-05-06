package com.webber.bridge_dds.handgeneration;

public record HandGenerationParameters(
        int minPoints,
        int maxPoints,
        HandDistribution handDistribution,
        HandGenerationCondition condition) {

    public HandGenerationParameters(int minPoints, int maxPoints, HandDistribution handDistribution) {
        this(minPoints, maxPoints, handDistribution, null);
    }

    public HandGenerationParameters {
           assert minPoints >= 0;
           assert maxPoints >= minPoints;
           assert handDistribution != null || condition != null;
    }
}
