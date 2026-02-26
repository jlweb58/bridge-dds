package com.webber.bridge_dds.service;

import com.webber.bridge_dds.jna.struct.AllParResults;
import com.webber.bridge_dds.jna.DDS;
import com.webber.bridge_dds.jna.struct.DDSInfo;
import com.webber.bridge_dds.jna.struct.DDTableDealsPBN;
import com.webber.bridge_dds.jna.struct.DDTablesRes;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

@Service
public class DdsService {

    private final DDS dds = DDS.INSTANCE;

    /**
     * Serialize all calls into the native DDS library.
     * DDS may use OpenMP internally, but many builds are not safe for concurrent external calls.
     */
    private final Semaphore nativeGate = new Semaphore(1);


    public DdsService() {
        withNativeGate(() -> {
            System.out.println(dds.SetMaxThreads(0));
            DDSInfo info = new DDSInfo();
            info.write();
            int rc = dds.GetDDSInfo(info);
            info.read();
            System.out.println("GetDDSInfo rc=" + rc);

            System.out.println("DDS Info: " + info.toPrettyString());
        });
    }

    public DDSResult calculateFromPbn(String pbn, int mode, int[] trumpFilter) {
        if (trumpFilter == null || trumpFilter.length != DDTableDealsPBN.DDS_STRAINS) {
            trumpFilter = new int[DDTableDealsPBN.DDS_STRAINS];
        } else {
            trumpFilter = Arrays.copyOf(trumpFilter, trumpFilter.length); // defensive copy for concurrency
        }

        DDTableDealsPBN deals = new DDTableDealsPBN();
        deals.noOfTables = 1;
        putPbn80(deals.deals[0].cards, pbn);

        DDTablesRes results = new DDTablesRes();
        AllParResults pres = new AllParResults();

        deals.write();
        results.write();
        pres.write();

        final int[] finalTrumpFilter = trumpFilter;
        int ret = withNativeGate(() -> dds.CalcAllTablesPBN(deals, mode, finalTrumpFilter, results, pres));

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
        } else {
            trumpFilter = Arrays.copyOf(trumpFilter, trumpFilter.length); // defensive copy for concurrency
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

        final int[] finalTrumpFilter = trumpFilter;
        int ret = withNativeGate(() -> dds.CalcAllTablesPBN(deals, mode, finalTrumpFilter, results, pres));

        results.read();
        pres.read();

        return new DDSBatchResult(ret, results, pres);
    }

    public record DDSBatchResult(int returnCode, DDTablesRes results, AllParResults parResults) {
    }

    /**
     * Simple DTO to return results from DDS
     */
    public record DDSResult(int returnCode, DDTablesRes results, AllParResults parResults) {
    }


    private static void putPbn80(byte[] dest80, String pbn) {
        Arrays.fill(dest80, (byte) 0); // NUL padding is safest for C strings

        int n = Math.min(pbn.length(), dest80.length - 1); // keep room for trailing '\0'
        for (int i = 0; i < n; i++) {
            char c = pbn.charAt(i);
            // ASCII-only conversion; non-ASCII characters replaced with '?'
            dest80[i] = (byte) (c <= 127 ? c : '?');
        }
        dest80[n] = 0;
    }


    private void withNativeGate(Runnable r) {
        try {
            nativeGate.acquire();
            r.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting to call native DDS", e);
        } finally {
            nativeGate.release();
        }
    }

    private int withNativeGate(IntSupplier supplier) {
        try {
            nativeGate.acquire();
            return supplier.getAsInt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting to call native DDS", e);
        } finally {
            nativeGate.release();
        }
    }

    @FunctionalInterface
    private interface IntSupplier {
        int getAsInt();
    }
}
