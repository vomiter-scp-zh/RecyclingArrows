package com.vomiter.recyclingarrows.common.arrow.logic;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IArrowItemAccessor {
    AbstractArrow recyclingarrows$getArrowEntity(Level level, ItemStack stack, LivingEntity owner);
}
