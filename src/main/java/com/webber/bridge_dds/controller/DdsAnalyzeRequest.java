package com.webber.bridge_dds.controller;

import com.webber.bridge_dds.model.Player;

import java.util.List;
import java.util.Map;

/**
 * What the frontend posts.
 *
 * Example shape:
 * {
 *   "first": "NORTH",
 *   "hands": {
 *     "NORTH": ["SA","SK", ...],
 *     "EAST":  ["H2","D3", ...],
 *     "SOUTH": [...],
 *     "WEST":  [...]
 *   }
 * }
 */
public record DdsAnalyzeRequest(
        Player first,
        Map<Player, List<String>> hands
) { }
