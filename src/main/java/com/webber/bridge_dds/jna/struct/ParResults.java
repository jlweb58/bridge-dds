package com.webber.bridge_dds.jna.struct;

import com.sun.jna.Structure;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ParResults extends Structure {

    public static final int VIEWS = 2;
    public static final int PAR_SCORE_LEN = 16;
    public static final int PAR_CONTRACTS_LEN = 128;

    public byte[] parScore = new byte[VIEWS * PAR_SCORE_LEN];
    public byte[] parContractsString = new byte[VIEWS * PAR_CONTRACTS_LEN];

    @Override
    protected List<String> getFieldOrder() {
        return List.of("parScore", "parContractsString");
    }

    public String getParScore(int view) {
        int off = view * PAR_SCORE_LEN;
        int len = 0;
        while (len < PAR_SCORE_LEN && parScore[off + len] != 0) len++;
        return new String(parScore, off, len, StandardCharsets.US_ASCII);
    }

    public String getParContractsString(int view) {
        int off = view * PAR_CONTRACTS_LEN;
        int len = 0;
        while (len < PAR_CONTRACTS_LEN && parContractsString[off + len] != 0) len++;
        return new String(parContractsString, off, len, StandardCharsets.US_ASCII);
    }
}
