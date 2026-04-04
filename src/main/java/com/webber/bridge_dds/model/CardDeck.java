package com.webber.bridge_dds.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class CardDeck {
    private List<Card> deck = new ArrayList<>(Card.values().length);
    private final Random random;

    public CardDeck() {
        this (new SecureRandom());
    }

    // Useful for tests
    CardDeck(long seed) {
        this(new java.util.Random(seed));
    }

    CardDeck(Random random) {
        this.random = random;
        Collections.addAll(deck, Card.values());
        shuffle();
    }

    private void shuffle() {
        Collections.shuffle(deck, random);
    }

    public Card dealCard() {
        if (deck.isEmpty()) {
            throw new IllegalStateException("No cards in this deck");
        }
        return deck.removeFirst();
    }
}
