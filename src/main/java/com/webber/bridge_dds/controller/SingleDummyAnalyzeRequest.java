package com.webber.bridge_dds.controller;

import com.webber.bridge_dds.handgeneration.HandGenerationParameters;
import com.webber.bridge_dds.model.Denomination;
import com.webber.bridge_dds.model.Player;

import java.util.List;
import java.util.Map;

/**
 * Single dummy analysis request.
 * <p>
 * The declarer hand must always be fully specified in {@code hands}. The dummy hand may be fully
 * specified in {@code hands} or described by {@code constraints}. The defenders' hands are always
 * dealt randomly from the remaining cards, optionally subject to {@code constraints}
 * (any defender entries in {@code hands} are ignored).
 *
 * @param constraints optional per-seat constraints (dummy and/or defenders)
 * @param evaluator   optional point-count evaluator used for constraints (see HandEvaluatorType); defaults to "standard"
 */
public record SingleDummyAnalyzeRequest(
        Player declarer,
        Player dummy,
        Contract contract,
        Map<Player, List<String>> hands, // values are "SA" strings
        int samples,
        Long seed,
        Map<Player, HandGenerationParameters> constraints,
        String evaluator
) {
    public SingleDummyAnalyzeRequest(
            Player declarer,
            Player dummy,
            Contract contract,
            Map<Player, List<String>> hands,
            int samples,
            Long seed
    ) {
        this(declarer, dummy, contract, hands, samples, seed, null, null);
    }

    public record Contract(int level, Denomination denomination) { }
}
