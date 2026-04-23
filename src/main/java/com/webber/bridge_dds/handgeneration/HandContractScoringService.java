package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.controller.SingleDummyAnalyzeRequest;
import com.webber.bridge_dds.controller.SingleDummyAnalyzeResponse;
import com.webber.bridge_dds.jna.struct.DDTableResults;
import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Deal;
import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.parser.DealParsers;
import com.webber.bridge_dds.service.DdsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class HandContractScoringService {

    private final DdsService ddsService;

    private static final int DDS_BATCH_LIMIT = 40;

    public HandContractScoringService(DdsService ddsService) {
        this.ddsService = ddsService;
    }

    public List<HandGenerationResponse.ContractScoreDto> scoreContracts(
            Hand westHand,
            Hand eastHand,
            List<ContractSuggestion> contractSuggestions,
            int samples,
            Long seed
    ) {
        List<SampledDeal> sampledDeals = generateSharedSamples(westHand, eastHand, samples, seed);

        List<ContractScoreResult> results = new ArrayList<>(contractSuggestions.size());
        for (ContractSuggestion contract : contractSuggestions) {
            int successes = evaluateContractOnBatch(contract, sampledDeals);
            int probability = (int) Math.round(successes * 100.0 / samples);
            results.add(new ContractScoreResult(contract, probability));
        }

        results.sort(Comparator.comparingInt(ContractScoreResult::successProbability).reversed());

        List<HandGenerationResponse.ContractScoreDto> output = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            ContractScoreResult r = results.get(i);
            output.add(new HandGenerationResponse.ContractScoreDto(
                    r.contract(),
                    r.successProbability(),
                    i + 1
            ));
        }

        return output;
    }


    private List<SampledDeal> generateSharedSamples(Hand westHand, Hand eastHand, int samples, Long seed) {
        long effectiveSeed = (seed != null) ? seed : System.nanoTime();
        Random random = new Random(effectiveSeed);

        List<Card> remainingCards = collectRemainingCards(westHand, eastHand);
        List<SampledDeal> out = new ArrayList<>(samples);

        for (int i = 0; i < samples; i++) {
            out.add(generateSingleSample(westHand, eastHand, remainingCards, random));
        }

        return out;
    }

    private List<Card> collectRemainingCards(Hand westHand, Hand eastHand) {
        List<Card> remaining = new ArrayList<>(26);
        for (Card card : Card.values()) {
            if (!westHand.contains(card) && !eastHand.contains(card)) {
                remaining.add(card);
            }
        }
        return remaining;
    }

    private SampledDeal generateSingleSample(Hand westHand, Hand eastHand, List<Card> remainingCards, Random random) {
        List<Card> shuffled = new ArrayList<>(remainingCards);
        java.util.Collections.shuffle(shuffled, random);

        Hand northHand = new Hand();
        Hand southHand = new Hand();

        for (int i = 0; i < 13; i++) {
            northHand.add(shuffled.get(i));
            southHand.add(shuffled.get(i + 13));
        }


        return new SampledDeal(northHand, eastHand, southHand, westHand);
    }

    private List<String> buildPbnBatch(List<SampledDeal> sampledDeals) {
        List<String> pbns = new ArrayList<>(sampledDeals.size());

        for (SampledDeal sample : sampledDeals) {
            Deal deal = new Deal();
            deal.setFirst(Player.NORTH); // arbitrary but consistent

            for (Card card : Card.values()) {
                if (sample.north().contains(card)) {
                    deal.give(Player.NORTH, card);
                } else if (sample.east().contains(card)) {
                    deal.give(Player.EAST, card);
                } else if (sample.south().contains(card)) {
                    deal.give(Player.SOUTH, card);
                } else if (sample.west().contains(card)) {
                    deal.give(Player.WEST, card);
                }
            }

            pbns.add(DealParsers.toPbn(deal));
        }

        return pbns;
    }


    private int evaluateContractOnBatch(ContractSuggestion contract, List<SampledDeal> sampledDeals) {
        int neededTricks = contract.level() + 6;
        int declarerIndex = 3; // WEST in DDS table ordering N,E,S,W
        int strainIndex = contract.denomination().ddsIndex();
        int executionCount = 0;
        int successes = 0;

        for (int start = 0; start < sampledDeals.size(); start += DDS_BATCH_LIMIT) {
            int end = Math.min(start + DDS_BATCH_LIMIT, sampledDeals.size());
            List<SampledDeal> chunk = sampledDeals.subList(start, end);
            List<String> pbns = buildPbnBatch(chunk);

            int[] trumpFilter = {1, 1, 1, 1, 1};
            trumpFilter[strainIndex] = 0;
            ++executionCount;
            long executionStart = System.currentTimeMillis();
            DdsService.DDSBatchResult raw = ddsService.calculateFromPbnBatch(pbns, 0, trumpFilter);
            if (raw.returnCode() != 1) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "DDS CalcAllTablesPBN failed with return code " + raw.returnCode()
                );
            }
            long executionEnd = System.currentTimeMillis();
            System.out.printf("DDS batch %d took %dms%n", executionCount, executionEnd - executionStart);
            for (int i = 0; i < pbns.size(); i++) {
                DDTableResults table = raw.results().results[i];
                int tricks = table.get(strainIndex, declarerIndex);
                if (tricks >= neededTricks) {
                    successes++;
                }
            }
        }

        return successes;
    }

    private SingleDummyAnalyzeRequest buildRequest(ContractSuggestion contract, SampledDeal sample) {
        return new SingleDummyAnalyzeRequest(
                Player.WEST,
                Player.EAST,
                new SingleDummyAnalyzeRequest.Contract(contract.level(), contract.denomination()),
                Map.of(
                        Player.WEST, sample.west().toCardCodes(),
                        Player.EAST, sample.east().toCardCodes()
                ),
                1,
                null
        );
    }

    private boolean contractMade(SingleDummyAnalyzeResponse response, int contractLevel) {
        int targetTricks = contractLevel + 6;
        return response.successes() > 0 && response.successes() >= targetTricks;
    }

    private record ContractScoreResult(ContractSuggestion contract, int successProbability) {
    }
}
