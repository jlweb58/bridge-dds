package com.webber.bridge_dds.service.handgeneration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Rank;
import com.webber.bridge_dds.model.Suit;

import java.util.List;
import java.util.Map;

public record HandGenerationResponse(List<GeneratedHandDto> hands) {

    public record GeneratedHandDto(@JsonProperty("WEST") List<String> west, @JsonProperty("EAST") List<String> east) {
    }
}
