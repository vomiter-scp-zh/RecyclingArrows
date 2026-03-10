package com.vomiter.recyclingarrows.common.arrow.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record StoredArrow(ResourceLocation itemId, CompoundTag tag) {
    public StoredArrow copy() {
        return new StoredArrow(itemId, tag == null ? new CompoundTag() : tag.copy());
    }
}