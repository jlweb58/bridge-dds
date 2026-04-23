package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Rank;
import com.webber.bridge_dds.model.Suit;
import com.webber.bridge_dds.service.HandEvaluator;
import com.webber.bridge_dds.service.HandEvaluatorType;
import com.webber.bridge_dds.service.StandardHandEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HandGenerationTest {

    @Autowired
    private HandGenerationService handGenerationService;

    private final HandEvaluator handEvaluator = new StandardHandEvaluator();


    @Test
    public void testGenerateTenHands() {
        int numOfHands = 10;

        EnumMap<Suit, SuitLengthRange> westSuitLengthRange = new EnumMap<>(Suit.class);
        westSuitLengthRange.put(Suit.SPADES, new SuitLengthRange(2, 4));
        westSuitLengthRange.put(Suit.HEARTS, new SuitLengthRange(2, 4));
        westSuitLengthRange.put(Suit.DIAMONDS, new SuitLengthRange(2, 5));
        westSuitLengthRange.put(Suit.CLUBS, new SuitLengthRange(2, 5));
        HandDistribution westDistribution = new HandDistribution(westSuitLengthRange);
        HandGenerationParameters westParameters = new HandGenerationParameters(15, 17, westDistribution);

        EnumMap<Suit, SuitLengthRange> eastSuitLengthRange = new EnumMap<>(Suit.class);
        eastSuitLengthRange.put(Suit.SPADES, new SuitLengthRange(1, 6));
        eastSuitLengthRange.put(Suit.HEARTS, new SuitLengthRange(1, 6));
        eastSuitLengthRange.put(Suit.DIAMONDS, new SuitLengthRange(1, 6));
        eastSuitLengthRange.put(Suit.CLUBS, new SuitLengthRange(1, 6));
        HandDistribution eastDistribution = new HandDistribution(eastSuitLengthRange);
        HandGenerationParameters eastParameters = new HandGenerationParameters(10, 17, eastDistribution);

        Map<Player, HandGenerationParameters> parametersMap = new HashMap<>();
        parametersMap.put(Player.WEST, westParameters);
        parametersMap.put(Player.EAST, eastParameters);
        HandGenerationRequest request = new HandGenerationRequest(parametersMap, numOfHands, HandEvaluatorType.STANDARD.identifier(), null);
        HandGenerationResponse response = handGenerationService.generateHands(request);
        assertNotNull(response);
        assertEquals(numOfHands, response.hands().size());
        response.hands().forEach(hand -> validateHand(hand.west(), westParameters));
        response.hands().forEach(hand -> validateHand(hand.east(), eastParameters));
    }

    @Test
    public void testItGeneratesANTOpenerFacingA5CardSpadeSuit() {
        int numOfHands = 10;
        EnumMap<Suit, SuitLengthRange> westSuitLengthRange = new EnumMap<>(Suit.class);
        westSuitLengthRange.put(Suit.SPADES, new SuitLengthRange(2, 4));
        westSuitLengthRange.put(Suit.HEARTS, new SuitLengthRange(2, 4));
        westSuitLengthRange.put(Suit.DIAMONDS, new SuitLengthRange(2, 5));
        westSuitLengthRange.put(Suit.CLUBS, new SuitLengthRange(2, 5));
        HandDistribution westDistribution = new HandDistribution(westSuitLengthRange);
        HandGenerationParameters westParameters = new HandGenerationParameters(15, 17, westDistribution);

        EnumMap<Suit, SuitLengthRange> eastSuitLengthRange = new EnumMap<>(Suit.class);
        eastSuitLengthRange.put(Suit.SPADES, new SuitLengthRange(5, 6));
        eastSuitLengthRange.put(Suit.HEARTS, new SuitLengthRange(1, 3));
        eastSuitLengthRange.put(Suit.DIAMONDS, new SuitLengthRange(1, 3));
        eastSuitLengthRange.put(Suit.CLUBS, new SuitLengthRange(1, 3));
        HandDistribution eastDistribution = new HandDistribution(eastSuitLengthRange);
        HandGenerationParameters eastParameters = new HandGenerationParameters(10, 17, eastDistribution);

        Map<Player, HandGenerationParameters> parametersMap = new HashMap<>();
        parametersMap.put(Player.WEST, westParameters);
        parametersMap.put(Player.EAST, eastParameters);
        HandGenerationRequest request = new HandGenerationRequest(parametersMap, numOfHands, HandEvaluatorType.STANDARD.identifier(), null);
        HandGenerationResponse response = handGenerationService.generateHands(request);
        assertNotNull(response);
        assertEquals(numOfHands, response.hands().size());
        response.hands().forEach(hand -> validateHand(hand.west(), westParameters));
        response.hands().forEach(hand -> validateHand(hand.east(), eastParameters));
    }

    private void validateHand(List<String> handCodes, HandGenerationParameters parameters) {
        assertNotNull(handCodes);
        assertEquals(13, handCodes.size());
        Hand hand = handFromCodeString(handCodes);
        EnumSet<Rank> spades = hand.ranksForSuit(Suit.SPADES);
        EnumSet<Rank> hearts = hand.ranksForSuit(Suit.HEARTS);
        EnumSet<Rank> diamonds = hand.ranksForSuit(Suit.DIAMONDS);
        EnumSet<Rank> clubs = hand.ranksForSuit(Suit.CLUBS);
        HandDistribution handDistribution = parameters.handDistribution();

        validateSuitLengthRange(handDistribution.suitLengths().get(Suit.SPADES), spades);
        validateSuitLengthRange(handDistribution.suitLengths().get(Suit.HEARTS), hearts);
        validateSuitLengthRange(handDistribution.suitLengths().get(Suit.DIAMONDS), diamonds);
        validateSuitLengthRange(handDistribution.suitLengths().get(Suit.CLUBS), clubs);

        validatePointCount(parameters, hand);

    }

    private Hand handFromCodeString(List<String> cardCodes) {
        Hand hand = new Hand();
        cardCodes.forEach(cardCode -> hand.add(Card.fromCode(cardCode)));
        return hand;
    }

    private void validateSuitLengthRange(SuitLengthRange range, EnumSet<Rank> cards) {
        int size = cards.size();
        assertTrue(
                size >= range.min() && size <= range.max(),
                () -> "Expected suit size in range [" + range.min() + ", " + range.max()
                        + "], but was " + size + " for cards: " + cards
        );    }

    private void validatePointCount(HandGenerationParameters parameters, Hand hand) {
        double pointCount = handEvaluator.evaluate(hand);
        assertTrue(
                pointCount >= parameters.minPoints() && pointCount <= parameters.maxPoints(),
                () -> "Expected point count in range [" + parameters.minPoints() + ", "
                        + parameters.maxPoints() + "], but was " + pointCount + " for hand: " + hand.view()
        );    }
}
