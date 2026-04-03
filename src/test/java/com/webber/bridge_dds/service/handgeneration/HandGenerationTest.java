package com.webber.bridge_dds.service.handgeneration;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Suit;
import com.webber.bridge_dds.service.StandardHandEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HandGenerationTest {

    private HandGenerationService handGenerationService;
    private final Player westPlayer = Player.WEST;
    private final Player eastPlayer = Player.EAST;

    @BeforeEach
    public void setUp() {
        handGenerationService = new HandGenerationService(new StandardHandEvaluator());
    }

    @Test
    public void testGenerateOneHand() {
        Map<Player, HandGenerationParameters> parameters = new HashMap<>();
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
        Map<Player, List<Hand>> hands = handGenerationService.generateHand(parametersMap);
        assertNotNull(hands);
        assertEquals(1, hands.get(westPlayer).size());
        assertEquals(1, hands.get(eastPlayer).size());
    }
}
