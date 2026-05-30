package com.vomiter.recyclingarrows.common.arrow.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record StoredArrow(ResourceLocation itemId, ItemStack stack) {

    public StoredArrow {
        stack = stack == null ? ItemStack.EMPTY : stack.copyWithCount(1);
    }

    private static ResourceLocation getId(ItemStack stack1){
        return BuiltInRegistries.ITEM.getKey(stack1.getItem());
    }

    public StoredArrow copy() {
        return new StoredArrow(itemId, stack.copyWithCount(1));
    }

    public boolean isEmpty(){
        return stack.isEmpty();
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        if (stack.isEmpty()) {
            return new CompoundTag();
        }

        Tag saved = stack.save(registries);
        return saved instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    public static StoredArrow load(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag == null || tag.isEmpty()) {
            return new StoredArrow(BuiltInRegistries.ITEM.getKey(Items.AIR), ItemStack.EMPTY);
        }

        ItemStack stack = ItemStack.parseOptional(registries, tag);
        return new StoredArrow(getId(stack), stack);
    }


    public StoredArrow copyAs(ResourceLocation newItemId) {
        return new StoredArrow(newItemId, copyAsStack(newItemId));
    }

    public ItemStack copyAsStack(ResourceLocation newItemId) {
        Item newItem = BuiltInRegistries.ITEM.get(newItemId);
        if (newItem == null) {
            return ItemStack.EMPTY;
        }

        ItemStack copied = stack.copyWithCount(1);
        copied.transmuteCopy(newItem, 1);
        return copied;
    }
}