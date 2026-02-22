package com.webber.bridge_dds;

import com.sun.jna.Structure;

import java.util.List;

public class DDTablesRes extends Structure {

    public static final int DDS_STRAINS = 5;
    public static final int MAXNOOFTABLES = 40;
    public static final int RESULTS_SIZE = MAXNOOFTABLES * DDS_STRAINS; // 200

    public int noOfBoards = 0;
    public DDTableResults[] results =
            (DDTableResults[]) new DDTableResults().toArray(RESULTS_SIZE);

    @Override
    protected List<String> getFieldOrder() {
        return List.of("noOfBoards", "results");
    }
}
