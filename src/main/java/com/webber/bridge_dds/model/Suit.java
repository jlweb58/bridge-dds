package com.webber.bridge_dds.model;


import com.fasterxml.jackson.annotation.JsonCreator;

public enum Suit {
  SPADES('S'),
  HEARTS('H'),
  DIAMONDS('D'),
  CLUBS('C');

  private final char pbn;

  Suit(char pbn) {
    this.pbn = pbn;
  }

  public char toPbnChar() {
    return pbn;
  }

  @JsonCreator
  public static Suit fromJson(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Suit value must not be blank");
    }
    String normalized = value.trim().toUpperCase();
    return switch (normalized) {
      case "S", "SPADES" -> SPADES;
      case "H", "HEARTS" -> HEARTS;
      case "D", "DIAMONDS" -> DIAMONDS;
      case "C", "CLUBS" -> CLUBS;
      default -> throw new IllegalArgumentException("Unknown suit: " + value);
    };
  }
}
