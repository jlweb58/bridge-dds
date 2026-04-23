package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Denomination;

public record ContractSuggestion(
    int level,
    Denomination denomination
) {
}
