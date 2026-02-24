package com.webber.bridge_dds.controller;

import com.webber.bridge_dds.model.Denomination;
import com.webber.bridge_dds.model.Player;

import java.util.Map;

public record DdsAnalyzeResponse(
        String pbn,
        int ddsReturnCode,
        Map<Denomination, Map<Player, Integer>> tricks,
        ParView par
) {
    public record ParView(
            String nsScore,
            String ewScore,
            String nsContracts,
            String ewContracts
    ) { }
}
