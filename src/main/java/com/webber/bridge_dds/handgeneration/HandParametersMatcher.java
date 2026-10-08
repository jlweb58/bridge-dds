package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Suit;
import com.webber.bridge_dds.service.HandEvaluator;
import org.springframework.stereotype.Component;

/**
 * Checks whether a hand satisfies a set of {@link HandGenerationParameters}
 * (distribution or condition, point range and suit quality).
 */
@Component
public class HandParametersMatcher {

    private final SuitQualityRequirementsValidator suitQualityRequirementsValidator;

    public HandParametersMatcher(SuitQualityRequirementsValidator suitQualityRequirementsValidator) {
        this.suitQualityRequirementsValidator = suitQualityRequirementsValidator;
    }

    public boolean matches(HandEvaluator handEvaluator, HandGenerationParameters parameters, Hand hand) {
        assert hand != null;
        assert hand.size() == 13;
        return matchesDistribution(parameters, hand)
                && matchesPointCount(handEvaluator, parameters, hand)
                && suitQualityRequirementsValidator.satisfies(parameters, hand);
    }

    public boolean matchesDistribution(HandGenerationParameters parameters, Hand hand) {
        if (parameters.condition() != null) {
            return parameters.condition().matches(hand);
        }

        HandDistribution handDistribution = parameters.handDistribution();
        if (handDistribution == null || handDistribution.suitLengths() == null) {
            return true;
        }

        for (Suit suit : Suit.values()) {
            SuitLengthRange range = handDistribution.suitLengths().get(suit);
            if (range == null) {
                continue; // suit not constrained
            }
            int size = hand.ranksForSuit(suit).size();
            if (size < range.min() || size > range.max()) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesPointCount(HandEvaluator handEvaluator, HandGenerationParameters parameters, Hand hand) {
        if (parameters.minPoints() == null && parameters.maxPoints() == null) {
            return true;
        }
        double pointCount = handEvaluator.evaluate(hand);
        return (parameters.minPoints() == null || pointCount >= parameters.minPoints())
                && (parameters.maxPoints() == null || pointCount <= parameters.maxPoints());
    }

    public boolean matchesSuitQuality(HandGenerationParameters parameters, Hand hand) {
        return suitQualityRequirementsValidator.satisfies(parameters, hand);
    }
}
