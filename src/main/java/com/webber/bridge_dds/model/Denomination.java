package com.webber.bridge_dds.model;

public enum Denomination {
    SPADES(0),
    HEARTS(1),
    DIAMONDS(2),
    CLUBS(3),
    NOTRUMP(4);

    private final int ddsIndex;

    Denomination(int ddsIndex) {
        this.ddsIndex = ddsIndex;
    }

    public int ddsIndex() {
        return ddsIndex;
    }
}
