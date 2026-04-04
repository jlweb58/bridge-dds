package com.webber.bridge_dds.service.handgeneration;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Rank;
import com.webber.bridge_dds.model.Suit;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record HandGenerationResponse(Map<Player, List<GeneratedHandDto>> hands) {
    public record GeneratedHandDto(Map<Suit, List<Rank>> cards) {
        public static GeneratedHandDto from(Hand hand) {
            Map<Suit, List<Rank>> cardsBySuit = new EnumMap<>(Suit.class);
            for (Suit suit : Suit.values()) {
                cardsBySuit.put(suit, List.copyOf(hand.ranksForSuit(suit)));
            }
            return new GeneratedHandDto(cardsBySuit);
        }
    }
}
