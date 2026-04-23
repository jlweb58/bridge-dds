package com.webber.bridge_dds.handgeneration;

public record HandGenerationParameters(
        int minPoints,
        int maxPoints,
        HandDistribution handDistribution) {

       public HandGenerationParameters {
           assert minPoints >= 0;
           assert maxPoints >= minPoints;
           assert handDistribution != null;
       }
}
