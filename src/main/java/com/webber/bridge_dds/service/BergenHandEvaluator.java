package com.webber.bridge_dds.service;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Rank;
import com.webber.bridge_dds.model.Suit;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

public class BergenHandEvaluator extends AbstractHandEvaluator {

    @Override
    public double evaluate(Hand hand) {
        double points = super.evaluate(hand);
        points += adjustForHonors(hand);
        if (is4_3_3_3_Distribution(hand)) points -= 1;
        return points;
    }


    @Override
    protected double evaluateSuit(Hand hand, Suit suit) {
        double points = 0.0;
        Set<Rank> ranks = hand.ranksForSuit(suit);
        points = ranks.stream().mapToDouble(Rank::points).sum();
        if (isQualitySuit(ranks)) points += 1;
        switch (ranks.size()) {
            case 1: points += adjustForSingleton(ranks); break;
            case 2: points += adjustForDoubleton(ranks); break;
            case 5: points += 1; break;
            case 6: points += 2; break;
            case 7: points += 3; break;
            case 8: points += 4; break;
            case 9: points += 5; break;
            default:
        }
        return points;
    }

    private boolean isQualitySuit(Set<Rank> ranks) {
        return ranks.stream()
                .filter(r -> r == Rank.ACE
                        || r == Rank.KING
                        || r == Rank.QUEEN
                        || r == Rank.JACK
                        || r == Rank.TEN)
                .count() >= 3;

    }

    private int adjustForSingleton(Set<Rank> ranks) {
        if (ranks.size() != 1) throw new IllegalArgumentException("Method can only be called with singleton");
        if (Stream.of(Rank.KING, Rank.QUEEN, Rank.JACK).anyMatch(ranks::contains)) {
            return -1;
        }
        return 0;
    }

    private int adjustForDoubleton(Set<Rank> ranks) {
        if (ranks.size() != 2) throw new IllegalArgumentException("Method can only be called with doubleton");
        return (ranks.contains(Rank.JACK) ||
                (ranks.contains(Rank.QUEEN) && !ranks.contains(Rank.ACE)))
                ? -1
                : 0;
    }

    private int adjustForHonors(Hand hand) {
        EnumSet<Rank> spades = hand.ranksForSuit(Suit.SPADES);
        EnumSet<Rank> hearts = hand.ranksForSuit(Suit.HEARTS);
        EnumSet<Rank> diamonds = hand.ranksForSuit(Suit.DIAMONDS);
        EnumSet<Rank> clubs = hand.ranksForSuit(Suit.CLUBS);
        int acesAndTens = acesAndTensForSuit(spades) + acesAndTensForSuit(hearts) + acesAndTensForSuit(diamonds) + acesAndTensForSuit(clubs);
        int queensAndJacks = queensAndJacksForSuit(spades) + queensAndJacksForSuit(hearts) + queensAndJacksForSuit(diamonds) + queensAndJacksForSuit(clubs);
        boolean moreAcesAndTens = acesAndTens > queensAndJacks;
        int rawAdjustment = acesAndTens - queensAndJacks;
        int absoluteAdjustment = Math.abs(rawAdjustment);
        if (absoluteAdjustment < 3) return 0;
        if (absoluteAdjustment < 6) {
            return moreAcesAndTens ? 1 : -1;
        }
        return moreAcesAndTens ? 2 : -2;
    }

    private int acesAndTensForSuit(EnumSet<Rank> ranks) {
       return (int)ranks.stream().filter(r -> r == Rank.ACE || r == Rank.TEN).count();
    }

    private int queensAndJacksForSuit(EnumSet<Rank> ranks) {
        return (int)ranks.stream().filter(r -> r == Rank.QUEEN || r == Rank.JACK).count();
    }
}
