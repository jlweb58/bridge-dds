package com.webber.bridge_dds.service;

import com.webber.bridge_dds.jna.struct.DDTableResults;
import com.webber.bridge_dds.model.DoubleDummyResult;
import com.webber.bridge_dds.model.Player;
import com.webber.bridge_dds.model.Strain;

/**
 * Converts between DDS native results and our domain model.
 */
public class DdsResultConverter {

    /**
     * Convert a DDTableResults (from DDS library) to our DoubleDummyResult model.
     * <p>
     * DDS returns results in a 5x4 matrix where:
     * - Row index (strain): 0=Spades, 1=Hearts, 2=Diamonds, 3=Clubs, 4=NoTrump
     * - Column index (player): 0=North, 1=East, 2=South, 3=West
     * - Value: number of tricks that player can make as declarer in that strain
     *
     * @param ddTableResults The raw DDS result structure
     * @return Converted DoubleDummyResult
     */
    public static DoubleDummyResult convert(DDTableResults ddTableResults) {
        DoubleDummyResult result = new DoubleDummyResult();

        Strain[] strains = {Strain.SPADES, Strain.HEARTS, Strain.DIAMONDS, Strain.CLUBS, Strain.NOTRUMP};
        Player[] players = {Player.NORTH, Player.EAST, Player.SOUTH, Player.WEST};

        for (int strainIndex = 0; strainIndex < 5; strainIndex++) {
            for (int playerIndex = 0; playerIndex < 4; playerIndex++) {
                int tricks = ddTableResults.get(strainIndex, playerIndex);
                result.setTricks(strains[strainIndex], players[playerIndex], tricks);
            }
        }

        return result;
    }
}
