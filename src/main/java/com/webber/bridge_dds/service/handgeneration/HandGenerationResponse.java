package com.webber.bridge_dds.service.handgeneration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Vulnerability;

import java.util.List;

public record HandGenerationResponse(List<GeneratedHandDto> hands) {

    public record GeneratedHandDto(
            @JsonProperty("dealer") Player dealer,
            @JsonProperty("vulnerability") Vulnerability vulnerability,
            @JsonProperty("WEST") List<String> west,
            @JsonProperty("EAST") List<String> east) {
    }
}
