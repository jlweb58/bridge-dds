package com.webber.bridge_dds.service;

import com.webber.bridge_dds.handgeneration.HandGenerationParameters;
import com.webber.bridge_dds.handgeneration.HandParametersMatcher;
import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Deal;
import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Deals random completions of a partially known deal, subject to per-seat constraints.
 * <p>
 * Uses joint rejection sampling: all unknown seats are dealt at once and the deal is accepted only
 * if every constrained seat matches. This yields a uniform sample over all matching deals. (Retrying
 * a single seat while keeping the others fixed, as hand generation does, would bias the sample.)
 * <p>
 * Not thread-safe; use one instance per thread.
 */
class ConstrainedDealSampler {

    private final Map<Player, List<Card>> knownHands;
    private final Card[] cards;
    private final Player[] seats;
    private final HandGenerationParameters[] seatConstraints;
    private final HandParametersMatcher matcher;
    private final HandEvaluator handEvaluator;

    private long attempts;

    /**
     * @param knownHands  fully specified 13-card hands (must be disjoint)
     * @param constraints constraints for seats not in {@code knownHands}; unconstrained seats are dealt freely
     */
    ConstrainedDealSampler(
            Map<Player, List<Card>> knownHands,
            Map<Player, HandGenerationParameters> constraints,
            HandParametersMatcher matcher,
            HandEvaluator handEvaluator
    ) {
        this.knownHands = knownHands;
        this.matcher = matcher;
        this.handEvaluator = handEvaluator;

        // constrained seats first, so that a deal can be rejected as early as possible
        List<Player> unknownSeats = new ArrayList<>(4);
        for (Player p : Player.values()) {
            if (!knownHands.containsKey(p) && constraints.containsKey(p)) unknownSeats.add(p);
        }
        this.seatConstraints = unknownSeats.stream().map(constraints::get).toArray(HandGenerationParameters[]::new);
        for (Player p : Player.values()) {
            if (!knownHands.containsKey(p) && !constraints.containsKey(p)) unknownSeats.add(p);
        }
        this.seats = unknownSeats.toArray(new Player[0]);

        EnumSet<Card> known = EnumSet.noneOf(Card.class);
        knownHands.values().forEach(known::addAll);
        this.cards = EnumSet.complementOf(known).toArray(new Card[0]);
    }

    boolean hasConstraints() {
        return seatConstraints.length > 0;
    }

    /** Total number of deals tried so far (accepted and rejected). */
    long attempts() {
        return attempts;
    }

    /**
     * @return a matching deal, or null if none was found within {@code maxAttempts} tries
     */
    Deal sample(Random rng, long maxAttempts) {
        for (long a = 0; a < maxAttempts; a++) {
            attempts++;

            if (constrainedSeatsMatch(rng)) {
                shuffle(rng, seatConstraints.length * 13, cards.length);
                return toDeal();
            }
        }
        return null;
    }

    /**
     * Shuffles lazily, one seat at a time, so that a rejected deal usually only costs 13 random draws.
     */
    private boolean constrainedSeatsMatch(Random rng) {
        for (int s = 0; s < seatConstraints.length; s++) {
            shuffle(rng, s * 13, (s + 1) * 13);
            Hand hand = new Hand();
            for (int k = s * 13; k < (s + 1) * 13; k++) hand.add(cards[k]);
            if (!matcher.matches(handEvaluator, seatConstraints[s], hand)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Forward Fisher-Yates step for positions [from, to): each position receives a uniformly chosen card
     * from the not yet placed ones. Positions [0, to) are then a uniform random sample of the cards,
     * whatever the array's previous order was.
     */
    private void shuffle(Random rng, int from, int to) {
        for (int k = from; k < Math.min(to, cards.length - 1); k++) {
            int j = k + rng.nextInt(cards.length - k);
            Card tmp = cards[k];
            cards[k] = cards[j];
            cards[j] = tmp;
        }
    }

    private Deal toDeal() {
        Deal deal = new Deal();
        deal.setFirst(Player.NORTH); // arbitrary; DDS just needs consistent seat assignments
        knownHands.forEach((player, hand) -> hand.forEach(c -> deal.give(player, c)));
        for (int s = 0; s < seats.length; s++) {
            for (int k = s * 13; k < (s + 1) * 13; k++) deal.give(seats[s], cards[k]);
        }
        return deal;
    }
}
