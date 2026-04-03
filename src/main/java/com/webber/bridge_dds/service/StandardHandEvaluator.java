package com.webber.bridge_dds.service;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Rank;
import com.webber.bridge_dds.model.Suit;

import java.util.Set;

public class StandardHandEvaluator extends AbstractHandEvaluator {
    @Override
    protected double evaluateSuit(Hand hand, Suit suit) {
        Set<Rank> ranks = hand.ranksForSuit(suit);
        return ranks.stream().mapToDouble(Rank::points).sum();
    }
}
