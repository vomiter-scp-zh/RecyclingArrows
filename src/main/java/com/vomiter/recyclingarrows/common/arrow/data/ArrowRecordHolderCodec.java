package com.vomiter.recyclingarrows.common.arrow.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class ArrowRecordHolderCodec {
    private static final String KEY_ARROWS = "Arrows";
    private static final String KEY_ITEM = "Item";
    private static final String KEY_TAG = "Tag";
    private static final String KEY_OCTANTS = "Octants";

    // 可選：保留舊版相容
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

            ListTag octants = new ListTag();
            for (HitOctant octant : arrow.getOctants()) {
                octants.add(IntTag.valueOf(octant.ordinal()));
            }
            entry.put(KEY_OCTANTS, octants);

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

            List<HitOctant> octants = new ArrayList<>();

            if (entry.contains(KEY_OCTANTS, Tag.TAG_LIST)) {
                ListTag octantList = entry.getList(KEY_OCTANTS, Tag.TAG_INT);
                for (int j = 0; j < octantList.size(); j++) {
                    int ordinal = octantList.getInt(j);
                    octants.add(HitOctant.byOrdinalSafe(ordinal));
                }
            }
            // 舊存檔相容：如果還沒有 Octants，就退回 Count
            else if (entry.contains(KEY_COUNT, Tag.TAG_INT)) {
                int count = Math.max(0, entry.getInt(KEY_COUNT));
                for (int j = 0; j < count; j++) {
                    octants.add(HitOctant.EAST_UP_SOUTH);
                }
            }
            else {
                octants.add(HitOctant.EAST_UP_SOUTH);
            }

            holder.addArrow(new StoredArrowStack(new StoredArrow(itemId, stackTag), octants));
        }
    }
}