package com.webber.bridge_dds.jna.struct;

import com.sun.jna.Structure;

import java.util.List;

public class AllParResults extends Structure {

    public static final int MAXNOOFTABLES = 40;
    public ParResults[] presults = (ParResults[]) new ParResults().toArray(MAXNOOFTABLES);


    @Override
    protected List<String> getFieldOrder() {
        return List.of("presults");
    }
}
