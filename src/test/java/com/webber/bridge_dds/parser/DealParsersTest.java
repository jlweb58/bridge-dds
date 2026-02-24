package com.webber.bridge_dds.parser;

import com.webber.bridge_dds.model.Deal;
import static com.webber.bridge_dds.model.Player.*;

import org.junit.jupiter.api.Test;
import static com.webber.bridge_dds.model.Card.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DealParsersTest {

    private static final String pbn1 = "N:QJ6.K652.J85.T98 873.J97.AT764.Q4 K5.T83.KQ9.A7652 AT942.AQ4.32.KJ3";
    @Test
    public void testDealToPbn() {

        Deal deal = new Deal();
        // North hand
        deal.give(NORTH, SQ).give(NORTH, SJ).give(NORTH, S6);
        deal.give(NORTH, HK).give(NORTH, H6).give(NORTH, H5).give(NORTH, H2);
        deal.give(NORTH, DJ).give(NORTH, D8).give(NORTH, D5);
        deal.give(NORTH, CT).give(NORTH, C9).give(NORTH, C8);
        // East hand
        deal.give(EAST, S8).give(EAST, S7).give(EAST, S3);
        deal.give(EAST, HJ).give(EAST, H9).give(EAST, H7);
        deal.give(EAST, DA).give(EAST, DT).give(EAST, D7).give(EAST, D6).give(EAST, D4);
        deal.give(EAST, CQ).give(EAST, C4);
        // South hand
        deal.give(SOUTH, SK).give(SOUTH, S5);
        deal.give(SOUTH, HT).give(SOUTH, H8).give(SOUTH, H3);
        deal.give(SOUTH, DK).give(SOUTH, DQ).give(SOUTH, D9);
        deal.give(SOUTH, CA).give(SOUTH, C7).give(SOUTH, C6).give(SOUTH, C5).give(SOUTH, C2);
        // West hand
        deal.give(WEST, SA).give(WEST, ST).give(WEST, S9).give(WEST, S4).give(WEST, S2);
        deal.give(WEST, HA).give(WEST, HQ).give(WEST, H4);
        deal.give(WEST, D3).give(WEST, D2);
        deal.give(WEST, CK).give(WEST, CJ).give(WEST, C3);


        assertEquals(pbn1, DealParsers.toPbn(deal));
    }

    @Test
    public void testPbnToDeal() {
        Deal deal = DealParsers.fromPbn(pbn1);
        assertEquals(NORTH, deal.ownerOf(SQ));
        assertEquals(NORTH, deal.ownerOf(SJ));
        assertEquals(NORTH, deal.ownerOf(S6));
        assertEquals(NORTH, deal.ownerOf(HK));
        assertEquals(NORTH, deal.ownerOf(H6));
        assertEquals(NORTH, deal.ownerOf(H5));
        assertEquals(NORTH, deal.ownerOf(H2));
        assertEquals(NORTH, deal.ownerOf(DJ));
        assertEquals(NORTH, deal.ownerOf(D8));
        assertEquals(NORTH, deal.ownerOf(D5));
        assertEquals(NORTH, deal.ownerOf(CT));
        assertEquals(NORTH, deal.ownerOf(C9));
        assertEquals(NORTH, deal.ownerOf(C8));
        assertEquals(EAST, deal.ownerOf(S8));
        assertEquals(EAST, deal.ownerOf(S7));
        assertEquals(EAST, deal.ownerOf(S3));
        assertEquals(EAST, deal.ownerOf(HJ));
        assertEquals(EAST, deal.ownerOf(H9));
        assertEquals(EAST, deal.ownerOf(H7));
        assertEquals(EAST, deal.ownerOf(DA));
        assertEquals(EAST, deal.ownerOf(DT));
        assertEquals(EAST, deal.ownerOf(D7));
        assertEquals(EAST, deal.ownerOf(D6));
        assertEquals(EAST, deal.ownerOf(D4));
        assertEquals(EAST, deal.ownerOf(CQ));
        assertEquals(EAST, deal.ownerOf(C4));
        assertEquals(SOUTH, deal.ownerOf(SK));
        assertEquals(SOUTH, deal.ownerOf(S5));
        assertEquals(SOUTH, deal.ownerOf(HT));
        assertEquals(SOUTH, deal.ownerOf(H8));
        assertEquals(SOUTH, deal.ownerOf(H3));
        assertEquals(SOUTH, deal.ownerOf(DK));
        assertEquals(SOUTH, deal.ownerOf(DQ));
        assertEquals(SOUTH, deal.ownerOf(D9));
        assertEquals(SOUTH, deal.ownerOf(CA));
        assertEquals(SOUTH, deal.ownerOf(C7));
        assertEquals(SOUTH, deal.ownerOf(C6));
        assertEquals(SOUTH, deal.ownerOf(C5));
        assertEquals(SOUTH, deal.ownerOf(C2));
        assertEquals(WEST, deal.ownerOf(SA));
        assertEquals(WEST, deal.ownerOf(ST));
        assertEquals(WEST, deal.ownerOf(S9));
        assertEquals(WEST, deal.ownerOf(S4));
        assertEquals(WEST, deal.ownerOf(S2));
        assertEquals(WEST, deal.ownerOf(HA));
        assertEquals(WEST, deal.ownerOf(HQ));
        assertEquals(WEST, deal.ownerOf(H4));
        assertEquals(WEST, deal.ownerOf(D3));
        assertEquals(WEST, deal.ownerOf(D2));
        assertEquals(WEST, deal.ownerOf(CK));
        assertEquals(WEST, deal.ownerOf(CJ));
        assertEquals(WEST, deal.ownerOf(C3));




    }

}
