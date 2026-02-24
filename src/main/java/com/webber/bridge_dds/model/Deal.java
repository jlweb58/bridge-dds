package com.webber.bridge_dds.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Deal {

    private Player first = Player.NORTH;

    private final Map<Player, Hand> hands = new EnumMap<>(Player.class);

    public Deal() {
        for (Player p : Player.values()) {
            hands.put(p, new Hand());
        }
    }

    public Player getFirst() {
        return first;
    }

    public void setFirst(Player first) {
        this.first = Objects.requireNonNull(first, "first");
    }

    public Hand hand(Player player) {
        return hands.get(player);
    }

    public Map<Player, Hand> viewHands() {
        return Collections.unmodifiableMap(hands);
    }

    /** Assign a card to a player; throws if the card already exists in another hand. */
    public Deal give(Player player, Card card) {
        Player owner = ownerOf(card);
        if (owner != null && owner != player) {
            throw new IllegalArgumentException("Card " + card + " already owned by " + owner);
        }
        hands.get(player).add(card);
        return this;
    }

    public Player ownerOf(Card card) {
        for (Player p : Player.values()) {
            if (hands.get(p).contains(card)) return p;
        }
        return null;
    }
}
