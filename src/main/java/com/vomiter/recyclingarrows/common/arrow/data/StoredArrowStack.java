package com.vomiter.recyclingarrows.common.arrow.data;

public final class StoredArrowStack {
    private final StoredArrow arrow;
    private int count;

    public StoredArrowStack(StoredArrow arrow, int count) {
        this.arrow = arrow.copy();
        this.count = count;
    }

    public StoredArrow getArrow() {
        return arrow;
    }

    public int getCount() {
        return count;
    }

    public void grow(int amount) {
        this.count += amount;
    }
}