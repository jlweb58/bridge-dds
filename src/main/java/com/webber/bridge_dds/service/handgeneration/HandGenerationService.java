package com.webber.bridge_dds.service.handgeneration;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.service.HandEvaluator;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class HandGenerationService {

    private final HandEvaluator handEvaluator;

    public HandGenerationService(HandEvaluator handEvaluator) {
        this.handEvaluator = handEvaluator;
    }

    public Map<Player, List<Hand>> generateHand(Map<Player, HandGenerationParameters> parameters) {
        Map<Player, List<Hand>> hands = new EnumMap<>(Player.class);
        return hands;
    }



}
