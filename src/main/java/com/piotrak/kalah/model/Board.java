package com.piotrak.kalah.model;

import java.util.Map;

/**
 * Immutable representation of the Kalah game board.
 */
public record Board(Map<Integer, Integer> status) {

    public Board {
        status = Map.copyOf(status);
    }

    /**
     * Gets the number of rocks in a pit.
     * @param pit the pit number
     * @return the number of rocks
     */
    public int getRocks(int pit) {
        return status.get(pit);
    }

    /**
     * Checks if a pit is empty.
     * @param pit the pit number
     * @return true if empty
     */
    public boolean isEmpty(int pit) {
        return getRocks(pit) == 0;
    }
}
