package com.webber.bridge_dds;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface DDS extends Library {

    DDS INSTANCE = Native.load("dds", DDS.class);

    int CalcAllTablesPBN(
            DDTableDealsPBN deals,
            int mode,
            int[] trumpFilter,
            DDTablesRes results,
            AllParResults pres
    );

}
