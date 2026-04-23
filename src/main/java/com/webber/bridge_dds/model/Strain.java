package com.webber.bridge_dds.model;

import lombok.Getter;

@Getter
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

}
