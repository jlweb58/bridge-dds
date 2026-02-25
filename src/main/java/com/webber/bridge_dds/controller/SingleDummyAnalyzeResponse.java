package com.webber.bridge_dds.controller;

import java.util.Map;

public record SingleDummyAnalyzeResponse(
        int samples,
        int successes,
        double successProbability,
        ConfidenceInterval95 confidence95,
        Map<Integer, Integer> tricksHistogram
) {
    public record ConfidenceInterval95(double low, double high) { }
}
