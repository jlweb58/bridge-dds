package com.webber.bridge_dds.service;

public enum HandEvaluatorType {

    STANDARD("standard"),
    KAPLAN_RUBENS("kaplan-rubens"),
    BERGEN("bergen");

    private final String identifier;

    HandEvaluatorType(String identifier) {
        this.identifier = identifier;
    }

    public String identifier() {
        return identifier;
    }

    public static HandEvaluatorType fromId(String id) {
        for (HandEvaluatorType type : values()) {
            if (type.identifier.equalsIgnoreCase(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown hand evaluator: " + id);
    }
}
