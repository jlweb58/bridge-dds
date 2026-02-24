package com.webber.bridge_dds.model;

public enum Strain {
    SPADES("S"),
    HEARTS("H"),
    DIAMONDS("D"),
    CLUBS("C"),
    NOTRUMP("NT");

    private final String symbol;

    Strain(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
