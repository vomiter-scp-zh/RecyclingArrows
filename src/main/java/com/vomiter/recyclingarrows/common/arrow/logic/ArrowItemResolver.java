package com.vomiter.recyclingarrows.common.arrow.logic;

import com.vomiter.recyclingarrows.common.arrow.data.StoredArrow;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public final class ArrowItemResolver {
    private ArrowItemResolver() {
    }

    public static StoredArrow resolve(AbstractArrow arrow) {
        if (arrow == null) {
            return null;
        }

        ItemStack pickup = ((IArrowAccessor) arrow).recyclingarrows$getItem();
        return resolve(pickup);
    }

    public static StoredArrow resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) {
            return null;
        }

        return new StoredArrow(itemId, stack);
    }

    public static ItemStack build(StoredArrowStack storedArrowStack) {
        return build(storedArrowStack.getArrow()).copyWithCount(storedArrowStack.getCount());
    }

    public static ItemStack build(StoredArrow storedArrow) {
        var item = BuiltInRegistries.ITEM.get(storedArrow.itemId());
        return storedArrow.stack().copy();
    }
}