package com.vomiter.recyclingarrows.common.arrow.data;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record StoredArrow(Identifier itemId, ItemStack stack) {

    public StoredArrow {
        stack = stack == null ? ItemStack.EMPTY : stack.copyWithCount(1);
    }

    private static Identifier getId(ItemStack stack1){
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

        Tag saved = ItemStack.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .getOrThrow();
        return saved instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    public static StoredArrow load(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag == null || tag.isEmpty()) {
            return new StoredArrow(BuiltInRegistries.ITEM.getKey(Items.AIR), ItemStack.EMPTY);
        }

        ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(
                registries.createSerializationContext(NbtOps.INSTANCE),
                tag).result().orElse(ItemStack.EMPTY);
        return new StoredArrow(getId(stack), stack);
    }


    public StoredArrow copyAs(Identifier newItemId) {
        return new StoredArrow(newItemId, copyAsStack(newItemId));
    }

    public ItemStack copyAsStack(Identifier newItemId) {
        Item newItem = BuiltInRegistries.ITEM.get(newItemId).map(Holder.Reference::value).orElse(Items.AIR);
        if (newItem.getDefaultInstance().isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack copied = stack.copyWithCount(1);
        copied.transmuteCopy(newItem, 1);
        return copied;
    }
}