package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Suit;

import java.util.List;

public record HandGenerationCondition(
        HandGenerationConditionOperator operator,
        List<HandGenerationCondition> conditions,
        Suit suit,
        SuitLengthRange range
) {

    public HandGenerationCondition {
        boolean isCompositeCondition = operator != null;
        boolean isSuitCondition = suit != null && range != null;

        assert isCompositeCondition || isSuitCondition;

        if (isCompositeCondition) {
            assert conditions != null;
            assert !conditions.isEmpty();
        }
    }

    public boolean matches(Hand hand) {
        if (operator == null) {
            int suitLength = hand.ranksForSuit(suit).size();
            return suitLength >= range.min() && suitLength <= range.max();
        }

        return switch (operator) {
            case AND -> conditions.stream().allMatch(condition -> condition.matches(hand));
            case OR -> conditions.stream().anyMatch(condition -> condition.matches(hand));
        };
    }
}
