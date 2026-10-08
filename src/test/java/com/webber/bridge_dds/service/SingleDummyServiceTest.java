package com.webber.bridge_dds.service;

import com.webber.bridge_dds.controller.SingleDummyAnalyzeRequest;
import com.webber.bridge_dds.controller.SingleDummyAnalyzeResponse;
import com.webber.bridge_dds.handgeneration.HandDistribution;
import com.webber.bridge_dds.handgeneration.HandGenerationParameters;
import com.webber.bridge_dds.handgeneration.HandParametersMatcher;
import com.webber.bridge_dds.handgeneration.SuitLengthRange;
import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Deal;
import com.webber.bridge_dds.model.Denomination;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Suit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class SingleDummyServiceTest {

    // South: AK42 / K32 / Q32 / 432 (12 HCP), North: 765 / QJ54 / AK5 / A65 (14 HCP)
    private static final List<String> SOUTH = List.of(
            "SA", "SK", "S4", "S2", "HK", "H3", "H2", "DQ", "D3", "D2", "C4", "C3", "C2");
    private static final List<String> NORTH = List.of(
            "S7", "S6", "S5", "HQ", "HJ", "H5", "H4", "DA", "DK", "D5", "CA", "C6", "C5");

    private static final SingleDummyAnalyzeRequest.Contract THREE_NT =
            new SingleDummyAnalyzeRequest.Contract(3, Denomination.NOTRUMP);

    @Autowired
    private SingleDummyService singleDummyService;

    @Autowired
    private HandParametersMatcher handParametersMatcher;

    private final HandEvaluator standardEvaluator = new StandardHandEvaluator();

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void testRequestJsonDeserialization() {
        String legacy = """
                {"declarer":"SOUTH","dummy":"NORTH","contract":{"level":3,"denomination":"NOTRUMP"},
                 "hands":{"SOUTH":["SA"],"NORTH":["S5"]},"samples":100,"seed":null}
                """;
        SingleDummyAnalyzeRequest legacyRequest = jsonMapper.readValue(legacy, SingleDummyAnalyzeRequest.class);
        assertEquals(100, legacyRequest.samples());
        assertNull(legacyRequest.constraints());
        assertNull(legacyRequest.evaluator());

        String advanced = """
                {"declarer":"SOUTH","dummy":"NORTH","contract":{"level":3,"denomination":"NOTRUMP"},
                 "hands":{"SOUTH":["SA"]},"samples":100,"evaluator":"bergen",
                 "constraints":{"WEST":{"minPoints":10,"maxPoints":12,
                                        "handDistribution":{"suitLengths":{"SPADES":{"min":5,"max":7}}}}}}
                """;
        SingleDummyAnalyzeRequest advancedRequest = jsonMapper.readValue(advanced, SingleDummyAnalyzeRequest.class);
        HandGenerationParameters west = advancedRequest.constraints().get(Player.WEST);
        assertEquals(10, west.minPoints());
        assertEquals(new SuitLengthRange(5, 7), west.handDistribution().suitLengths().get(Suit.SPADES));
        assertEquals("bergen", advancedRequest.evaluator());
    }

    @Test
    public void testLegacyRequestStillWorks() {
        SingleDummyAnalyzeRequest request = new SingleDummyAnalyzeRequest(
                Player.SOUTH, Player.NORTH, THREE_NT,
                Map.of(Player.SOUTH, SOUTH, Player.NORTH, NORTH),
                100, 42L);

        SingleDummyAnalyzeResponse response = singleDummyService.analyze(request);

        assertEquals(100, response.samples());
        assertEquals(100, response.tricksHistogram().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    public void testDefenderConstraintsAreApplied() {
        Map<Player, HandGenerationParameters> constraints = new EnumMap<>(Player.class);
        constraints.put(Player.WEST, westFiveToSevenSpadesTenToTwelve());
        constraints.put(Player.EAST, new HandGenerationParameters(0, 6, null, null, null));

        SingleDummyAnalyzeRequest request = new SingleDummyAnalyzeRequest(
                Player.SOUTH, Player.NORTH, THREE_NT,
                Map.of(Player.SOUTH, SOUTH, Player.NORTH, NORTH),
                200, 7L, constraints, null);

        SingleDummyAnalyzeResponse response = singleDummyService.analyze(request);
        assertEquals(200, response.samples());
    }

    @Test
    public void testConstrainedDummyInsteadOfCards() {
        Map<Player, HandGenerationParameters> constraints = new EnumMap<>(Player.class);
        EnumMap<Suit, SuitLengthRange> northShape = new EnumMap<>(Suit.class);
        northShape.put(Suit.HEARTS, new SuitLengthRange(4, 5));
        constraints.put(Player.NORTH, new HandGenerationParameters(10, 12, new HandDistribution(northShape), null, null));
        constraints.put(Player.WEST, westFiveToSevenSpadesTenToTwelve());

        SingleDummyAnalyzeRequest request = new SingleDummyAnalyzeRequest(
                Player.SOUTH, Player.NORTH, THREE_NT,
                Map.of(Player.SOUTH, SOUTH),
                100, 7L, constraints, "standard");

        SingleDummyAnalyzeResponse response = singleDummyService.analyze(request);
        assertEquals(100, response.samples());
    }

    @Test
    public void testSampledDealsSatisfyConstraints() {
        HandGenerationParameters west = westFiveToSevenSpadesTenToTwelve();
        EnumMap<Suit, SuitLengthRange> northShape = new EnumMap<>(Suit.class);
        northShape.put(Suit.HEARTS, new SuitLengthRange(4, 5));
        HandGenerationParameters north = new HandGenerationParameters(10, 12, new HandDistribution(northShape), null, null);

        Map<Player, List<Card>> known = Map.of(Player.SOUTH, SOUTH.stream().map(Card::fromCode).toList());
        Map<Player, HandGenerationParameters> constraints = Map.of(Player.WEST, west, Player.NORTH, north);
        ConstrainedDealSampler sampler = new ConstrainedDealSampler(known, constraints, handParametersMatcher, standardEvaluator);

        Random rng = new Random(1);
        for (int i = 0; i < 200; i++) {
            Deal deal = sampler.sample(rng, 1_000_000);
            assertNotNull(deal);
            for (Player p : Player.values()) assertEquals(13, deal.hand(p).size());
            for (String code : SOUTH) assertEquals(Player.SOUTH, deal.ownerOf(Card.fromCode(code)));
            assertTrue(handParametersMatcher.matches(standardEvaluator, west, deal.hand(Player.WEST)));
            assertTrue(handParametersMatcher.matches(standardEvaluator, north, deal.hand(Player.NORTH)));
        }
    }

    @Test
    public void testImpossibleConstraintsAreRejected() {
        // South + North hold 7 spades, so West cannot hold 7+
        EnumMap<Suit, SuitLengthRange> shape = new EnumMap<>(Suit.class);
        shape.put(Suit.SPADES, new SuitLengthRange(7, 13));
        Map<Player, HandGenerationParameters> constraints =
                Map.of(Player.WEST, new HandGenerationParameters(null, null, new HandDistribution(shape), null, null));

        SingleDummyAnalyzeRequest request = new SingleDummyAnalyzeRequest(
                Player.SOUTH, Player.NORTH, THREE_NT,
                Map.of(Player.SOUTH, SOUTH, Player.NORTH, NORTH),
                40, 1L, constraints, null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> singleDummyService.analyze(request));
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, ex.getStatusCode());
    }

    @Test
    public void testDeclarerConstraintsAreNotAllowed() {
        Map<Player, HandGenerationParameters> constraints =
                Map.of(Player.SOUTH, new HandGenerationParameters(10, 12, null, null, null));

        SingleDummyAnalyzeRequest request = new SingleDummyAnalyzeRequest(
                Player.SOUTH, Player.NORTH, THREE_NT,
                Map.of(Player.SOUTH, SOUTH, Player.NORTH, NORTH),
                40, 1L, constraints, null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> singleDummyService.analyze(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void testDummyCardsAndConstraintsTogetherAreNotAllowed() {
        Map<Player, HandGenerationParameters> constraints =
                Map.of(Player.NORTH, new HandGenerationParameters(10, 12, null, null, null));

        SingleDummyAnalyzeRequest request = new SingleDummyAnalyzeRequest(
                Player.SOUTH, Player.NORTH, THREE_NT,
                Map.of(Player.SOUTH, SOUTH, Player.NORTH, NORTH),
                40, 1L, constraints, null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> singleDummyService.analyze(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    private static HandGenerationParameters westFiveToSevenSpadesTenToTwelve() {
        EnumMap<Suit, SuitLengthRange> shape = new EnumMap<>(Suit.class);
        shape.put(Suit.SPADES, new SuitLengthRange(5, 7));
        return new HandGenerationParameters(10, 12, new HandDistribution(shape), null, null);
    }
}
