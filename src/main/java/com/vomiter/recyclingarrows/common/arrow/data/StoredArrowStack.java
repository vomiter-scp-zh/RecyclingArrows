package com.vomiter.recyclingarrows.common.arrow.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StoredArrowStack {
    private final StoredArrow arrow;
    private final List<HitOctant> octants;

    public StoredArrowStack(StoredArrow arrow, int count) {
        this.arrow = arrow.copy();
        this.octants = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.octants.add(HitOctant.EAST_UP_SOUTH);
        }
    }

    public StoredArrowStack(StoredArrow arrow, List<HitOctant> octants) {
        this.arrow = arrow.copy();
        this.octants = new ArrayList<>(octants);
    }

    public StoredArrow getArrow() {
        return arrow;
    }

    public int getCount() {
        return octants.size();
    }

    public List<HitOctant> getOctants() {
        return Collections.unmodifiableList(octants);
    }

    public void addArrow(HitOctant octant) {
        octants.add(octant);
    }
}