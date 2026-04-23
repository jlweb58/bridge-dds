package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.CardDeck;
import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Rank;
import com.webber.bridge_dds.model.Suit;
import com.webber.bridge_dds.model.Vulnerability;
import com.webber.bridge_dds.service.HandEvaluator;
import com.webber.bridge_dds.service.HandEvaluatorFactory;
import com.webber.bridge_dds.service.HandEvaluatorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HandGenerationService {

    private final HandEvaluatorFactory handEvaluatorFactory;

    private final HandContractScoringService handContractScoringService;

    private static final int NUMBER_OF_SAMPLES = 100;

    private static final Player[] DEALER_CYCLE = {
            Player.NORTH, Player.EAST, Player.SOUTH, Player.WEST
    };

    private static final Vulnerability[] VULNERABILITY_CYCLE = {
            Vulnerability.NONE,
            Vulnerability.NS,
            Vulnerability.EW,
            Vulnerability.BOTH,
            Vulnerability.NS,
            Vulnerability.EW,
            Vulnerability.BOTH,
            Vulnerability.NONE,
            Vulnerability.EW,
            Vulnerability.BOTH,
            Vulnerability.NONE,
            Vulnerability.NS,
            Vulnerability.BOTH,
            Vulnerability.NONE,
            Vulnerability.NS,
            Vulnerability.EW
    };

    public HandGenerationService(HandEvaluatorFactory handEvaluatorFactory, HandContractScoringService handContractScoringService) {
        this.handEvaluatorFactory = handEvaluatorFactory;
        this.handContractScoringService = handContractScoringService;
    }

    public HandGenerationResponse generateHands(HandGenerationRequest request) {
        long start = System.currentTimeMillis();

        Map<Player, List<Hand>> hands = new EnumMap<>(Player.class);
        for (int i = 0; i < request.numberOfHands(); i++) {
            Map<Player, Hand> generatedHand = generateSingleHand(request);
            generatedHand.forEach((player, hand) ->
                    hands.computeIfAbsent(player, p -> new java.util.ArrayList<>()).add(hand)
            );
        }
        List<HandGenerationResponse.GeneratedHandDto> responseHands = new java.util.ArrayList<>();
        List<ContractSuggestion> contractSuggestions =
                request.contractSuggestions() == null ? List.of() : request.contractSuggestions();

        int handCount = hands.getOrDefault(Player.WEST, List.of()).size();
        for (int i = 0; i < handCount; i++) {
            int boardNumber = i + 1;
            Hand westHand = hands.get(Player.WEST).get(i);
            Hand eastHand = hands.get(Player.EAST).get(i);

            responseHands.add(new HandGenerationResponse.GeneratedHandDto(
                    dealerForBoard(boardNumber),
                    vulnerabilityForBoard(boardNumber),
                    westHand.toCardCodes(),
                    eastHand.toCardCodes(),
                    contractSuggestions.isEmpty()
                            ? List.of()
                            : handContractScoringService.scoreContracts(
                            westHand,
                            eastHand,
                            contractSuggestions,
                            NUMBER_OF_SAMPLES,
                            null
                    )
            ));
        }
        long end = System.currentTimeMillis();
        log.info("Hand generation took " + (end - start) + " ms");
        return new HandGenerationResponse(responseHands);
    }

    private static Player dealerForBoard(int boardNumber) {
        return DEALER_CYCLE[(boardNumber - 1) % 4];
    }

    private static Vulnerability vulnerabilityForBoard(int boardNumber) {
        return VULNERABILITY_CYCLE[(boardNumber - 1) % 16];
    }

    private Map<Player, Hand> generateSingleHand(HandGenerationRequest request) {
        HandEvaluatorType handEvaluatorType = request.evaluator() == null ? HandEvaluatorType.STANDARD : HandEvaluatorType.fromId(request.evaluator());
        HandEvaluator handEvaluator = handEvaluatorFactory.fromType(handEvaluatorType);
        Map<Player, Hand> generatedHand = new EnumMap<>(Player.class);
        while(generatedHand.size() != 2) {
            CardDeck cardDeck = new CardDeck();
            Hand westHand = new Hand();
            Hand eastHand = new Hand();
            for (int i = 0; i < 13; i++) {
                westHand.add(cardDeck.dealCard());
                eastHand.add(cardDeck.dealCard());
            }
            if (validateGeneratedHand(handEvaluator, request.parameters().get(Player.WEST), westHand) && validateGeneratedHand(handEvaluator, request.parameters().get(Player.EAST), eastHand)) {
                generatedHand.put(Player.WEST, westHand);
                generatedHand.put(Player.EAST, eastHand);
            }
        }
        return generatedHand;
    }

    private boolean validateGeneratedHand(HandEvaluator handEvaluator, HandGenerationParameters parameters, Hand hand) {
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
        && validatePointCount(handEvaluator, parameters, hand);
    }

    private boolean validateSuitLengthRange(SuitLengthRange range, EnumSet<Rank> cards) {
        int size = cards.size();
        return size >= range.min() && size <= range.max();
    }

    private boolean validatePointCount(HandEvaluator handEvaluator, HandGenerationParameters parameters, Hand hand) {
        double pointCount = handEvaluator.evaluate(hand);
        return pointCount >= parameters.minPoints() && pointCount <= parameters.maxPoints();
    }

}
