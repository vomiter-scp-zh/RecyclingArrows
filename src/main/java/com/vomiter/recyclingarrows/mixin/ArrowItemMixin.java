package com.vomiter.recyclingarrows.mixin;

import com.vomiter.recyclingarrows.common.arrow.logic.IArrowItemAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ArrowItem.class)
public abstract class ArrowItemMixin implements IArrowItemAccessor {
    @Shadow
    public abstract AbstractArrow createArrow(Level p_40513_, ItemStack p_40514_, LivingEntity p_40515_);

    @Override
    public AbstractArrow recyclingarrows$getArrowEntity(Level level, ItemStack stack, LivingEntity owner) {
        return createArrow(level, stack, owner);
    }
}
