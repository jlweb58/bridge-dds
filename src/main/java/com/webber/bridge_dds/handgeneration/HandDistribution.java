package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Suit;

import java.util.EnumMap;

public record HandDistribution(EnumMap<Suit, SuitLengthRange> suitLengths) {
}
