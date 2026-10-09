package com.webber.bridge_dds.service;

import com.webber.bridge_dds.controller.SingleDummyAnalyzeRequest;
import com.webber.bridge_dds.controller.SingleDummyAnalyzeResponse;
import com.webber.bridge_dds.handgeneration.HandGenerationParameters;
import com.webber.bridge_dds.handgeneration.HandParametersMatcher;
import com.webber.bridge_dds.jna.struct.DDTableResults;
import com.webber.bridge_dds.jna.struct.DDTableDealsPBN;
import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Deal;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.parser.DealParsers;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class SingleDummyService {

    /**
     * Logs every analysed sample at DEBUG. Switch off with
     * {@code logging.level.com.webber.bridge_dds.service.SingleDummyService.samples=INFO}.
     */
    private static final Logger SAMPLE_LOG = LoggerFactory.getLogger(SingleDummyService.class.getName() + ".samples");

    private static final int DDS_BATCH_SIZE = DDTableDealsPBN.MAXNOOFTABLES;

    private static final int MAX_SAMPLES = 2000;

    /**
     * Rejection sampling budget: a batch of n samples may try at most n * MAX_ATTEMPTS_PER_SAMPLE deals
     * (roughly 1M attempts per second per thread). This bounds the work for very restrictive constraints.
     */
    private static final int MAX_ATTEMPTS_PER_SAMPLE = 10_000;

    /**
     * All batches give up once this many attempts (summed over the batches) have not produced a single
     * matching deal, so that impossible constraints fail fast.
     */
    private static final long NO_MATCH_ABORT_ATTEMPTS = 1_000_000;

    /** Attempts per sampler call between checks of the shared progress. */
    private static final long ATTEMPT_CHUNK = 10_000;

    private final DdsService ddsService;

    private final HandEvaluatorFactory handEvaluatorFactory;

    private final HandParametersMatcher handParametersMatcher;

    public SingleDummyService(DdsService ddsService, HandEvaluatorFactory handEvaluatorFactory, HandParametersMatcher handParametersMatcher) {
        this.ddsService = ddsService;
        this.handEvaluatorFactory = handEvaluatorFactory;
        this.handParametersMatcher = handParametersMatcher;
    }

    public SingleDummyAnalyzeResponse analyze(SingleDummyAnalyzeRequest req) {
        long start = System.currentTimeMillis();
        validate(req);

        // protect the server from attack via a high number of samples
        int requestedSamples = Math.min(req.samples(), MAX_SAMPLES);
        int neededTricks = req.contract().level() + 6;

        Player declarer = req.declarer();
        Player dummy = req.dummy();
        Map<Player, HandGenerationParameters> constraints =
                req.constraints() == null ? Map.of() : req.constraints();
        HandEvaluatorType evaluatorType = parseEvaluator(req.evaluator());

        Map<Player, List<Card>> knownHands = new EnumMap<>(Player.class);
        knownHands.put(declarer, parseHand(req.hands().get(declarer), "hands[" + declarer + "]"));

        List<String> dummyCodes = req.hands().get(dummy);
        boolean dummyKnown = dummyCodes != null && !dummyCodes.isEmpty();
        if (dummyKnown) {
            if (constraints.containsKey(dummy)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Specify either hands[" + dummy + "] or constraints[" + dummy + "], not both");
            }
            knownHands.put(dummy, parseHand(dummyCodes, "hands[" + dummy + "]"));
        } else if (!constraints.containsKey(dummy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "hands[" + dummy + "] or constraints[" + dummy + "] is required");
        }

        EnumSet<Card> known = EnumSet.noneOf(Card.class);
        knownHands.values().forEach(known::addAll);
        if (known.size() != 13 * knownHands.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The specified hands contain duplicate cards");
        }

        int ddsStrainIndex = req.contract().denomination().ddsIndex();
        int ddsDeclarerIndex = declarerToDdsHandIndex(declarer);

        int[] trumpFilter = {1, 1, 1, 1, 1};
        trumpFilter[ddsStrainIndex] = 0; // compute only this denomination

        long seed = (req.seed() != null) ? req.seed() : System.nanoTime();
        Random master = new Random(seed);

        int batches = (requestedSamples + DDS_BATCH_SIZE - 1) / DDS_BATCH_SIZE;
        SamplingProgress progress = new SamplingProgress(new AtomicLong(), new AtomicLong());

        // batches run concurrently, so sample lines are interleaved; sort by sample number if needed
        SAMPLE_LOG.debug("Samples for {}{} by {} (needs {} tricks), seed {}: sample, South, West, North, East, tricks",
                req.contract().level(), req.contract().denomination(), declarer, neededTricks, seed);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<BatchOutcome>> futures = new ArrayList<>(batches);

            for (int b = 0; b < batches; b++) {
                int batchStart = b * DDS_BATCH_SIZE;
                int batchCount = Math.min(DDS_BATCH_SIZE, requestedSamples - batchStart);

                long batchSeed = master.nextLong();

                Callable<BatchOutcome> task = () -> runBatch(
                        batchStart,
                        batchCount,
                        batchSeed,
                        knownHands,
                        constraints,
                        evaluatorType,
                        progress,
                        ddsStrainIndex,
                        ddsDeclarerIndex,
                        neededTricks,
                        trumpFilter
                );

                futures.add(executor.submit(task));
            }

            int samples = 0;
            int successes = 0;
            long attempts = 0;
            int[] combinedHistogram = new int[14];

            for (Future<BatchOutcome> f : futures) {
                BatchOutcome o = f.get();
                samples += o.samples();
                successes += o.successes();
                attempts += o.attempts();
                int[] h = o.histogram();
                for (int i = 0; i < h.length; i++) {
                    combinedHistogram[i] += h[i];
                }
            }

            if (samples == 0) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT,
                        "No deals matching the constraints could be generated after " + attempts
                                + " attempts. The constraints may be too restrictive or incompatible with the known hands."
                );
            }

            Map<Integer, Integer> histogram = new HashMap<>();
            for (int i = 0; i < combinedHistogram.length; i++) {
                if (combinedHistogram[i] != 0) histogram.put(i, combinedHistogram[i]);
            }

            double p = successes / (double) samples;
            SingleDummyAnalyzeResponse.ConfidenceInterval95 ci = wilson95(successes, samples);
            long end = System.currentTimeMillis();
            log.info("Single dummy analysis took {}ms: {} of {} requested samples, {} deal attempts",
                    end - start, samples, requestedSamples, attempts);
            return new SingleDummyAnalyzeResponse(samples, successes, p, ci, histogram);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (java.util.concurrent.ExecutionException ex) {
            if (ex.getCause() instanceof ResponseStatusException rse) throw rse;
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Single dummy analysis failed", ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Single dummy analysis failed", ex);
        }
    }

    private BatchOutcome runBatch(
            int batchStart,
            int batchCount,
            long seed,
            Map<Player, List<Card>> knownHands,
            Map<Player, HandGenerationParameters> constraints,
            HandEvaluatorType evaluatorType,
            SamplingProgress progress,
            int ddsStrainIndex,
            int ddsDeclarerIndex,
            int neededTricks,
            int[] trumpFilter
    ) {
        Random rng = new Random(seed);
        ConstrainedDealSampler sampler = new ConstrainedDealSampler(
                knownHands, constraints, handParametersMatcher, handEvaluatorFactory.fromType(evaluatorType));

        long attemptsPerSample = sampler.hasConstraints() ? MAX_ATTEMPTS_PER_SAMPLE : 1;
        long maxAttempts = batchCount * attemptsPerSample;

        List<Deal> deals = new ArrayList<>(batchCount);
        List<String> pbns = new ArrayList<>(batchCount);
        while (pbns.size() < batchCount && sampler.attempts() < maxAttempts) {
            if (progress.matches().get() == 0 && progress.attempts().get() >= NO_MATCH_ABORT_ATTEMPTS) break;

            long before = sampler.attempts();
            Deal deal = sampler.sample(rng, Math.min(ATTEMPT_CHUNK, maxAttempts - before));
            progress.attempts().addAndGet(sampler.attempts() - before);
            if (deal != null) {
                progress.matches().incrementAndGet();
                deals.add(deal);
                pbns.add(DealParsers.toPbn(deal));
            }
        }
        long attempts = sampler.attempts();

        int successes = 0;
        int[] histogram = new int[14];

        if (pbns.isEmpty()) {
            return new BatchOutcome(0, 0, attempts, histogram);
        }

        DdsService.DDSBatchResult raw = ddsService.calculateFromPbnBatch(pbns, 0, trumpFilter);
        if (raw.returnCode() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DDS CalcAllTablesPBN failed with return code " + raw.returnCode()
            );
        }

        for (int i = 0; i < pbns.size(); i++) {
            DDTableResults table = raw.results().results[i];
            int tricks = table.get(ddsStrainIndex, ddsDeclarerIndex);
            if (tricks < 0) tricks = 0; // defensive, but DDS should return 0..13
            if (tricks > 13) tricks = 13;
            histogram[tricks]++;
            if (tricks >= neededTricks) successes++;
            logSample(batchStart + i + 1, deals.get(i), tricks);
        }

        return new BatchOutcome(pbns.size(), successes, attempts, histogram);
    }


    private static void logSample(int sampleNumber, Deal deal, int tricks) {
        if (!SAMPLE_LOG.isDebugEnabled()) return;
        SAMPLE_LOG.debug("Sample {}, S {}, W {}, N {}, E {}, tricks {}",
                sampleNumber,
                DealParsers.handToPbn(deal.hand(Player.SOUTH)),
                DealParsers.handToPbn(deal.hand(Player.WEST)),
                DealParsers.handToPbn(deal.hand(Player.NORTH)),
                DealParsers.handToPbn(deal.hand(Player.EAST)),
                tricks);
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
        if (req.constraints() != null) {
            if (req.constraints().containsKey(req.declarer())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "constraints[" + req.declarer() + "] not allowed: the declarer hand must be fully specified");
            }
            req.constraints().forEach(SingleDummyService::validateConstraint);
        }
    }

    private static void validateConstraint(Player player, HandGenerationParameters c) {
        String field = "constraints[" + player + "]";
        if (c == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " must not be null");
        if (c.minPoints() != null && c.minPoints() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + ".minPoints must be >= 0");
        }
        if (c.minPoints() != null && c.maxPoints() != null && c.maxPoints() < c.minPoints()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + ".maxPoints must be >= minPoints");
        }
        if (c.handDistribution() != null && c.handDistribution().suitLengths() != null) {
            c.handDistribution().suitLengths().forEach((suit, range) -> {
                if (range == null || range.min() < 0 || range.max() > 13 || range.min() > range.max()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            field + ".handDistribution[" + suit + "] must satisfy 0 <= min <= max <= 13");
                }
            });
        }
    }

    private static HandEvaluatorType parseEvaluator(String evaluator) {
        if (evaluator == null) return HandEvaluatorType.STANDARD;
        try {
            return HandEvaluatorType.fromId(evaluator);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private static List<Card> parseHand(List<String> codes, String fieldName) {
        List<Card> cards = parseCards(codes, fieldName);
        if (cards.size() != 13 || EnumSet.copyOf(cards).size() != 13) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    fieldName + " must contain exactly 13 distinct cards; got " + cards.size());
        }
        return cards;
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

    /** Shared between all batches of one analysis. */
    private record SamplingProgress(AtomicLong attempts, AtomicLong matches) { }

    private record BatchOutcome(int samples, int successes, long attempts, int[] histogram) { }

}
