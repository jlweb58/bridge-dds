package com.webber.bridge_dds.controller;

import com.webber.bridge_dds.model.Denomination;
import com.webber.bridge_dds.model.Player;

import java.util.List;
import java.util.Map;

public record SingleDummyAnalyzeRequest(
        Player declarer,
        Player dummy,
        Contract contract,
        Map<Player, List<String>> hands, // only declarer + dummy required; values are "SA" strings
        int samples,
        Long seed
) {
    public record Contract(int level, Denomination denomination) { }
}
