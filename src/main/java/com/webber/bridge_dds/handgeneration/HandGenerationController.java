package com.webber.bridge_dds.handgeneration;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:4201", "https://bridge.johnwebber.de"},
        allowedHeaders = "*",
        methods = {org.springframework.web.bind.annotation.RequestMethod.POST,
                org.springframework.web.bind.annotation.RequestMethod.OPTIONS}
)
public class HandGenerationController {

    private final HandGenerationService handGenerationService;

    public HandGenerationController(HandGenerationService handGenerationService) {
        this.handGenerationService = handGenerationService;
    }

    @PostMapping("/dds/hand-generation")
    public HandGenerationResponse generateHands(@RequestBody HandGenerationRequest request) {
        return handGenerationService.generateHands(request);
    }

}
