package com.webber.bridge_dds.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents the double-dummy analysis result for a bridge deal.
 * For each strain (suit or notrump) and each player, indicates the maximum
 * number of tricks that player can take as declarer.
 */
public class DoubleDummyResult {

    private final Map<Strain, Map<Player, Integer>> table = new EnumMap<>(Strain.class);

    public DoubleDummyResult() {
        for (Strain strain : Strain.values()) {
            table.put(strain, new EnumMap<>(Player.class));
        }
    }

    /**
     * Set the number of tricks a player can make as declarer in a given strain.
     *
     * @param strain The strain (suit or notrump)
     * @param player The player (declarer)
     * @param tricks Number of tricks (0-13)
     */
    public void setTricks(Strain strain, Player player, int tricks) {
        table.get(strain).put(player, tricks);
    }

    /**
     * Get the number of tricks a player can make as declarer in a given strain.
     *
     * @param strain The strain (suit or notrump)
     * @param player The player (declarer)
     * @return Number of tricks (0-13), or null if not set
     */
    public Integer getTricks(Strain strain, Player player) {
        return table.get(strain).get(player);
    }

    /**
     * Get the full table of results.
     *
     * @return Map of strain -> (player -> tricks)
     */
    public Map<Strain, Map<Player, Integer>> getTable() {
        return table;
    }
}
