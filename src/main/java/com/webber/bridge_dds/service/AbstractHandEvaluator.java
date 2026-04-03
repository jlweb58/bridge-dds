package com.webber.bridge_dds.service;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Suit;

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

    protected abstract double evaluateSuit(Hand hand, Suit suit);
}
