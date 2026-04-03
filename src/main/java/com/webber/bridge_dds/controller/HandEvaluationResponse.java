package com.webber.bridge_dds.controller;

/**
 * Represents the response from hand evaluation.
 * @param handValue The evaluated hand value as calculated by the Kaplan-Rubens algorithm
 */
public record HandEvaluationResponse(double handValue) {
}
