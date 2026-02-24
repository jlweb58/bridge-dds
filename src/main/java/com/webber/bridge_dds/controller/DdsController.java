package com.webber.bridge_dds.controller;

import com.webber.bridge_dds.jna.struct.DDTableResults;
import com.webber.bridge_dds.jna.struct.ParResults;
import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Deal;
import com.webber.bridge_dds.model.Denomination;
import com.webber.bridge_dds.model.DoubleDummyResult;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.parser.DealParsers;
import com.webber.bridge_dds.service.DdsResultConverter;
import com.webber.bridge_dds.service.DdsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
public class DdsController {

    private static final String[] PBN = {
            "N:QJ6.K652.J85.T98 873.J97.AT764.Q4 K5.T83.KQ9.A7652 AT942.AQ4.32.KJ3",
            "E:QJT5432.T.6.QJ82 .J97543.K7532.94 87.A62.QJT4.AT75 AK96.KQ8.A98.K63",
            "N:73.QJT.AQ54.T752 QT6.876.KJ9.AQ84 5.A95432.7632.K6 AKJ9842.K.T8.J93"
    };
    private final DdsService ddsService;

    public DdsController(DdsService ddsService) {
        this.ddsService = ddsService;
    }

    /**
     * Analyze a bridge deal for double-dummy results.
     * <p>
     * This endpoint accepts a Deal JSON object representing the four hands,
     * calculates the double-dummy analysis using the DDS library, and returns
     * the number of tricks each player can make as declarer in each strain.
     *
     * @param request The bridge deal to analyze
     * @return DoubleDummyResult containing tricks for each player/strain combination
     */
    @PostMapping("/dds/analyze")
    public DdsAnalyzeResponse analyze(@RequestBody DdsAnalyzeRequest request) {
        Deal deal = toDeal(request);
        String pbn = DealParsers.toPbn(deal);

        int[] trumpFilter = new int[5]; // 0 = any
        DdsService.DDSResult raw = ddsService.calculateFromPbn(pbn, 0, trumpFilter);

        if (raw.returnCode != 1) {
            // DDS uses a set of return codes; treat anything non-success as server error for now.
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DDS CalcAllTablesPBN failed with return code " + raw.returnCode
            );
        }

        DDTableResults table = raw.results.results[0];
        Map<Denomination, Map<Player, Integer>> tricks = toTricksMap(table);

        ParResults par0 = raw.parResults.presults[0];
        DdsAnalyzeResponse.ParView par = new DdsAnalyzeResponse.ParView(
                par0.getParScore(0),
                par0.getParScore(1),
                par0.getParContractsString(0),
                par0.getParContractsString(1)
        );

        return new DdsAnalyzeResponse(pbn, raw.returnCode, tricks, par);
    }
    @GetMapping("/test-ds")
    public String testDds(int hand) {

        if (hand < 0 || hand >= PBN.length) {
            throw new IllegalArgumentException("hand must be 0.." + (PBN.length - 1));
        }



        int[] trumpFilter = new int[5]; // 0 = any

        DdsService.DDSResult result = ddsService.calculateFromPbn(PBN[hand], 0, trumpFilter);

        String[] denom = {"S", "H", "D", "C", "NT"};
        String[] decl = {"N", "E", "S", "W"};

        System.out.println("noOfBoards=" + result.results.noOfBoards);

        DDTableResults table = result.results.results[0];
        for (int d = 0; d < 5; d++) {
            for (int p = 0; p < 4; p++) {
                System.out.println("denom=" + denom[d] + " declarer=" + decl[p] + " tricks=" + table.get(d, p));
            }
        }
        return "DDS returned code: " + result.returnCode
                + ", first table res[0][0]: " + result.results.results[0].get(0, 0);
    }

    private static Deal toDeal(DdsAnalyzeRequest request) {
        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        if (request.first() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"first\" is required");
        if (request.hands() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\"hands\" is required");

        Deal deal = new Deal();
        deal.setFirst(request.first());

        for (Player p : Player.values()) {
            List<String> cards = request.hands().get(p);
            if (cards == null) continue;
            for (String code : cards) {
                try {
                    deal.give(p, Card.fromCode(code));
                } catch (IllegalArgumentException ex) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Invalid card for " + p + ": " + code + " (" + ex.getMessage() + ")"
                    );
                }7
            }
        }

        return deal;
    }

    private static Map<Denomination, Map<Player, Integer>> toTricksMap(DDTableResults table) {
        Map<Denomination, Map<Player, Integer>> out = new EnumMap<>(Denomination.class);

        for (Denomination d : Denomination.values()) {
            Map<Player, Integer> byDeclarer = new EnumMap<>(Player.class);

            // DDS declarer index order for table results is N,E,S,W (matches your existing test printing)
            byDeclarer.put(Player.NORTH, table.get(d.ddsIndex(), 0));
            byDeclarer.put(Player.EAST, table.get(d.ddsIndex(), 1));
            byDeclarer.put(Player.SOUTH, table.get(d.ddsIndex(), 2));
            byDeclarer.put(Player.WEST, table.get(d.ddsIndex(), 3));

            out.put(d, byDeclarer);
        }

        return out;
    }

}
