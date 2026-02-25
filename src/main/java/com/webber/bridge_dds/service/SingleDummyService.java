package com.webber.bridge_dds.service;

import com.webber.bridge_dds.controller.SingleDummyAnalyzeRequest;
import com.webber.bridge_dds.controller.SingleDummyAnalyzeResponse;
import com.webber.bridge_dds.jna.struct.DDTableResults;
import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Deal;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.parser.DealParsers;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@Service
public class SingleDummyService {

    private static final int DDS_BATCH_SIZE = 40;

    private final DdsService ddsService;

    /**
     * DDS may or may not be thread-safe depending on build/options.
     * Start conservatively with 1; raise once verified.
     */
    private final Semaphore ddsConcurrencyGate = new Semaphore(1);

    public SingleDummyService(DdsService ddsService) {
        this.ddsService = ddsService;
    }

    public SingleDummyAnalyzeResponse analyze(SingleDummyAnalyzeRequest req) {
        long start = System.currentTimeMillis();
        validate(req);

        int samples = req.samples();
        int neededTricks = req.contract().level() + 6;

        Player declarer = req.declarer();
        Player dummy = req.dummy();
        Player[] defenders = defendersOf(declarer, dummy);

        List<Card> declarerCards = parseCards(req.hands().get(declarer), "hands[" + declarer + "]");
        List<Card> dummyCards = parseCards(req.hands().get(dummy), "hands[" + dummy + "]");

        EnumSet<Card> known = EnumSet.noneOf(Card.class);
        known.addAll(declarerCards);
        known.addAll(dummyCards);

        if (known.size() != 26) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expected exactly 26 distinct cards in declarer+dummy; got " + known.size()
            );
        }

        List<Card> unknown = new ArrayList<>(52 - known.size());
        for (Card c : Card.values()) {
            if (!known.contains(c)) unknown.add(c);
        }

        long seed = (req.seed() != null) ? req.seed() : System.nanoTime();
        Random master = new Random(seed);

        int batches = (samples + DDS_BATCH_SIZE - 1) / DDS_BATCH_SIZE;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<BatchOutcome>> futures = new ArrayList<>(batches);

            for (int b = 0; b < batches; b++) {
                int batchStart = b * DDS_BATCH_SIZE;
                int batchCount = Math.min(DDS_BATCH_SIZE, samples - batchStart);

                long batchSeed;
                synchronized (master) {
                    batchSeed = master.nextLong();
                }

                Callable<BatchOutcome> task = () -> runBatch(
                        batchCount,
                        batchSeed,
                        unknown,
                        declarer,
                        dummy,
                        defenders[0],
                        defenders[1],
                        req.contract().denomination().ddsIndex(),
                        declarerToDdsHandIndex(declarer),
                        neededTricks,
                        declarerCards,
                        dummyCards
                );

                futures.add(executor.submit(task));
            }

            int successes = 0;
            Map<Integer, Integer> histogram = new HashMap<>();

            for (Future<BatchOutcome> f : futures) {
                BatchOutcome o = f.get();
                successes += o.successes();
                o.histogram().forEach((k, v) -> histogram.merge(k, v, Integer::sum));
            }

            double p = successes / (double) samples;
            SingleDummyAnalyzeResponse.ConfidenceInterval95 ci = wilson95(successes, samples);
            long end = System.currentTimeMillis();
            System.out.printf("Single dummy analysis took %dms%n", end - start);
            return new SingleDummyAnalyzeResponse(samples, successes, p, ci, histogram);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Single dummy analysis failed", ex);
        }
    }

    private BatchOutcome runBatch(
            int batchCount,
            long seed,
            List<Card> unknownDeck,
            Player declarer,
            Player dummy,
            Player def1,
            Player def2,
            int ddsStrainIndex,
            int ddsDeclarerIndex,
            int neededTricks,
            List<Card> declarerCards,
            List<Card> dummyCards
    ) throws InterruptedException {
        Random rng = new Random(seed);

        List<String> pbns = new ArrayList<>(batchCount);

        for (int i = 0; i < batchCount; i++) {
            List<Card> shuffled = new ArrayList<>(unknownDeck);
            java.util.Collections.shuffle(shuffled, rng);

            List<Card> def1Cards = shuffled.subList(0, 13);
            List<Card> def2Cards = shuffled.subList(13, 26);

            Deal deal = new Deal();
            deal.setFirst(Player.NORTH); // arbitrary; DDS just needs consistent seat assignments

            for (Card c : declarerCards) deal.give(declarer, c);
            for (Card c : dummyCards) deal.give(dummy, c);
            for (Card c : def1Cards) deal.give(def1, c);
            for (Card c : def2Cards) deal.give(def2, c);

            pbns.add(DealParsers.toPbn(deal));
        }

        ddsConcurrencyGate.acquire();
        try {
            int[] trumpFilter = new int[5];
            DdsService.DDSBatchResult raw = ddsService.calculateFromPbnBatch(pbns, 0, trumpFilter);
            if (raw.returnCode() != 1) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "DDS CalcAllTablesPBN failed with return code " + raw.returnCode()
                );
            }

            int successes = 0;
            Map<Integer, Integer> histogram = new HashMap<>();

            for (int i = 0; i < batchCount; i++) {
                DDTableResults table = raw.results().results[i];
                int tricks = table.get(ddsStrainIndex, ddsDeclarerIndex);
                histogram.merge(tricks, 1, Integer::sum);
                if (tricks >= neededTricks) successes++;
            }

            return new BatchOutcome(successes, histogram);
        } finally {
            ddsConcurrencyGate.release();
        }
    }


    private static void validate(SingleDummyAnalyzeRequest req) {
        if (req == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        if (req.declarer() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"declarer\" is required");
        if (req.dummy() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"dummy\" is required");
        if (req.declarer() == req.dummy()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"dummy\" must be partner of declarer (different seat)");
        }
        if (req.contract() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"contract\" is required");
        if (req.contract().denomination() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"contract.denomination\" is required");
        }
        if (req.contract().level() < 1 || req.contract().level() > 7) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"contract.level\" must be 1..7");
        }
        if (req.hands() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"hands\" is required");
        if (req.samples() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"samples\" must be > 0");
    }

    private static Player[] defendersOf(Player declarer, Player dummy) {
        List<Player> defs = new ArrayList<>(2);
        for (Player p : Player.values()) {
            if (p != declarer && p != dummy) defs.add(p);
        }
        return new Player[]{defs.get(0), defs.get(1)};
    }

    private static List<Card> parseCards(List<String> codes, String fieldName) {
        if (codes == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        List<Card> out = new ArrayList<>(codes.size());
        for (String code : codes) {
            try {
                out.add(Card.fromCode(code));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card in " + fieldName + ": " + code);
            }
        }
        return out;
    }

    /**
     * DDS hand index for DDTableResults is N=0,E=1,S=2,W=3 (matching your earlier table printing).
     */
    private static int declarerToDdsHandIndex(Player p) {
        return switch (p) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };
    }

    private static SingleDummyAnalyzeResponse.ConfidenceInterval95 wilson95(int successes, int n) {
        if (n <= 0) return new SingleDummyAnalyzeResponse.ConfidenceInterval95(0.0, 0.0);

        double z = 1.959963984540054; // 95%
        double phat = successes / (double) n;

        double denom = 1.0 + (z * z) / n;
        double center = (phat + (z * z) / (2.0 * n)) / denom;
        double margin = (z * Math.sqrt((phat * (1.0 - phat) + (z * z) / (4.0 * n)) / n)) / denom;

        double low = Math.max(0.0, center - margin);
        double high = Math.min(1.0, center + margin);
        return new SingleDummyAnalyzeResponse.ConfidenceInterval95(low, high);
    }

    private record BatchOutcome(int successes, Map<Integer, Integer> histogram) { }

}
