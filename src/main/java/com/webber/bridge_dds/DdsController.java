package com.webber.bridge_dds;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;

@RestController
public class DdsController {

    private final DdsService ddsService;

    public DdsController(DdsService ddsService) {
        this.ddsService = ddsService;
    }

    @GetMapping("/test-ds")
    public String testDds() {
        // Example: fill a deal with dummy data (all 'A's for simplicity)
        byte[] dealCards = new byte[80];
        Arrays.fill(dealCards, (byte)'A');

        int[] trumpFilter = new int[5]; // 0 = any

        DdsService.DDSResult result = ddsService.calculate(dealCards, 0, trumpFilter);

        return "DDS returned code: " + result.returnCode
                + ", first table res[0][0]: " + result.results.results[0].get(0, 0);
    }
}
