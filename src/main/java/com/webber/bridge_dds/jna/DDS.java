package com.webber.bridge_dds.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.webber.bridge_dds.jna.struct.AllParResults;
import com.webber.bridge_dds.jna.struct.DDTableDealsPBN;
import com.webber.bridge_dds.jna.struct.DDTablesRes;

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
