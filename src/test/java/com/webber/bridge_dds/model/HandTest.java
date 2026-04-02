package com.webber.bridge_dds.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

public class HandTest {

    @Test
    public void itShouldNotAllowMoreThanThirteenCards() {
        Hand hand = new Hand();
        hand.add(Card.SA).add(Card.S2).add(Card.S3).add(Card.S4).add(Card.S5).add(Card.S6);
        hand.add(Card.S7).add(Card.S8).add(Card.S9).add(Card.ST).add(Card.SJ).add(Card.SQ).add(Card.SK);
        assertThrows(IllegalStateException.class, () -> hand.add(Card.H7));
    }

    @Test
    public void itShouldNotAllowDuplicateCards() {
        Hand hand = new Hand();
        hand.add(Card.SA).add(Card.S2).add(Card.S3).add(Card.S4);
        assertThrows(IllegalArgumentException.class, () -> hand.add(Card.SA));
    }

    @Test
    public void itShouldReturnAllCardsForASuit() {
     Hand hand = new Hand();
     hand.add(Card.SA).add(Card.S2).add(Card.S3).add(Card.S4);
     hand.add(Card.HK).add(Card.H2).add(Card.H3);
     hand.add(Card.DQ).add(Card.D4).add(Card.D5);
     hand.add(Card.CJ).add(Card.C5).add(Card.C6);
     EnumSet<Rank> ranks = hand.ranksForSuit(Suit.SPADES);
     assertEquals(4, ranks.size());
     assertTrue(ranks.contains(Rank.ACE));
     assertTrue(ranks.contains(Rank.TWO));
     assertTrue(ranks.contains(Rank.THREE));
     assertTrue(ranks.contains(Rank.FOUR));
     ranks = hand.ranksForSuit(Suit.HEARTS);
     assertEquals(3, ranks.size());
     assertTrue(ranks.contains(Rank.KING));
     assertTrue(ranks.contains(Rank.TWO));
     assertTrue(ranks.contains(Rank.THREE));
     ranks = hand.ranksForSuit(Suit.DIAMONDS);
     assertEquals(3, ranks.size());
     assertTrue(ranks.contains(Rank.QUEEN));
     assertTrue(ranks.contains(Rank.FIVE));
     assertTrue(ranks.contains(Rank.FOUR));
     ranks = hand.ranksForSuit(Suit.CLUBS);
     assertEquals(3, ranks.size());
     assertTrue(ranks.contains(Rank.JACK));
     assertTrue(ranks.contains(Rank.SIX));
     assertTrue(ranks.contains(Rank.FIVE));
     }
}
