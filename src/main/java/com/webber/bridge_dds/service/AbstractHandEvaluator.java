package com.webber.bridge_dds.service;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Suit;

import java.util.List;
import java.util.Set;

public abstract class AbstractHandEvaluator implements HandEvaluator{

    @Override
    public double evaluate(Hand hand) {
        double points = 0.0;
        points += evaluateSuit(hand, Suit.SPADES);
        points += evaluateSuit(hand, Suit.HEARTS);
        points += evaluateSuit(hand, Suit.DIAMONDS);
        points += evaluateSuit(hand, Suit.CLUBS);
        return points;
    }

    protected boolean is4_3_3_3_Distribution(Hand hand) {
        return hand.view().values().stream()
                .mapToInt(Set::size)
                .sorted()
                .boxed()
                .toList()
                .equals(List.of(3, 3, 3, 4));
    }

    protected abstract double evaluateSuit(Hand hand, Suit suit);
}
