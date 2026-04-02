package com.webber.bridge_dds.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class Hand {

    private final Map<Suit, EnumSet<Rank>> cards = new EnumMap<>(Suit.class);

    @Getter
    @Setter
    private double evaluation = 0.0;

    public Hand() {
        for (Suit s : Suit.values()) {
            cards.put(s, EnumSet.noneOf(Rank.class));
        }
    }

    public Hand add(Card card) {
        if (this.size() >= 13) throw new IllegalStateException("Hand is full");
        if (contains(card)) throw new IllegalArgumentException("Hand already contains " + card);
        cards.get(card.suit()).add(card.rank());
        return this;
    }

    public boolean contains(Card card) {
        return cards.get(card.suit()).contains(card.rank());
    }

    public int size() {
        int total = 0;
        for (Suit s : Suit.values()) total += cards.get(s).size();
        return total;
    }

    public EnumSet<Rank> ranksForSuit(Suit suit) {
        return cards.get(suit);
    }

    /**
     * Read-only view for JSON/debugging/inspection.
     */
    public Map<Suit, Set<Rank>> view() {
        Map<Suit, Set<Rank>> out = new EnumMap<>(Suit.class);
        for (Suit s : Suit.values()) {
            out.put(s, Collections.unmodifiableSet(cards.get(s)));
        }
        return Collections.unmodifiableMap(out);
    }

}
