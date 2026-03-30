package com.vomiter.recyclingarrows.common.arrow.data;

public enum HitOctant {
    WEST_DOWN_NORTH(-1, -1, -1),
    WEST_DOWN_SOUTH(-1, -1,  1),
    WEST_UP_NORTH  (-1,  1, -1),
    WEST_UP_SOUTH  (-1,  1,  1),
    EAST_DOWN_NORTH( 1, -1, -1),
    EAST_DOWN_SOUTH( 1, -1,  1),
    EAST_UP_NORTH  ( 1,  1, -1),
    EAST_UP_SOUTH  ( 1,  1,  1);

    private final int xSign;
    private final int ySign;
    private final int zSign;

    HitOctant(int xSign, int ySign, int zSign) {
        this.xSign = xSign;
        this.ySign = ySign;
        this.zSign = zSign;
    }

    public int xSign() {
        return xSign;
    }

    public int ySign() {
        return ySign;
    }

    public int zSign() {
        return zSign;
    }

    public static HitOctant fromSigns(boolean east, boolean up, boolean south) {
        if (east) {
            if (up) {
                return south ? EAST_UP_SOUTH : EAST_UP_NORTH;
            } else {
                return south ? EAST_DOWN_SOUTH : EAST_DOWN_NORTH;
            }
        } else {
            if (up) {
                return south ? WEST_UP_SOUTH : WEST_UP_NORTH;
            } else {
                return south ? WEST_DOWN_SOUTH : WEST_DOWN_NORTH;
            }
        }
    }

    public static HitOctant byOrdinalSafe(int ordinal) {
        HitOctant[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return EAST_UP_SOUTH;
        }
        return values[ordinal];
    }
}