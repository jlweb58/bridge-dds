package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Rank;

import java.util.Set;

public record SuitQualityRequirement(
        Set<Rank> minimumRanks
) {
}
