package com.vomiter.recyclingarrows.common.arrow.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ArrowRecordHolderCodec {
    private static final String KEY_ARROWS = "Arrows";
    private static final String KEY_ITEM = "Item";
    private static final String KEY_STACK = "Stack";
    private static final String KEY_OCTANTS = "Octants";
    private static final String KEY_COUNT = "Count";

    private ArrowRecordHolderCodec() {
    }

    public static CompoundTag save(IArrowRecordHolder holder, HolderLookup.Provider registries) {
        CompoundTag out = new CompoundTag();
        ListTag list = new ListTag();
        for (StoredArrowStack arrow : holder.getArrows()) {
            CompoundTag entry = new CompoundTag();
            entry.putString(KEY_ITEM, arrow.getArrow().itemId().toString());
            entry.put(KEY_STACK, arrow.getArrow().save(registries));
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

    public static void load(IArrowRecordHolder holder, CompoundTag tag, HolderLookup.Provider registries) {
        holder.clear();

        if (tag == null || !tag.contains(KEY_ARROWS)) {
            return;
        }

        ListTag list = tag.getList(KEY_ARROWS).get();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i).get();

            if (!entry.contains(KEY_ITEM)) {
                continue;
            }

            Identifier itemId = Identifier.tryParse(entry.getString(KEY_ITEM).get());
            if (itemId == null) {
                continue;
            }

            List<HitOctant> octants = new ArrayList<>();

            if (entry.contains(KEY_OCTANTS)) {
                ListTag octantList = entry.getList(KEY_OCTANTS).get();
                for (int j = 0; j < octantList.size(); j++) {
                    int ordinal = octantList.getInt(j).get();
                    octants.add(HitOctant.byOrdinalSafe(ordinal));
                }
            }
            // 舊存檔相容：如果還沒有 Octants，就退回 Count
            else if (entry.contains(KEY_COUNT)) {
                int count = Math.max(0, entry.getInt(KEY_COUNT).get());
                for (int j = 0; j < count; j++) {
                    octants.add(HitOctant.EAST_UP_SOUTH);
                }
            }
            else {
                octants.add(HitOctant.EAST_UP_SOUTH);
            }

            holder.addArrow(new StoredArrowStack(StoredArrow.load(entry.getCompound(KEY_STACK).get(), registries), octants));
        }
    }


    public static void save(IArrowRecordHolder holder, ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList(KEY_ARROWS);

        for (StoredArrowStack arrow : holder.getArrows()) {
            ValueOutput entry = list.addChild();

            entry.putString(KEY_ITEM, arrow.getArrow().itemId().toString());
            entry.store(KEY_STACK, ItemStack.CODEC, arrow.getArrow().stack());
            ValueOutput.TypedOutputList<@NotNull Integer> octants =
                    entry.list(KEY_OCTANTS, Codec.INT);

            for (HitOctant octant : arrow.getOctants()) {
                octants.add(octant.ordinal());
            }
        }

        if (list.isEmpty()) {
            output.discard(KEY_ARROWS);
        }
    }

    public static void load(IArrowRecordHolder holder, ValueInput input) {
        holder.clear();

        for (ValueInput entry : input.childrenListOrEmpty(KEY_ARROWS)) {
            Optional<String> itemIdString = entry.getString(KEY_ITEM);
            if (itemIdString.isEmpty()) {
                continue;
            }

            Identifier itemId = Identifier.tryParse(itemIdString.get());
            if (itemId == null) {
                continue;
            }

            Optional<ItemStack> stack = entry.read(KEY_STACK, ItemStack.CODEC);
            if (stack.isEmpty() || stack.get().isEmpty()) {
                continue;
            }

            List<HitOctant> octants = new ArrayList<>();

            for (int ordinal : entry.listOrEmpty(KEY_OCTANTS, Codec.INT)) {
                octants.add(HitOctant.byOrdinalSafe(ordinal));
            }

            if (octants.isEmpty()) {
                int count = entry.getIntOr(KEY_COUNT, 0);

                if (count > 0) {
                    for (int j = 0; j < count; j++) {
                        octants.add(HitOctant.EAST_UP_SOUTH);
                    }
                } else {
                    octants.add(HitOctant.EAST_UP_SOUTH);
                }
            }

            holder.addArrow(new StoredArrowStack(
                    new StoredArrow(itemId, stack.get()),
                    octants
            ));
        }
    }
}