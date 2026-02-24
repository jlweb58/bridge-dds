package com.webber.bridge_dds.parser;

import com.webber.bridge_dds.model.Card;
import com.webber.bridge_dds.model.Deal;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Rank;
import com.webber.bridge_dds.model.Suit;

public class DealParsers {
    private DealParsers() {}

    public static Deal fromPbn(String pbn) {
        // Format: "N:spades.hearts.diamonds.clubs spades.hearts.diamonds.clubs spades.hearts.diamonds.clubs spades.hearts.diamonds.clubs"
        if (pbn == null || pbn.length() < 2 || pbn.charAt(1) != ':') {
            throw new IllegalArgumentException("Invalid PBN prefix, expected like \"N:...\": " + pbn);
        }

        Player first = switch (Character.toUpperCase(pbn.charAt(0))) {
            case 'N' -> Player.NORTH;
            case 'E' -> Player.EAST;
            case 'S' -> Player.SOUTH;
            case 'W' -> Player.WEST;
            default -> throw new IllegalArgumentException("Invalid first player: " + pbn.charAt(0));
        };

        String rest = pbn.substring(2).trim();
        String[] handStrings = rest.split(" ");
        if (handStrings.length != 4) {
            throw new IllegalArgumentException("Expected 4 hands after prefix, got " + handStrings.length);
        }

        Player[] order = rotateFrom(first);

        Deal deal = new Deal();
        deal.setFirst(first);

        for (int i = 0; i < 4; i++) {
            parseHandInto(deal, order[i], handStrings[i]);
        }

        return deal;
    }

    public static String toPbn(Deal deal) {
        if (deal == null) throw new IllegalArgumentException("deal must be non-null");

        Player first = deal.getFirst();
        Player[] order = rotateFrom(first);

        StringBuilder sb = new StringBuilder(80);
        sb.append(first.toPbnChar()).append(':');

        for (int i = 0; i < order.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(handToPbn(deal, order[i]));
        }
        return sb.toString();
    }

    private static String handToPbn(Deal deal, Player player) {
        var view = deal.hand(player).view(); // Map<Suit, Set<Rank>>
        return suitToPbn(view, Suit.SPADES) + "."
                + suitToPbn(view, Suit.HEARTS) + "."
                + suitToPbn(view, Suit.DIAMONDS) + "."
                + suitToPbn(view, Suit.CLUBS);
    }

    private static String suitToPbn(java.util.Map<Suit, java.util.Set<Rank>> view, Suit suit) {
        java.util.Set<Rank> ranks = view.get(suit);
        if (ranks == null || ranks.isEmpty()) return "-";

        // Your Rank enum is TWO..ACE, so iterate backwards for A..2.
        StringBuilder sb = new StringBuilder();
        Rank[] values = Rank.values();
        for (int i = values.length - 1; i >= 0; i--) {
            Rank r = values[i];
            if (ranks.contains(r)) sb.append(r.toPbnChar());
        }
        return sb.toString();
    }


    private static void parseHandInto(Deal deal, Player player, String handPbn) {
        String[] suits = handPbn.split("\\.");
        if (suits.length != 4) throw new IllegalArgumentException("Hand must have 4 suits: " + handPbn);

        putSuit(deal, player, Suit.SPADES, suits[0]);
        putSuit(deal, player, Suit.HEARTS, suits[1]);
        putSuit(deal, player, Suit.DIAMONDS, suits[2]);
        putSuit(deal, player, Suit.CLUBS, suits[3]);
    }

    private static void putSuit(Deal deal, Player player, Suit suit, String ranks) {
        if (ranks.equals("-") || ranks.isEmpty()) return;
        for (int i = 0; i < ranks.length(); i++) {
            Rank r = rankFromPbn(ranks.charAt(i));
            deal.give(player, Card.of(suit, r));
        }
    }

    private static Rank rankFromPbn(char c) {
        return switch (Character.toUpperCase(c)) {
            case '2' -> Rank.TWO;
            case '3' -> Rank.THREE;
            case '4' -> Rank.FOUR;
            case '5' -> Rank.FIVE;
            case '6' -> Rank.SIX;
            case '7' -> Rank.SEVEN;
            case '8' -> Rank.EIGHT;
            case '9' -> Rank.NINE;
            case 'T' -> Rank.TEN;
            case 'J' -> Rank.JACK;
            case 'Q' -> Rank.QUEEN;
            case 'K' -> Rank.KING;
            case 'A' -> Rank.ACE;
            default -> throw new IllegalArgumentException("Invalid rank char: " + c);
        };
    }

    private static Player[] rotateFrom(Player first) {
        return switch (first) {
            case NORTH -> new Player[]{Player.NORTH, Player.EAST, Player.SOUTH, Player.WEST};
            case EAST -> new Player[]{Player.EAST, Player.SOUTH, Player.WEST, Player.NORTH};
            case SOUTH -> new Player[]{Player.SOUTH, Player.WEST, Player.NORTH, Player.EAST};
            case WEST -> new Player[]{Player.WEST, Player.NORTH, Player.EAST, Player.SOUTH};
        };
    }
}
