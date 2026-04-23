package com.webber.bridge_dds.service;

import org.springframework.stereotype.Component;

@Component
public final class HandEvaluatorFactory {
    public HandEvaluator fromType(HandEvaluatorType type) {
        return switch (type) {
            case STANDARD -> new StandardHandEvaluator();
            case KAPLAN_RUBENS -> new KaplanRubensHandEvaluator();
            case BERGEN -> new BergenHandEvaluator();
        };
    }

}
