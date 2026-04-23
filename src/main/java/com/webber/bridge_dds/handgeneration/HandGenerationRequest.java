package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.service.HandEvaluatorType;

import java.util.List;
import java.util.Map;

public record HandGenerationRequest(
        Map<Player, HandGenerationParameters> parameters,
        int numberOfHands,
        String evaluator,
        List<ContractSuggestion> contractSuggestions) {
    public HandGenerationRequest {
        assert parameters.containsKey(Player.WEST);
        assert parameters.containsKey(Player.EAST);
        assert parameters.size() == 2;
        assert numberOfHands > 0;
        if (evaluator == null) evaluator = HandEvaluatorType.STANDARD.identifier();
        assert contractSuggestions == null || contractSuggestions.size() <= 4;
    }
}
