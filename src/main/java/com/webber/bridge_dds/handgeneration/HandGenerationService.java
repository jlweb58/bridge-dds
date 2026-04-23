package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.CardDeck;
import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Rank;
import com.webber.bridge_dds.model.Suit;
import com.webber.bridge_dds.service.HandEvaluator;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class HandGenerationService {

    private final HandEvaluator handEvaluator;

    public HandGenerationService(HandEvaluator handEvaluator) {
        this.handEvaluator = handEvaluator;
    }

    public Map<Player, List<Hand>> generateHands(HandGenerationRequest request) {
        long start = System.currentTimeMillis();
        Map<Player, List<Hand>> hands = new EnumMap<>(Player.class);
        for (int i = 0; i < request.numberOfHands(); i++) {
            Map<Player, Hand> generatedHand = generateSingleHand(request);
            generatedHand.forEach((player, hand) ->
                    hands.computeIfAbsent(player, p -> new java.util.ArrayList<>()).add(hand)
            );
        }
        long end = System.currentTimeMillis();
        System.out.println("Hand generation took " + (end - start) + " ms");
        return hands;
    }

    private Map<Player, Hand> generateSingleHand(HandGenerationRequest request) {
        Map<Player, Hand> generatedHand = new EnumMap<>(Player.class);
        while(generatedHand.size() != 2) {
            CardDeck cardDeck = new CardDeck();
            Hand westHand = new Hand();
            Hand eastHand = new Hand();
            for (int i = 0; i < 13; i++) {
                westHand.add(cardDeck.dealCard());
                eastHand.add(cardDeck.dealCard());
            }
            if (validateGeneratedHand(request.parameters().get(Player.WEST), westHand) && validateGeneratedHand(request.parameters().get(Player.EAST), eastHand)) {
                generatedHand.put(Player.WEST, westHand);
                generatedHand.put(Player.EAST, eastHand);
            }
        }
        return generatedHand;
    }

    private boolean validateGeneratedHand(HandGenerationParameters parameters, Hand hand) {
        assert hand != null;
        assert hand.size() == 13;
        EnumSet<Rank> spades = hand.ranksForSuit(Suit.SPADES);
        EnumSet<Rank> hearts = hand.ranksForSuit(Suit.HEARTS);
        EnumSet<Rank> diamonds = hand.ranksForSuit(Suit.DIAMONDS);
        EnumSet<Rank> clubs = hand.ranksForSuit(Suit.CLUBS);
        HandDistribution handDistribution = parameters.handDistribution();

        return validateSuitLengthRange(handDistribution.suitLengths().get(Suit.SPADES), spades)
        && validateSuitLengthRange(handDistribution.suitLengths().get(Suit.HEARTS), hearts)
        && validateSuitLengthRange(handDistribution.suitLengths().get(Suit.DIAMONDS), diamonds)
        && validateSuitLengthRange(handDistribution.suitLengths().get(Suit.CLUBS), clubs)
        && validatePointCount(parameters, hand);
    }

    private boolean validateSuitLengthRange(SuitLengthRange range, EnumSet<Rank> cards) {
        int size = cards.size();
        return size >= range.min() && size <= range.max();
    }

    private boolean validatePointCount(HandGenerationParameters parameters, Hand hand) {
        double pointCount = handEvaluator.evaluate(hand);
        return pointCount >= parameters.minPoints() && pointCount <= parameters.maxPoints();
    }

}
