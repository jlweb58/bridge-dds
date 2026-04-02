package com.webber.bridge_dds.service;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Rank;
import com.webber.bridge_dds.model.Suit;

import java.util.List;
import java.util.Set;

public class HandEvaluator {

    public double evaluate(Hand hand) {
        double points = 0.0;
        points += evaluateSuit(hand, Suit.SPADES);
        points += evaluateSuit(hand, Suit.HEARTS);
        points += evaluateSuit(hand, Suit.DIAMONDS);
        points += evaluateSuit(hand, Suit.CLUBS);

        points = points -1;
        if (is4_3_3_3_Distribution(hand)) {
            points += 0.5;
        }
        return points;
    }

    private boolean is4_3_3_3_Distribution(Hand hand) {
        return hand.view().values().stream()
                .mapToInt(Set::size)
                .sorted()
                .boxed()
                .toList()
                .equals(List.of(3, 3, 3, 4));
    }

    private double evaluateSuit(Hand hand, Suit suit) {
        double points;
        Set<Rank> ranks = hand.ranksForSuit(suit);
        points = ranks.stream().mapToDouble(Rank::points).sum();
        if (isTenWithJackOrTwoHigherHonors(ranks)
                || isNineWithEightOrTenOrTwoHigherHonors(ranks)
                || isNineWithThreeHigherHonors(ranks)
               ) {
            points += 0.5;
        }
        if (isSevenCardsMissingQueenOrJackOrBoth(ranks)
                || isEightPlusCardsMissingQueen(ranks)
                || isNinePlusCardsMissingQueenAndJack(ranks)) {
            points += 1.0;
        }
        points = points * ranks.size() / 10.0;
        points += calculateBonusPointsForHonorsAndShortness(ranks);
        return points;
    }

    private double calculateBonusPointsForHonorsAndShortness(Set<Rank> ranks) {
        double points = 0.0;
        if (ranks.isEmpty()) return 3.0; // void
        if (ranks.contains(Rank.ACE)) points += 3.0;
        if (ranks.contains(Rank.KING)) {
            if (ranks.size() >=2) points += 2.0;
            else points += 0.5;
        }
        if (ranks.contains(Rank.QUEEN)) {
            if (ranks.size() >=3) {
                if (ranks.contains(Rank.KING) || ranks.contains(Rank.ACE)) points += 1.0;
                else points += 0.75;
            } else if (ranks.size() == 2) {
                if (ranks.contains(Rank.KING) || ranks.contains(Rank.ACE)) points += 0.5;
                else points += 0.25;
            }
        }
        if (ranks.contains(Rank.JACK)) {
            if (hasTwoHigherHonors(ranks)) points += 0.5;
            else if (ranks.contains(Rank.ACE) || ranks.contains(Rank.KING) || ranks.contains(Rank.QUEEN)) points += 0.25;
        }
        if (ranks.contains(Rank.TEN)) {
            if (hasTwoHigherHonors(ranks)) points += 0.25;
            else if (ranks.contains(Rank.NINE) &&(ranks.contains(Rank.ACE) || ranks.contains(Rank.KING) || ranks.contains(Rank.QUEEN))) points += 0.25;
        }
        if (ranks.size() == 1) points += 2.0;
        if (ranks.size() == 2) points += 1.0;

        return points;
    }

    boolean isSevenCardsMissingQueenOrJackOrBoth(Set<Rank> ranks) {
        return ranks.size() == 7 && !ranks.contains(Rank.QUEEN) && !ranks.contains(Rank.JACK);
    }

    boolean isEightPlusCardsMissingQueen(Set<Rank> ranks) {
        return ranks.size() >= 8 && !ranks.contains(Rank.QUEEN);
    }

    boolean isNinePlusCardsMissingQueenAndJack(Set<Rank> ranks) {
        return ranks.size() >= 9 && !(ranks.contains(Rank.QUEEN) && ranks.contains(Rank.JACK));
    }

    boolean isTenWithJackOrTwoHigherHonors(Set<Rank> ranks) {
        if (ranks.size() < 2 || ranks.size() > 6) return false;
        if (!ranks.contains(Rank.TEN)) return false;

        return ranks.contains(Rank.JACK)
                || hasTwoHigherHonors(ranks);
    }

    private boolean hasTwoHigherHonors(Set<Rank> ranks) {
        return ranks.stream()
                .filter(r -> r == Rank.ACE || r == Rank.KING || r == Rank.QUEEN)
                .count() >= 2;
    }

    boolean isNineWithEightOrTenOrTwoHigherHonors(Set<Rank> ranks) {
        if (ranks.size() < 2 || ranks.size() > 6) return false;
        if (!ranks.contains(Rank.NINE)) return false;

        return (ranks.contains(Rank.EIGHT)
                || ranks.contains(Rank.TEN)
                || hasTwoHigherHonors(ranks));
    }

    boolean isNineWithThreeHigherHonors(Set<Rank> ranks) {
        if (ranks.size() < 4 || ranks.size() > 6) return false;
        if (!ranks.contains(Rank.NINE)) return false;

        return ranks.stream()
                .filter(r -> r == Rank.ACE || r == Rank.KING || r == Rank.QUEEN)
                .count() == 3;
    }
}
