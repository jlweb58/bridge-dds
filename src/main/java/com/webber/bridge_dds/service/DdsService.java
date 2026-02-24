package com.webber.bridge_dds.service;

import com.webber.bridge_dds.jna.struct.AllParResults;
import com.webber.bridge_dds.jna.DDS;
import com.webber.bridge_dds.jna.struct.DDTableDealsPBN;
import com.webber.bridge_dds.jna.struct.DDTablesRes;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
public class DdsService {

    private final DDS dds = DDS.INSTANCE;

    /**
     * Calculate double dummy tables for a single deal.
     *
     * @param dealCards An 80-byte array representing one deal (ddTableDealPBN.cards)
     * @param mode Calculation mode (usually 0)
     * @param trumpFilter Array of 5 ints (0 = any)
     * @return DDSResult object containing raw tables and par results
     */
    public DDSResult calculate(byte[] dealCards, int mode, int[] trumpFilter) {
        if (dealCards.length != 80) {
            throw new IllegalArgumentException("Deal cards must be exactly 80 bytes");
        }
        if (trumpFilter == null || trumpFilter.length != DDTableDealsPBN.DDS_STRAINS) {
            trumpFilter = new int[DDTableDealsPBN.DDS_STRAINS]; // default: no filter
        }

        // Prepare structs
        DDTableDealsPBN deals = new DDTableDealsPBN();
        deals.noOfTables = 1;
        // Copy deal cards into first deal
        System.arraycopy(dealCards, 0, deals.deals[0].cards, 0, 80);

        DDTablesRes results = new DDTablesRes();
        AllParResults pres = new AllParResults();

        // Write structs before passing to native code
        deals.write();
        results.write();
        pres.write();

        // Call DDS
        int ret = dds.CalcAllTablesPBN(deals, mode, trumpFilter, results, pres);

        // Read back results
        results.read();
        pres.read();

        return new DDSResult(ret, results, pres);
    }

    public DDSResult calculateFromPbn(String pbn, int mode, int[] trumpFilter) {
        if (trumpFilter == null || trumpFilter.length != DDTableDealsPBN.DDS_STRAINS) {
            trumpFilter = new int[DDTableDealsPBN.DDS_STRAINS];
        }

        DDTableDealsPBN deals = new DDTableDealsPBN();
        deals.noOfTables = 1;
        putPbn80(deals.deals[0].cards, pbn);

        DDTablesRes results = new DDTablesRes();
        AllParResults pres = new AllParResults();

        deals.write();
        results.write();
        pres.write();

        int ret = dds.CalcAllTablesPBN(deals, mode, trumpFilter, results, pres);

        results.read();
        pres.read();

        return new DDSResult(ret, results, pres);
    }

    private static void putPbn80(byte[] dest80, String pbn) {
        Arrays.fill(dest80, (byte) 0); // NUL padding is safest for C strings
        byte[] bytes = pbn.getBytes(StandardCharsets.US_ASCII);

        int n = Math.min(bytes.length, dest80.length - 1); // keep room for trailing '\0'
        System.arraycopy(bytes, 0, dest80, 0, n);
        dest80[n] = 0;
    }

    /**
     * Simple DTO to return results from DDS
     */
    public static class DDSResult {
        public final int returnCode;
        public final DDTablesRes results;
        public final AllParResults parResults;

        public DDSResult(int returnCode, DDTablesRes results, AllParResults parResults) {
            this.returnCode = returnCode;
            this.results = results;
            this.parResults = parResults;
        }
    }
}
