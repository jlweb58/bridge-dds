package com.webber.bridge_dds.controller;

import com.webber.bridge_dds.jna.struct.DDTableResults;
import com.webber.bridge_dds.service.DdsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
