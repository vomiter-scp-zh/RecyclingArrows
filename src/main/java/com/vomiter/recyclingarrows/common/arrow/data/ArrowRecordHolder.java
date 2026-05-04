package com.vomiter.recyclingarrows.common.arrow.data;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ArrowRecordHolder implements IArrowRecordHolder {

    private final List<StoredArrowStack> arrows = new ArrayList<>();

    @Override
    public void addArrow(StoredArrowStack arrow) {
        if (arrow == null) {
            return;
        }

        for (StoredArrowStack stack : arrows) {
            if (canStack(stack.getArrow(), arrow.getArrow())) {
                arrow.getOctants().forEach(stack::addArrow);
                return;
            }
        }

        arrows.add(new StoredArrowStack(arrow.getArrow(), arrow.getOctants()));
    }

    @Override
    public StoredArrow removeArrow() {
        if (arrows.isEmpty()) {
            return null;
        }

        StoredArrowStack stack = arrows.get(arrows.size() - 1);
        StoredArrow removed = stack.getArrow().copy();

        stack.removeArrow();

        if (stack.getCount() <= 0) {
            arrows.remove(arrows.size() - 1);
        }

        return removed;
    }

    @Override
    public StoredArrow removeArrow(StoredArrow arrow) {
        if (arrow == null) {
            return null;
        }

        for (int i = arrows.size() - 1; i >= 0; i--) {
            StoredArrowStack stack = arrows.get(i);

            if (!canStack(stack.getArrow(), arrow)) {
                continue;
            }

            StoredArrow removed = stack.getArrow().copy();

            stack.removeArrow();

            if (stack.getCount() <= 0) {
                arrows.remove(i);
            }

            return removed;
        }

        return null;
    }

    @Override
    public List<StoredArrowStack> getArrows() {
        return Collections.unmodifiableList(arrows);
    }

    @Override
    public void clear() {
        arrows.clear();
    }

    private static boolean canStack(StoredArrow a, StoredArrow b) {
        if (!a.itemId().equals(b.itemId())) {
            return false;
        }

        CompoundTag ta = a.tag() == null ? new CompoundTag() : a.tag();
        CompoundTag tb = b.tag() == null ? new CompoundTag() : b.tag();

        return ta.equals(tb);
    }
}