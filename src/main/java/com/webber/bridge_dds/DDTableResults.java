package com.webber.bridge_dds;

import com.sun.jna.Structure;

import java.util.List;

public class DDTableResults extends Structure {

    public static final int DDS_STRAINS = 5;
    public static final int DDS_HANDS = 4;

    // Flattened 2D array
    public int[] resTable = new int[DDS_STRAINS * DDS_HANDS];

    @Override
    protected List<String> getFieldOrder() {
        return List.of("resTable");
    }

    public int get(int strain, int hand) {
        return resTable[strain * DDS_HANDS + hand];
    }

}
