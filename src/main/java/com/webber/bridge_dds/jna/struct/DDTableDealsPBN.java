package com.webber.bridge_dds.jna.struct;

import com.sun.jna.Structure;

import java.util.List;

public class DDTableDealsPBN extends Structure {

    public static final int DDS_STRAINS = 5;
    public static final int MAXNOOFTABLES = 40;
    public static final int DEALS_SIZE = MAXNOOFTABLES * DDS_STRAINS; // 200

    public int noOfTables;
    public DDTableDealPBN[] deals = (DDTableDealPBN[]) new DDTableDealPBN().toArray(DEALS_SIZE);

    public DDTableDealsPBN() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return List.of("noOfTables", "deals");
    }
}
