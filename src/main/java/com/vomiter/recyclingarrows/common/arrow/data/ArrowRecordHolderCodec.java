package com.vomiter.recyclingarrows.common.arrow.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public final class ArrowRecordHolderCodec {
    private static final String KEY_ARROWS = "Arrows";
    private static final String KEY_ITEM = "Item";
    private static final String KEY_TAG = "Tag";
    private static final String KEY_COUNT = "Count";

    private ArrowRecordHolderCodec() {
    }

    public static CompoundTag save(IArrowRecordHolder holder) {
        CompoundTag out = new CompoundTag();
        ListTag list = new ListTag();

        for (StoredArrowStack arrow : holder.getArrows()) {
            CompoundTag entry = new CompoundTag();
            entry.putString(KEY_ITEM, arrow.getArrow().itemId().toString());
            entry.put(KEY_TAG, arrow.getArrow().tag() == null ? new CompoundTag() : arrow.getArrow().tag().copy());
            entry.putInt(KEY_COUNT, arrow.getCount());
            list.add(entry);
        }

        out.put(KEY_ARROWS, list);
        return out;
    }

    public static void load(IArrowRecordHolder holder, CompoundTag tag) {
        holder.clear();

        if (tag == null || !tag.contains(KEY_ARROWS, Tag.TAG_LIST)) {
            return;
        }

        ListTag list = tag.getList(KEY_ARROWS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);

            if (!entry.contains(KEY_ITEM, Tag.TAG_STRING)) {
                continue;
            }

            ResourceLocation itemId = ResourceLocation.tryParse(entry.getString(KEY_ITEM));
            if (itemId == null) {
                continue;
            }

            CompoundTag stackTag = entry.contains(KEY_TAG, Tag.TAG_COMPOUND)
                    ? entry.getCompound(KEY_TAG).copy()
                    : new CompoundTag();

            int count = entry.contains(KEY_COUNT)
                    ? entry.getInt(KEY_COUNT)
                    : 1;

            holder.addArrow(new StoredArrowStack(new StoredArrow(itemId, stackTag), count));
        }
    }
}