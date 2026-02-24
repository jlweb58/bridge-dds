package com.webber.bridge_dds.model;


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
}
