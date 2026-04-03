package com.webber.bridge_dds.service;

import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Rank;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class KaplanRubensHandEvaluatorTest {

    private final KaplanRubensHandEvaluator kaplanRubensHandEvaluator = new KaplanRubensHandEvaluator();


    @Test
    public void test4333Hand() {
        Hand hand_4333 = new Hand();
        hand_4333.add(Card.SA).add(Card.S2).add(Card.S3).add(Card.S4);
        hand_4333.add(Card.HK).add(Card.H2).add(Card.H3);
        hand_4333.add(Card.DQ).add(Card.D4).add(Card.D5);
        hand_4333.add(Card.CJ).add(Card.C5).add(Card.C6);
        double points = kaplanRubensHandEvaluator.evaluate(hand_4333);
        assertEquals(8.65, points, 0.01);
    }

    @Test
    public void test5332Hand() {
        Hand hand_5332 = new Hand();
        hand_5332.add(Card.SA).add(Card.S2).add(Card.S3).add(Card.ST).add(Card.SK);
        hand_5332.add(Card.HK).add(Card.H2).add(Card.H3);
        hand_5332.add(Card.DQ).add(Card.D4).add(Card.D5);
        hand_5332.add(Card.CJ).add(Card.C5);
        double points = kaplanRubensHandEvaluator.evaluate(hand_5332);
        assertEquals(13.7, points, 0.01);
    }

    @Test
    public void testHandWith7CardSuit() {
        Hand hand_7card_suit = new Hand();
        hand_7card_suit.add(Card.ST).add(Card.S2).add(Card.S3).add(Card.S4).add(Card.S5).add(Card.S6).add(Card.S7);
        hand_7card_suit.add(Card.HK).add(Card.H2).add(Card.H3);
        hand_7card_suit.add(Card.DQ).add(Card.D4).add(Card.D5);
        double points = kaplanRubensHandEvaluator.evaluate(hand_7card_suit);
        assertEquals(7.3, points, 0.01);
    }

    @Test
    public void testHandWith7CardSuitAndAce() {
        Hand hand_7card_suit = new Hand();
        hand_7card_suit.add(Card.SA).add(Card.ST).add(Card.S3).add(Card.S4).add(Card.S5).add(Card.S6).add(Card.S7);
        hand_7card_suit.add(Card.HK).add(Card.H2).add(Card.H3);
        hand_7card_suit.add(Card.DQ).add(Card.D4).add(Card.D5);
        double points = kaplanRubensHandEvaluator.evaluate(hand_7card_suit);
        assertEquals(13.1, points, 0.01);
    }

    @Test
    public void testBoard21March31() {
        Hand hand = new Hand();
        hand.add(Card.S3);
        hand.add(Card.HQ).add(Card.HJ).add(Card.H8).add(Card.H6);
        hand.add(Card.DA).add(Card.DK).add(Card.DT).add(Card.D9).add(Card.D8).add(Card.D6).add(Card.D4);
        hand.add(Card.CT);
        double points = kaplanRubensHandEvaluator.evaluate(hand);
        assertEquals(16.45, points, 0.01);
    }

    @Test
    public void testHandWithDiamondVoid() {
        Hand hand = new Hand();
        hand.add(Card.SK).add(Card.SJ).add(Card.ST).add(Card.S2);
        hand.add(Card.H9).add(Card.H8).add(Card.H6).add(Card.H3).add(Card.H2);
        hand.add(Card.CA).add(Card.CT).add(Card.C8).add(Card.C3);
        double points = kaplanRubensHandEvaluator.evaluate(hand);
        assertEquals(11.3, points, 0.01);
    }

    @Test
    public void testIsTenWithJackOrTwoHigherHonors() {
        Set<Rank> ranks = Set.of(Rank.TEN, Rank.JACK, Rank.TWO, Rank.EIGHT);
        boolean result = kaplanRubensHandEvaluator.isTenWithJackOrTwoHigherHonors(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.TEN, Rank.ACE, Rank.KING, Rank.TWO);
        result = kaplanRubensHandEvaluator.isTenWithJackOrTwoHigherHonors(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.TEN, Rank.ACE, Rank.QUEEN, Rank.TWO);
        result = kaplanRubensHandEvaluator.isTenWithJackOrTwoHigherHonors(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.TEN, Rank.KING, Rank.QUEEN, Rank.TWO);
        result = kaplanRubensHandEvaluator.isTenWithJackOrTwoHigherHonors(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.NINE, Rank.ACE, Rank.KING, Rank.TWO);
        result = kaplanRubensHandEvaluator.isTenWithJackOrTwoHigherHonors(ranks);
        assertFalse(result);
        ranks = Set.of(Rank.TEN);
        result = kaplanRubensHandEvaluator.isTenWithJackOrTwoHigherHonors(ranks);
        assertFalse(result);
        ranks = Set.of(Rank.NINE, Rank.ACE, Rank.KING, Rank.TWO, Rank.FIVE, Rank.FOUR, Rank.THREE);
        result = kaplanRubensHandEvaluator.isTenWithJackOrTwoHigherHonors(ranks);
        assertFalse(result);
    }

    @Test
    public void testIsNineWithEightOrTenOrTwoHigherHonors() {
        Set<Rank> ranks = Set.of(Rank.NINE, Rank.EIGHT, Rank.SIX, Rank.THREE, Rank.TWO);
        boolean result = kaplanRubensHandEvaluator.isNineWithEightOrTenOrTwoHigherHonors(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.NINE, Rank.JACK, Rank.TWO, Rank.TEN);
        result = kaplanRubensHandEvaluator.isNineWithEightOrTenOrTwoHigherHonors(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.NINE, Rank.ACE, Rank.KING, Rank.TWO);
        result = kaplanRubensHandEvaluator.isNineWithEightOrTenOrTwoHigherHonors(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.NINE, Rank.ACE, Rank.QUEEN, Rank.TWO);
        result = kaplanRubensHandEvaluator.isNineWithEightOrTenOrTwoHigherHonors(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.NINE, Rank.KING, Rank.QUEEN, Rank.TWO);
        result = kaplanRubensHandEvaluator.isNineWithEightOrTenOrTwoHigherHonors(ranks);
        assertTrue(result);
        assertTrue(result);
    }

    @Test
    public void testIsNineWithThreeHigherHonors() {
        Set<Rank> ranks = Set.of(Rank.NINE, Rank.ACE, Rank.KING, Rank.QUEEN);
        boolean result = kaplanRubensHandEvaluator.isNineWithThreeHigherHonors(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.NINE, Rank.ACE, Rank.KING, Rank.JACK);
        result = kaplanRubensHandEvaluator.isNineWithThreeHigherHonors(ranks);
        assertFalse(result);
    }

    @Test
    public void testIsSevenCardsMissingQueenOrJackOrBoth() {
        Set<Rank> ranks = Set.of(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT);
        boolean result = kaplanRubensHandEvaluator.isSevenCardsMissingQueenOrJackOrBoth(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.QUEEN);
        result = kaplanRubensHandEvaluator.isSevenCardsMissingQueenOrJackOrBoth(ranks);
        assertFalse(result);
        ranks = Set.of(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.JACK);
        result = kaplanRubensHandEvaluator.isSevenCardsMissingQueenOrJackOrBoth(ranks);
        assertFalse(result);
        ranks = Set.of(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.QUEEN, Rank.JACK);
        result = kaplanRubensHandEvaluator.isSevenCardsMissingQueenOrJackOrBoth(ranks);
        assertFalse(result);
    }

    @Test
    public void testIsEightPlusCardsMissingQueen() {
        Set<Rank> ranks = Set.of(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE);
        boolean result = kaplanRubensHandEvaluator.isEightPlusCardsMissingQueen(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.QUEEN);
        result = kaplanRubensHandEvaluator.isEightPlusCardsMissingQueen(ranks);
        assertFalse(result);
    }

    @Test
    public void testIsNinePlusCardsMissingQueenAndJack() {
        Set<Rank> ranks = Set.of(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN);
        boolean result = kaplanRubensHandEvaluator.isNinePlusCardsMissingQueenAndJack(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.TEN, Rank.JACK);
        result = kaplanRubensHandEvaluator.isNinePlusCardsMissingQueenAndJack(ranks);
        assertTrue(result);
        ranks = Set.of(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.QUEEN, Rank.JACK);
        result = kaplanRubensHandEvaluator.isNinePlusCardsMissingQueenAndJack(ranks);
        assertFalse(result);
    }

}
