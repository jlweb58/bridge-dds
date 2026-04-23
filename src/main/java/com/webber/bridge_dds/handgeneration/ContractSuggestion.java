package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Strain;

public record ContractSuggestion(
    int level,
    Strain strain
) {
}
