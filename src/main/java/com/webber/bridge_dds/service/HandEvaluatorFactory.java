package com.webber.bridge_dds.service;

public final class HandEvaluatorFactory {
    public static HandEvaluator fromType(HandEvaluatorType type) {
        return switch (type) {
            case STANDARD -> new StandardHandEvaluator();
            case KAPLAN_RUBENS -> new KaplanRubensHandEvaluator();
        };
    }

    private HandEvaluatorFactory() {
    }
}
