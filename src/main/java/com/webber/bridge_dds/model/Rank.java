package com.webber.bridge_dds.model;


public enum Rank {

  TWO('2'),
  THREE('3'),
  FOUR('4'),
  FIVE('5'),
  SIX('6'),
  SEVEN('7'),
  EIGHT('8'),
  NINE('9'),
  TEN('T'),
  JACK('J'),
  QUEEN('Q'),
  KING('K'),
  ACE('A');
  private final char pbn;

  Rank(char pbn) {
    this.pbn = pbn;
  }

  public double points() {
      return switch (this) {
          case ACE -> 4.0;
          case KING -> 3.0;
          case QUEEN -> 2.0;
          case JACK -> 1.0;
          case TEN -> 0.5;
          default -> 0.0;
      };
  }



  public char toPbnChar() {
    return pbn;
  }
  
}
