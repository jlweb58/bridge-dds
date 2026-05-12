package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Card;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class HandGenerationService {

    private final HandEvaluatorFactory handEvaluatorFactory;

    private final HandContractScoringService handContractScoringService;

    private final PreemptSuitQualityEvaluator preemptSuitQualityEvaluator;

    private static final int NUMBER_OF_SAMPLES = 100;

    private static final int MAX_TOTAL_WEST_CANDIDATES_PER_HAND = 5_000_000;

    private static final int MAX_VALID_WEST_HANDS_PER_HAND = 5_000;

    private static final int MAX_EAST_ATTEMPTS_PER_WEST_HAND = 100;

    private static final int ATTEMPT_LOG_INTERVAL = 100_000;

    private static final int REPRESENTATIVE_FAILURE_LOG_LIMIT = 5;

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

    public HandGenerationService(HandEvaluatorFactory handEvaluatorFactory, HandContractScoringService handContractScoringService, PreemptSuitQualityEvaluator preemptSuitQualityEvaluator) {
        this.handEvaluatorFactory = handEvaluatorFactory;
        this.handContractScoringService = handContractScoringService;
        this.preemptSuitQualityEvaluator = preemptSuitQualityEvaluator;
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
        log.info(
                "Generated {} hand pair(s) in {} ms using evaluator {}",
                responseHands.size(),
                end - start,
                request.evaluator() == null ? HandEvaluatorType.STANDARD.identifier() : request.evaluator()
        );        return new HandGenerationResponse(responseHands);
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

        HandGenerationParameters westParameters = request.parameters().get(Player.WEST);
        HandGenerationParameters eastParameters = request.parameters().get(Player.EAST);
        if (log.isDebugEnabled()) {
            log.debug(
                    "Generating hand pair. West={}, East={}, evaluator={}",
                    generationParameterSummary(westParameters),
                    generationParameterSummary(eastParameters),
                    handEvaluatorType.identifier()
            );
        }
        int validWestHands = 0;
        int eastAttempts = 0;
        int westDistributionFailures = 0;
        int westPointFailures = 0;
        int westSuitQualityFailures = 0;

        for (int westCandidateAttempt = 1; westCandidateAttempt <= MAX_TOTAL_WEST_CANDIDATES_PER_HAND; westCandidateAttempt++) {
            Hand westHand = generateWestCandidate();

            if (!validateDistribution(westParameters, westHand)) {
                westDistributionFailures++;

                if (log.isDebugEnabled() && westDistributionFailures <= REPRESENTATIVE_FAILURE_LOG_LIMIT) {
                    log.debug(
                            "Representative West distribution failure #{}: shape={}, hand={}, condition={}, distribution={}",
                            westDistributionFailures,
                            handShape(westHand),
                            westHand.toCardCodes(),
                            westParameters.condition(),
                            westParameters.handDistribution()
                    );
                }
                logGenerationProgressIfNeeded(westCandidateAttempt, validWestHands, eastAttempts, westDistributionFailures, westPointFailures, westSuitQualityFailures);
                continue;
            }

            if (!validatePointCount(handEvaluator, westParameters, westHand)) {
                westPointFailures++;
                logGenerationProgressIfNeeded(westCandidateAttempt, validWestHands, eastAttempts, westDistributionFailures, westPointFailures, westSuitQualityFailures);
                continue;
            }

            if (!validateSuitQualityRequirements(westParameters, westHand)) {
                westSuitQualityFailures++;
                logGenerationProgressIfNeeded(westCandidateAttempt, validWestHands, eastAttempts, westDistributionFailures, westPointFailures, westSuitQualityFailures);
                continue;
            }

            validWestHands++;

            if (validWestHands > MAX_VALID_WEST_HANDS_PER_HAND) {
                break;
            }

            List<Card> remainingCards = collectRemainingCards(westHand);

            for (int eastAttempt = 0; eastAttempt < MAX_EAST_ATTEMPTS_PER_WEST_HAND; eastAttempt++) {
                eastAttempts++;

                Hand eastHand = dealRandomHandFrom(remainingCards);

                if (validateGeneratedHand(handEvaluator, eastParameters, eastHand)) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Generated hand pair after {} West candidate(s), {} valid West hand(s), {} East attempt(s). West shape={}, East shape={}",
                                westCandidateAttempt,
                                validWestHands,
                                eastAttempts,
                                handShape(westHand),
                                handShape(eastHand)
                        );
                    }
                    Map<Player, Hand> generatedHand = new EnumMap<>(Player.class);
                    generatedHand.put(Player.WEST, westHand);
                    generatedHand.put(Player.EAST, eastHand);
                    return generatedHand;
                }
            }

            logGenerationProgressIfNeeded(westCandidateAttempt, validWestHands, eastAttempts, westDistributionFailures, westPointFailures, westSuitQualityFailures);
        }

        throw new IllegalStateException(
                "Unable to generate a matching West/East hand pair. "
                        + "West candidates tried: " + MAX_TOTAL_WEST_CANDIDATES_PER_HAND
                        + ". Valid West hands found: " + validWestHands
                        + ". East attempts made: " + eastAttempts
                        + ". West distribution failures: " + westDistributionFailures
                        + ". West point failures: " + westPointFailures
                        + ". West suit quality failures: " + westSuitQualityFailures
                        + ". The requested constraints may be too restrictive, incompatible, or incorrectly modeled."
        );
    }

    private String handShape(Hand hand) {
        return hand.ranksForSuit(Suit.SPADES).size()
                + "-"
                + hand.ranksForSuit(Suit.HEARTS).size()
                + "-"
                + hand.ranksForSuit(Suit.DIAMONDS).size()
                + "-"
                + hand.ranksForSuit(Suit.CLUBS).size();
    }

    private void logGenerationProgressIfNeeded(
            int westCandidateAttempt,
            int validWestHands,
            int eastAttempts,
            int westDistributionFailures,
            int westPointFailures,
            int westSuitQualityFailures
    ) {
        if (westCandidateAttempt % ATTEMPT_LOG_INTERVAL == 0) {
            log.debug(
                    "Hand generation progress: {} West candidates tried, {} valid West hands found, {} East attempts made, {} distribution failures, {} point failures, {} suit quality failures",
                    westCandidateAttempt,
                    validWestHands,
                    eastAttempts,
                    westDistributionFailures,
                    westPointFailures,
                    westSuitQualityFailures
            );
        }
    }

    private String generationParameterSummary(HandGenerationParameters parameters) {
        return "points=" + parameters.minPoints() + "-" + parameters.maxPoints()
                + ", distribution=" + parameters.handDistribution()
                + ", condition=" + parameters.condition()
                + ", suitQualityRequirements=" + parameters.suitQualityRequirements();
    }

    private Hand generateWestCandidate() {
        CardDeck cardDeck = new CardDeck();
        Hand westHand = new Hand();

        for (int i = 0; i < 13; i++) {
            westHand.add(cardDeck.dealCard());
        }

        return westHand;
    }


    private List<Card> collectRemainingCards(Hand westHand) {
        List<Card> remainingCards = new ArrayList<>(39);

        for (Card card : Card.values()) {
            if (!westHand.contains(card)) {
                remainingCards.add(card);
            }
        }

        return remainingCards;
    }

    private Hand dealRandomHandFrom(List<Card> availableCards) {
        List<Card> shuffledCards = new ArrayList<>(availableCards);
        Collections.shuffle(shuffledCards, ThreadLocalRandom.current());

        Hand hand = new Hand();
        for (int i = 0; i < 13; i++) {
            hand.add(shuffledCards.get(i));
        }

        return hand;
    }

    private boolean validateGeneratedHand(HandEvaluator handEvaluator, HandGenerationParameters parameters, Hand hand) {
        assert hand != null;
        assert hand.size() == 13;
        return validateDistribution(parameters, hand)
                && validatePointCount(handEvaluator, parameters, hand)
                && validateSuitQualityRequirements(parameters, hand);
    }

    private boolean validateSuitQualityRequirements(HandGenerationParameters parameters, Hand hand) {
        Map<Suit, SuitQualityRequirement> requirements = parameters.suitQualityRequirements();
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        return requirements.entrySet().stream()
                .allMatch(entry -> preemptSuitQualityEvaluator.satisfies(
                        hand.ranksForSuit(entry.getKey()),
                        entry.getValue()
                ));
    }

    private boolean validateDistribution(HandGenerationParameters parameters, Hand hand) {
        if (parameters.condition() != null) {
            return parameters.condition().matches(hand);
        }

        EnumSet<Rank> spades = hand.ranksForSuit(Suit.SPADES);
        EnumSet<Rank> hearts = hand.ranksForSuit(Suit.HEARTS);
        EnumSet<Rank> diamonds = hand.ranksForSuit(Suit.DIAMONDS);
        EnumSet<Rank> clubs = hand.ranksForSuit(Suit.CLUBS);
        HandDistribution handDistribution = parameters.handDistribution();
        return validateSuitLengthRange(handDistribution.suitLengths().get(Suit.SPADES), spades)
                && validateSuitLengthRange(handDistribution.suitLengths().get(Suit.HEARTS), hearts)
                && validateSuitLengthRange(handDistribution.suitLengths().get(Suit.DIAMONDS), diamonds)
                && validateSuitLengthRange(handDistribution.suitLengths().get(Suit.CLUBS), clubs);

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
