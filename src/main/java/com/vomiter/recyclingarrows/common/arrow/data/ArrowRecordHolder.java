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
                stack.grow(arrow.getCount());
                return;
            }
        }

        arrows.add(new StoredArrowStack(arrow.getArrow(), arrow.getCount()));
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