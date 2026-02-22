package com.webber.bridge_dds;

import com.sun.jna.Structure;

import java.util.List;

public class DDTableDealPBN extends Structure {

    public static final int CARDS_LEN = 80;
    public byte[] cards = new byte[CARDS_LEN];

    @Override
    protected List<String> getFieldOrder() {
        return List.of("cards");
    }
}
