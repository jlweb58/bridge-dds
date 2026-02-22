package com.webber.bridge_dds;

import com.sun.jna.Structure;

import java.util.List;

public class DDTableDealsPBN extends Structure {

    public static final int DDS_STRAINS = 5;
    public static final int MAXNOOFTABLES = 40;
    public static final int DEALS_SIZE = MAXNOOFTABLES * DDS_STRAINS; // 200

    public int noOfTables;
    public DDTableDealPBN[] deals = new DDTableDealPBN[DEALS_SIZE];

    public DDTableDealsPBN() {
        for (int i = 0; i < deals.length; i++) {
            deals[i] = new DDTableDealPBN();
        }
    }

    @Override
    protected List<String> getFieldOrder() {
        return List.of("noOfTables", "deals");
    }
}
