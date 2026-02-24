package com.webber.bridge_dds.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class Hand {

    private final Map<Suit, EnumSet<Rank>> cards = new EnumMap<>(Suit.class);

    public Hand() {
        for (Suit s : Suit.values()) {
            cards.put(s, EnumSet.noneOf(Rank.class));
        }
    }

    public void add(Card card) {
        cards.get(card.suit()).add(card.rank());
    }

    public void remove(Card card) {
        cards.get(card.suit()).remove(card.rank());
    }

    public boolean contains(Card card) {
        return cards.get(card.suit()).contains(card.rank());
    }

    public int size() {
        int total = 0;
        for (Suit s : Suit.values()) total += cards.get(s).size();
        return total;
    }

    /** Read-only view for JSON/debugging/inspection. */
    public Map<Suit, Set<Rank>> view() {
        Map<Suit, Set<Rank>> out = new EnumMap<>(Suit.class);
        for (Suit s : Suit.values()) {
            out.put(s, Collections.unmodifiableSet(cards.get(s)));
        }
        return Collections.unmodifiableMap(out);
    }

}
