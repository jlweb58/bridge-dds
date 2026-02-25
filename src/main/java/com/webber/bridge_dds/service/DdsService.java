package com.webber.bridge_dds.service;

import com.webber.bridge_dds.jna.struct.AllParResults;
import com.webber.bridge_dds.jna.DDS;
import com.webber.bridge_dds.jna.struct.DDTableDealsPBN;
import com.webber.bridge_dds.jna.struct.DDTablesRes;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class DdsService {

    private final DDS dds = DDS.INSTANCE;


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

    public DDSBatchResult calculateFromPbnBatch(List<String> pbns, int mode, int[] trumpFilter) {
        if (pbns == null || pbns.isEmpty()) {
            throw new IllegalArgumentException("pbns must be non-empty");
        }
        if (pbns.size() > DDTableDealsPBN.MAXNOOFTABLES) {
            throw new IllegalArgumentException("pbns size must be <= " + DDTableDealsPBN.MAXNOOFTABLES);
        }
        if (trumpFilter == null || trumpFilter.length != DDTableDealsPBN.DDS_STRAINS) {
            trumpFilter = new int[DDTableDealsPBN.DDS_STRAINS];
        }

        DDTableDealsPBN deals = new DDTableDealsPBN();
        deals.noOfTables = pbns.size();

        for (int i = 0; i < pbns.size(); i++) {
            String pbn = pbns.get(i);
            putPbn80(deals.deals[i].cards, pbn);
        }

        DDTablesRes results = new DDTablesRes();
        AllParResults pres = new AllParResults();

        deals.write();
        results.write();
        pres.write();

        int ret = dds.CalcAllTablesPBN(deals, mode, trumpFilter, results, pres);

        results.read();
        pres.read();

        return new DDSBatchResult(ret, results, pres);
    }

    public record DDSBatchResult(int returnCode, DDTablesRes results, AllParResults parResults) {
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
        public record DDSResult(int returnCode, DDTablesRes results, AllParResults parResults) {
    }
}
