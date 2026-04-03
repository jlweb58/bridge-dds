package com.webber.bridge_dds.service;

import com.webber.bridge_dds.model.Hand;

public interface HandEvaluator {

    /**
     * Evaluates a hand according to a specific strategy.
     * @param hand The hand to evaluate
     * @return The evaluation score, generally between 0.0 and 40.0 (could theoretically be negative)
     */
    double evaluate(Hand hand);
}
