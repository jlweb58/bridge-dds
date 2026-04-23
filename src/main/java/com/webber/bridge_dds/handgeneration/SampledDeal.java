package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Hand;

public record SampledDeal(
        Hand north,
        Hand east,
        Hand south,
        Hand west
) {
    public SampledDeal {
    }
}
