package com.vomiter.recyclingarrows.common.arrow.logic;

import com.vomiter.recyclingarrows.common.arrow.data.StoredArrow;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class ArrowItemResolver {
    private ArrowItemResolver() {
    }

    public static StoredArrow resolve(AbstractArrow arrow) {
        if (arrow == null) {
            return null;
        }

        ItemStack pickup = ((IArrowAccessor)arrow).recyclingarrows$getItem();
        if (pickup == null) return null;
        if (pickup.isEmpty()) {
            return null;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(pickup.getItem());
        if (itemId == null) {
            return null;
        }

        CompoundTag tag = pickup.getTag() == null ? new CompoundTag() : pickup.getTag().copy();
        return new StoredArrow(itemId, tag);
    }

    public static ItemStack build(StoredArrowStack storedArrowStack){
        return build(storedArrowStack.getArrow()).copyWithCount(storedArrowStack.getCount());
    }


    public static ItemStack build(StoredArrow storedArrow){
        var item = BuiltInRegistries.ITEM.get(storedArrow.itemId());
        var stack = new ItemStack(item);
        var tag = storedArrow.tag();
        if(!tag.isEmpty()){
            stack.getOrCreateTag();
            stack.setTag(tag);
        }
        return stack;
    }
}