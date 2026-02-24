package com.webber.bridge_dds.model;

public enum Player {
    NORTH('N'),
    EAST('E'),
    SOUTH('S'),
    WEST('W');

    private final char pbn;

    Player(char pbn) {
        this.pbn = pbn;
    }

    public char toPbnChar() {
        return pbn;
    }
}
