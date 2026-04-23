package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Vulnerability;
import com.webber.bridge_dds.service.HandEvaluatorFactory;
import com.webber.bridge_dds.service.HandEvaluatorType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:4201", "https://bridge.johnwebber.de"},
        allowedHeaders = "*",
        methods = { org.springframework.web.bind.annotation.RequestMethod.POST,
                org.springframework.web.bind.annotation.RequestMethod.OPTIONS }
)
public class HandGenerationController {

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


    @PostMapping("/dds/hand-generation")
    public HandGenerationResponse generateHands(@RequestBody HandGenerationRequest request) {
        HandEvaluatorType handEvaluatorType = request.evaluator() == null ? HandEvaluatorType.STANDARD : HandEvaluatorType.fromId(request.evaluator());
        HandGenerationService handGenerationService = new HandGenerationService(HandEvaluatorFactory.fromType(handEvaluatorType));
        Map<Player, List<Hand>> hands = handGenerationService.generateHands(request);
        List<HandGenerationResponse.GeneratedHandDto> responseHands = new java.util.ArrayList<>();

        int handCount = hands.getOrDefault(Player.WEST, List.of()).size();
        for (int i = 0; i < handCount; i++) {
            int boardNumber = i + 1;

            responseHands.add(new HandGenerationResponse.GeneratedHandDto(
                    dealerForBoard(boardNumber),
                    vulnerabilityForBoard(boardNumber),
                    toPbnCards(hands.get(Player.WEST).get(i)),
                    toPbnCards(hands.get(Player.EAST).get(i)),
                    Collections.emptyList()
            ));
        }
        return new HandGenerationResponse(responseHands);
    }

    private static Player dealerForBoard(int boardNumber) {
        return DEALER_CYCLE[(boardNumber - 1) % 4];
    }

    private static Vulnerability vulnerabilityForBoard(int boardNumber) {
        return VULNERABILITY_CYCLE[(boardNumber - 1) % 16];
    }

    private static List<String> toPbnCards(Hand hand) {
        List<String> cards = new java.util.ArrayList<>(13);
        for (com.webber.bridge_dds.model.Suit suit : com.webber.bridge_dds.model.Suit.values()) {
            for (com.webber.bridge_dds.model.Rank rank : hand.ranksForSuit(suit)) {
                cards.add("" + suit.toPbnChar() + rank.toPbnChar());
            }
        }
        return cards;
    }
}
