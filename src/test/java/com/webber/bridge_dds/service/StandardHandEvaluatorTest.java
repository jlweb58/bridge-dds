package com.webber.bridge_dds.service;

import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Hand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StandardHandEvaluatorTest {

    private HandEvaluator evaluator;

    @BeforeEach
    public void setUp() {
        evaluator = new StandardHandEvaluator();
    }

    @Test
    public void test10PointHand() {
        Hand hand = new Hand();
        hand.add(Card.SA).add(Card.S9).add(Card.S8).add(Card.S7);
        hand.add(Card.HK).add(Card.H2).add(Card.H3);
        hand.add(Card.DQ).add(Card.D4).add(Card.D5);
        hand.add(Card.CJ).add(Card.C5);
        double points = evaluator.evaluate(hand);
        assertEquals(10.0, points, 0.001);
    }
}
