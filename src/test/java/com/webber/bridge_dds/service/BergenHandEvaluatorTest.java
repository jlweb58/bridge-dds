package com.webber.bridge_dds.service;

import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Hand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BergenHandEvaluatorTest {

    private final BergenHandEvaluator evaluator = new BergenHandEvaluator();


    @Test
    public void testBergenHand1() {
        Hand hand = new Hand();
        hand.add(Card.SA).add(Card.SK).add(Card.SQ).add(Card.ST).add(Card.S5);
        hand.add(Card.HT).add(Card.H9).add(Card.H8).add(Card.H2);
        hand.add(Card.D6);
        hand.add(Card.CJ).add(Card.C7).add(Card.C6);
        assertEquals(12.0, evaluator.evaluate(hand), 0.0001);
    }

    @Test
    public void testBergenHand2() {
        Hand hand = new Hand();
        hand.add(Card.SA).add(Card.SK).add(Card.ST);
        hand.add(Card.HK).add(Card.HJ).add(Card.H3);
        hand.add(Card.DJ).add(Card.DT).add(Card.D5).add(Card.D2);
        hand.add(Card.C7).add(Card.C6).add(Card.C5);
        assertEquals(12.0, evaluator.evaluate(hand), 0.0001);
    }

    @Test
    public void testBergenHand3() {
        Hand hand = new Hand();
        hand.add(Card.SA).add(Card.S7).add(Card.S6);
        hand.add(Card.HK).add(Card.HQ).add(Card.H5).add(Card.H4);
        hand.add(Card.DQ).add(Card.D7);
        hand.add(Card.CJ).add(Card.C8).add(Card.C7).add(Card.C6);
        assertEquals(11.0, evaluator.evaluate(hand), 0.0001);
    }

    @Test
    public void testBergenHand4() {
        Hand hand = new Hand();
        hand.add(Card.SA).add(Card.ST).add(Card.S4);
        hand.add(Card.HT).add(Card.H5).add(Card.H4).add(Card.H3);
        hand.add(Card.DK).add(Card.DJ).add(Card.D7).add(Card.D6);
        hand.add(Card.CK).add(Card.CT);
        assertEquals(12.0, evaluator.evaluate(hand), 0.0001);
    }

    @Test
    public void testBergenHand5() {
        Hand hand = new Hand();
        hand.add(Card.SK).add(Card.S8).add(Card.S7);
        hand.add(Card.HA).add(Card.HQ).add(Card.H9).add(Card.H8).add(Card.H5).add(Card.H2);
        hand.add(Card.DA).add(Card.DT).add(Card.D9);
        hand.add(Card.CA);
        assertEquals(20.0, evaluator.evaluate(hand), 0.0001);

    }
}
