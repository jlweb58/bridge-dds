package com.webber.bridge_dds;

import org.springframework.stereotype.Service;
import com.sun.jna.Native;
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
