package com.webber.bridge_dds.model;

public enum Card {

    SA(Suit.SPADES, Rank.ACE),
    SK(Suit.SPADES, Rank.KING),
    SQ(Suit.SPADES, Rank.QUEEN),
    SJ(Suit.SPADES, Rank.JACK),
    ST(Suit.SPADES, Rank.TEN),
    S9(Suit.SPADES, Rank.NINE),
    S8(Suit.SPADES, Rank.EIGHT),
    S7(Suit.SPADES, Rank.SEVEN),
    S6(Suit.SPADES, Rank.SIX),
    S5(Suit.SPADES, Rank.FIVE),
    S4(Suit.SPADES, Rank.FOUR),
    S3(Suit.SPADES, Rank.THREE),
    S2(Suit.SPADES, Rank.TWO),

    HA(Suit.HEARTS, Rank.ACE),
    HK(Suit.HEARTS, Rank.KING),
    HQ(Suit.HEARTS, Rank.QUEEN),
    HJ(Suit.HEARTS, Rank.JACK),
    HT(Suit.HEARTS, Rank.TEN),
    H9(Suit.HEARTS, Rank.NINE),
    H8(Suit.HEARTS, Rank.EIGHT),
    H7(Suit.HEARTS, Rank.SEVEN),
    H6(Suit.HEARTS, Rank.SIX),
    H5(Suit.HEARTS, Rank.FIVE),
    H4(Suit.HEARTS, Rank.FOUR),
    H3(Suit.HEARTS, Rank.THREE),
    H2(Suit.HEARTS, Rank.TWO),

    DA(Suit.DIAMONDS, Rank.ACE),
    DK(Suit.DIAMONDS, Rank.KING),
    DQ(Suit.DIAMONDS, Rank.QUEEN),
    DJ(Suit.DIAMONDS, Rank.JACK),
    DT(Suit.DIAMONDS, Rank.TEN),
    D9(Suit.DIAMONDS, Rank.NINE),
    D8(Suit.DIAMONDS, Rank.EIGHT),
    D7(Suit.DIAMONDS, Rank.SEVEN),
    D6(Suit.DIAMONDS, Rank.SIX),
    D5(Suit.DIAMONDS, Rank.FIVE),
    D4(Suit.DIAMONDS, Rank.FOUR),
    D3(Suit.DIAMONDS, Rank.THREE),
    D2(Suit.DIAMONDS, Rank.TWO),

    CA(Suit.CLUBS, Rank.ACE),
    CK(Suit.CLUBS, Rank.KING),
    CQ(Suit.CLUBS, Rank.QUEEN),
    CJ(Suit.CLUBS, Rank.JACK),
    CT(Suit.CLUBS, Rank.TEN),
    C9(Suit.CLUBS, Rank.NINE),
    C8(Suit.CLUBS, Rank.EIGHT),
    C7(Suit.CLUBS, Rank.SEVEN),
    C6(Suit.CLUBS, Rank.SIX),
    C5(Suit.CLUBS, Rank.FIVE),
    C4(Suit.CLUBS, Rank.FOUR),
    C3(Suit.CLUBS, Rank.THREE),
    C2(Suit.CLUBS, Rank.TWO);
    private final Suit suit;

    private final Rank rank;

    Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Rank rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
    }

    /** e.g. "S7", "HA", "DT" */
    public String toCode() {
        return "" + suit.toPbnChar() + rank.toPbnChar();
    }

    public static Card fromCode(String code) {
        if (code == null || code.length() != 2) {
            throw new IllegalArgumentException("Card code must be 2 chars like \"S7\" or \"HA\"");
        }
        char s = Character.toUpperCase(code.charAt(0));
        char r = Character.toUpperCase(code.charAt(1));
        // simplest: scan (only 52 cards, so this is fast enough)
        for (Card c : values()) {
            if (c.suit.toPbnChar() == s && c.rank.toPbnChar() == r) return c;
        }
        throw new IllegalArgumentException("Unknown card code: " + code);
    }

    public static Card of(Suit suit, Rank rank) {
        if (suit == null || rank == null) {
            throw new IllegalArgumentException("suit and rank must be non-null");
        }
        for (Card c : values()) {
            if (c.suit == suit && c.rank == rank) return c;
        }
        throw new IllegalStateException("No Card enum constant for " + suit + " " + rank);
    }
}
